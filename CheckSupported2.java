import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class CheckSupported2 {
    public static void main(String[] args) throws Exception {
        String before = java.security.Security.getProperty("jdk.tls.disabledAlgorithms");
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }}, new SecureRandom());
        String[] supported = ctx.getSocketFactory().getSupportedCipherSuites();
        System.out.println("before='" + before + "'");
        System.out.println("supportedHasRsa=" + Arrays.asList(supported).contains("TLS_RSA_WITH_AES_256_GCM_SHA384"));
        System.out.println("supported count=" + supported.length);
    }
}