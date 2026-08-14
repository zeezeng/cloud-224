import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.net.InetSocketAddress;

public class CipherProbe {
    static String[] SUITES = {
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "TLS_RSA_WITH_AES_256_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
        "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256"
    };

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
        for (String suite : SUITES) {
            try (SSLSocket sock = (SSLSocket) factory.createSocket()) {
                SSLParameters params = new SSLParameters();
                params.setServerNames(java.util.List.of(new SNIHostName("danmuproxy.douyu.com")));
                params.setCipherSuites(new String[]{suite});
                params.setProtocols(new String[]{"TLSv1.2"});
                sock.setSSLParameters(params);
                sock.setSoTimeout(4000);
                sock.connect(new InetSocketAddress(host, port), 4000);
                sock.startHandshake();
                System.out.println("SUCCESS cipher=" + suite + " proto=" + sock.getSession().getProtocol());
            } catch (Exception e) {
                System.out.println("FAIL cipher=" + suite + " : " + e.getMessage());
            }
        }
        System.exit(0);
    }
}