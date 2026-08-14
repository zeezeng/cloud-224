import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.net.InetSocketAddress;

public class RawTls {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "47.90.12.253";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8501;

        TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }};
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new java.security.SecureRandom());
        SSLSocketFactory factory = ctx.getSocketFactory();
        try (SSLSocket sock = (SSLSocket) factory.createSocket()) {
            SSLParameters params = new SSLParameters();
            params.setEndpointIdentificationAlgorithm("");
            params.setServerNames(java.util.List.of(new SNIHostName("danmuproxy.douyu.com")));
            sock.setSSLParameters(params);
            sock.connect(new InetSocketAddress(host, port), 8000);
            sock.startHandshake();
            System.out.println("HANDSHAKE OK, protocol=" + sock.getSession().getProtocol()
                    + " cipher=" + sock.getSession().getCipherSuite());
            // send douyu loginreq
            byte[] login = ("type@=loginreq/roomid@=75060/").getBytes("UTF-8");
            int len = 12 + login.length + 1;
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(len).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buf.putInt(len); buf.putShort((short)0x02); buf.putShort((short)0); buf.putInt(0);
            buf.put(login); buf.put((byte)0);
            OutputStream out = sock.getOutputStream();
            out.write(buf.array()); out.flush();
            InputStream in = sock.getInputStream();
            byte[] read = new byte[4096];
            sock.setSoTimeout(3000);
            try {
                int n = in.read(read);
                System.out.println("READ bytes=" + n + " first=" + new String(read, 0, Math.min(n, 120), "UTF-8"));
            } catch (Exception e) {
                System.out.println("READ exception after login: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("HANDSHAKE FAILED: " + e);
        }
        System.exit(0);
    }
}