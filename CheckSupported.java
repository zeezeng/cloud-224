import javax.net.ssl.*;
import java.util.Arrays;

public class CheckSupported {
    public static void main(String[] args) throws Exception {
        String before = java.security.Security.getProperty("jdk.tls.disabledAlgorithms");
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        String[] supported = ctx.getSocketFactory().getSupportedCipherSuites();
        String[] def = ctx.getSocketFactory().getDefaultCipherSuites();
        System.out.println("before='" + before + "'");
        System.out.println("supportedHasRsa=" + Arrays.asList(supported).contains("TLS_RSA_WITH_AES_256_GCM_SHA384"));
        System.out.println("defaultHasRsa=" + Arrays.asList(def).contains("TLS_RSA_WITH_AES_256_GCM_SHA384"));
        System.out.println("supported count=" + supported.length);
    }
}