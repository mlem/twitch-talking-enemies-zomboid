package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

public class TokenValidator {

    public static boolean isTokenValid(String token) {
        if(token == null || token.isEmpty()) {
            return false;
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://id.twitch.tv/oauth2/validate"))
                .header("Authorization", "OAuth " + token)
                .GET().build();

        AtomicReference<Boolean> ok = new AtomicReference<>();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
            if (response.statusCode() != 200){
                ok.set(false);
            }
            else {
                ok.set(true);
            }
            return ok.get();
        } catch (IOException | InterruptedException e) {
            StormLogger.error("during token validation an error occured", e);
        }
        return false;
    }

}
