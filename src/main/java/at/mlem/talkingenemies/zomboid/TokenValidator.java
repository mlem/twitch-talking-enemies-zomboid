package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class TokenValidator {

    public static ValidatorResult validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return new ValidatorResult();
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
            ok.set(response.statusCode() == 200);
            String fullString = response.body().replaceAll("\n", "");
            String loginValuePair = Arrays.stream(fullString.split(",")).filter(x -> x.contains("login")).findFirst().orElseThrow(() -> new IOException("Couldn't find login"));
            String rawValue = loginValuePair.split(":")[1].trim();
            String login = rawValue.substring(1, rawValue.length()-1).trim();
            ValidatorResult validatorResult = new ValidatorResult();
            validatorResult.isValid =ok.get();
            validatorResult.login = login;
            return validatorResult;
        } catch (IOException | InterruptedException e) {
            StormLogger.error("during token validation an error occured", e);
        }
        return new ValidatorResult();
    }

    public static class ValidatorResult {
        public boolean isValid;
        public String login;
    }

}
