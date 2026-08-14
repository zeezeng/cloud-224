package com.mars.biz.douyu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 斗鱼弹幕 WebSocket 协议编解码。
 *
 * <p>数据包结构（小端序）：</p>
 * <pre>
 *   [4字节] 消息总长度（含自身4字节 + 8字节头 + 数据体）
 *   [2字节] 消息类型（0x02 客户端-&gt;服务器 / 0x03 服务器-&gt;客户端）
 *   [2字节] 加密类型（0x00 不加密）
 *   [4字节] 保留字段
 *   [数据体] 形如 key@=value/key@=value/ ...
 *   [1字节] 结束符 \0
 * </pre>
 */
public final class DouyuProtocol {

    private static final short MSG_TYPE_CLIENT = 0x02;
    private static final short MSG_TYPE_SERVER = 0x03;
    private static final int MAX_PACKET_LEN = 64 * 1024;

    private DouyuProtocol() {
    }

    /**
     * 构造发送数据包。
     *
     * @param content 形如 "type@=loginreq/roomid@=123/" 的内容
     * @return 待发送的二进制缓冲区
     */
    public static ByteBuffer encode(String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        int len = 12 + body.length + 1;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(len);
        buf.putShort(MSG_TYPE_CLIENT);
        buf.putShort((short) 0x00);
        buf.putInt(0);
        buf.put(body);
        buf.put((byte) 0x00);
        buf.flip();
        return buf;
    }

    /**
     * 解析服务器推送的二进制数据，逐包回调解析出的字段表。
     *
     * @param buffer  收到的二进制数据
     * @param handler 每个数据包的字段表回调
     */
    public static void decode(ByteBuffer buffer, Consumer<Map<String, String>> handler) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() >= 4) {
            int len = buffer.getInt();
            if (len <= 4 || len > MAX_PACKET_LEN) {
                return;
            }
            int bodyBytes = len - 4;
            if (buffer.remaining() < bodyBytes) {
                return;
            }
            byte[] packet = new byte[bodyBytes];
            buffer.get(packet);
            if (packet.length < 8) {
                continue;
            }
            ByteBuffer p = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
            short type = p.getShort();
            p.getShort(); // encrypt
            p.getInt();   // reserved
            int dataLen = packet.length - 8;
            if (dataLen <= 0) {
                continue;
            }
            if (type != MSG_TYPE_SERVER && type != MSG_TYPE_CLIENT) {
                continue;
            }
            byte[] data = new byte[dataLen];
            p.get(data);
            String text = new String(data, StandardCharsets.UTF_8);
            Map<String, String> fields = parseFields(text);
            if (fields != null && !fields.isEmpty()) {
                handler.accept(fields);
            }
        }
    }

    private static Map<String, String> parseFields(String text) {
        Map<String, String> fields = new LinkedHashMap<>();
        String[] parts = text.split("/", -1);
        for (String part : parts) {
            int idx = part.indexOf("@=");
            if (idx <= 0) {
                continue;
            }
            String key = part.substring(0, idx).trim();
            String value = part.substring(idx + 2);
            if (key.isEmpty()) {
                continue;
            }
            fields.putIfAbsent(key, value);
        }
        return fields;
    }
}