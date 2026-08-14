package com.mars.biz.douyu;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 斗鱼弹幕 WebSocket 客户端。
 *
 * <p>负责连接弹幕服务器、登录房间、加入分组、维持心跳，并在断线后自动重连。</p>
 */
@Slf4j
public class DouyuDanmakuClient {

    private static final int HEARTBEAT_INTERVAL_MS = 45_000;
    private static final int MAX_RECONNECT_DELAY_MS = 60_000;
    private static final int INITIAL_RECONNECT_DELAY_MS = 5_000;

    private final String roomId;
    private final String anchorId;
    private final List<String> hosts;
    private final MessageHandler messageHandler;

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger hostCursor = new AtomicInteger(0);
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private volatile WebSocket webSocket;
    private volatile ScheduledFuture<?> heartbeatTask;
    private ScheduledFuture<?> reconnectTask;

    /**
     * 消息回调。
     */
    public interface MessageHandler {
        void onMessage(String roomId, Map<String, String> fields);
    }

    public DouyuDanmakuClient(String roomId, String anchorId, List<String> hosts, MessageHandler messageHandler) {
        this.roomId = roomId;
        this.anchorId = anchorId;
        this.hosts = hosts;
        this.messageHandler = messageHandler;
        this.httpClient = HttpClient.newBuilder()
                .sslContext(buildSslContext())
                .sslParameters(buildSslParameters())
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "douyu-danmaku-" + roomId);
            t.setDaemon(true);
            return t;
        });
    }

    private static SSLParameters buildSslParameters() {
        SSLParameters params = new SSLParameters();
        params.setProtocols(new String[]{"TLSv1.2"});
        params.setCipherSuites(new String[]{"TLS_RSA_WITH_AES_256_GCM_SHA384"});
        return params;
    }

    /**
     * 斗鱼弹幕服务器仅支持 RSA 密钥交换套件 TLS_RSA_WITH_AES_256_GCM_SHA384，
     * 该套件在 JDK 默认安全配置中被禁用，需放宽 disabledAlgorithms。
     */
    private static SSLContext buildSslContext() {
        try {
            String before = java.security.Security.getProperty("jdk.tls.disabledAlgorithms");
            java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }}, new SecureRandom());
            log.info("[斗鱼] java.home={} disabledAlgorithms before='{}' after='{}'", System.getProperty("java.home"), before,
                    java.security.Security.getProperty("jdk.tls.disabledAlgorithms"));
            log.info("[斗鱼] enabledCipherSuites={}", String.join(",", ctx.getSocketFactory().getDefaultCipherSuites()));
            log.info("[斗鱼] supportedHasRsa={}", contains(ctx.getSocketFactory().getSupportedCipherSuites(), "TLS_RSA_WITH_AES_256_GCM_SHA384"));
            log.info("[斗鱼] defaultHasRsa={}", contains(ctx.getSocketFactory().getDefaultCipherSuites(), "TLS_RSA_WITH_AES_256_GCM_SHA384"));
            return ctx;
        } catch (Exception e) {
            throw new IllegalStateException("初始化斗鱼 SSLContext 失败", e);
        }
    }

    private static boolean contains(String[] arr, String target) {
        for (String s : arr) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public void start() {
        running.set(true);
        connect();
    }

    public void stop() {
        running.set(false);
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
        }
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
        }
        WebSocket ws = this.webSocket;
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
            } catch (Exception ignored) {
            }
        }
        connected.set(false);
        scheduler.shutdownNow();
        log.info("[斗鱼] 已停止监听房间 {} 主播 {}", roomId, anchorId);
    }

    public boolean isConnected() {
        return connected.get();
    }

    private void connect() {
        if (!running.get()) {
            return;
        }
        if (hosts.isEmpty()) {
            scheduleReconnect("无可用弹幕服务器");
            return;
        }
        int index = Math.abs(hostCursor.getAndIncrement() % hosts.size());
        String host = hosts.get(index);
        try {
            WebSocket ws = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(host), new Listener())
                    .join();
            this.webSocket = ws;
            log.info("[斗鱼] 房间 {} 已建立连接 {}", roomId, host);
        } catch (Exception e) {
            log.warn("[斗鱼] 房间 {} 连接 {} 失败: {}", roomId, host, e.getMessage());
            scheduleReconnect(e.getMessage());
        }
    }

    private void scheduleReconnect(String reason) {
        if (!running.get()) {
            return;
        }
        connected.set(false);
        cancelHeartbeat();
        int delay = Math.min(INITIAL_RECONNECT_DELAY_MS * (1 + connectedAttemptsAndReset()), MAX_RECONNECT_DELAY_MS);
        log.warn("[斗鱼] 房间 {} 将重连 (原因: {})", roomId, reason);
        reconnectTask = scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
    }

    private final AtomicInteger failureCount = new AtomicInteger(0);

    private int connectedAttemptsAndReset() {
        return failureCount.getAndIncrement();
    }

    private void onConnected(WebSocket ws) {
        connected.set(true);
        failureCount.set(0);
        ws.sendBinary(DouyuProtocol.encode("type@=loginreq/roomid@=" + roomId + "/"), true);
        ws.sendBinary(DouyuProtocol.encode("type@=joingroup/rid@=" + roomId + "/gid@=-9999/"), true);
        cancelHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (connected.get()) {
                try {
                    webSocket.sendBinary(DouyuProtocol.encode("type@=mrkl/"), true);
                } catch (Exception ignored) {
                }
            }
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info("[斗鱼] 房间 {} 已登录并加入分组", roomId);
    }

    private void cancelHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
            heartbeatTask = null;
        }
    }

    private void onMessage(Map<String, String> fields) {
        try {
            messageHandler.onMessage(roomId, fields);
        } catch (Exception e) {
            log.warn("[斗鱼] 房间 {} 处理消息异常: {}", roomId, e.getMessage());
        }
    }

    /**
     * WebSocket 监听器。
     */
    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            onConnected(webSocket);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            try {
                DouyuProtocol.decode(data, Listener.this::dispatch);
            } catch (Exception e) {
                log.warn("[斗鱼] 房间 {} 解析数据异常: {}", roomId, e.getMessage());
            }
            webSocket.request(1);
            return null;
        }

        private void dispatch(Map<String, String> fields) {
            onMessage(fields);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("[斗鱼] 房间 {} 连接关闭 status={} reason={}", roomId, statusCode, reason);
            scheduleReconnect("onClose:" + statusCode);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("[斗鱼] 房间 {} 连接错误: {}", roomId, error.getMessage());
            scheduleReconnect(error.getMessage());
        }
    }
}