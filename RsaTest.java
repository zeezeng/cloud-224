import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.List;

public class RsaTest {
    public static void main(String[] args) throws Exception {
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        String host = args.length > 0 ? args[0] : "14.119.108.22";
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
            params.setServerNames(List.of(new SNIHostName("danmuproxy.douyu.com")));
            params.setProtocols(new String[]{"TLSv1.2"});
            params.setCipherSuites(new String[]{"TLS_RSA_WITH_AES_256_GCM_SHA384"});
            sock.setSSLParameters(params);
            sock.setSoTimeout(6000);
            sock.connect(new InetSocketAddress(host, port), 6000);
            sock.startHandshake();
            System.out.println("SUCCESS proto=" + sock.getSession().getProtocol()
                    + " cipher=" + sock.getSession().getCipherSuite());
        } catch (Exception e) {
            System.out.println("FAIL: " + e);
        }
    }
}