import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class HttpWs {
    public static void main(String[] args) throws Exception {
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }};
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new SecureRandom());

        SSLParameters params = new SSLParameters();
        params.setProtocols(new String[]{"TLSv1.2"});
        params.setCipherSuites(new String[]{"TLS_RSA_WITH_AES_256_GCM_SHA384"});

        HttpClient client = HttpClient.newBuilder()
                .sslContext(ctx)
                .sslParameters(params)
                .connectTimeout(Duration.ofSeconds(6))
                .build();

        try {
            WebSocket ws = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .buildAsync(URI.create("wss://danmuproxy.douyu.com:8501"), new WebSocket.Listener() {
                        public void onOpen(WebSocket webSocket) {
                            System.out.println("OPEN");
                            webSocket.request(1);
                        }
                        public CompletionStage<?> onBinary(WebSocket webSocket, java.nio.ByteBuffer data, boolean last) {
                            System.out.println("BINARY bytes=" + data.remaining());
                            webSocket.request(1);
                            return null;
                        }
                        public void onError(WebSocket webSocket, Throwable error) {
                            System.out.println("ERROR: " + error);
                        }
                    }).join();
            Thread.sleep(3000);
            System.out.println("DONE connected=" + (ws != null));
        } catch (Exception e) {
            System.out.println("FAIL: " + e);
        }
        System.exit(0);
    }
}