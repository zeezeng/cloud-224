import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class WsEcho {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        CompletableFuture<WebSocket> f = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .buildAsync(URI.create("wss://ws.postman-echo.com/raw"), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("OPENED");
                        webSocket.request(1);
                    }
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        System.out.println("TEXT: " + data);
                        webSocket.request(1);
                        return null;
                    }
                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int s, String r) {
                        System.out.println("CLOSE " + s + " " + r);
                        return null;
                    }
                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.out.println("ERROR: " + error);
                    }
                });
        try {
            WebSocket ws = f.get(8, TimeUnit.SECONDS);
            ws.sendText("hello", true);
            System.out.println("CONNECTED, sleeping...");
            Thread.sleep(3000);
            ws.abort();
        } catch (Exception e) {
            System.out.println("FAILED: " + e);
        }
        System.exit(0);
    }
}