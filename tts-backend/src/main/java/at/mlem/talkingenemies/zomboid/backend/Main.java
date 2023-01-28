package at.mlem.talkingenemies.zomboid.backend;

import at.mlem.talkingenemies.zomboid.backend.tts.GoogleCloudTTSService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;

public class Main {
    public static void main(String[] args) {

        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .get("/tts/{nick}", ctx -> {
                    String nick = ctx.pathParam("nick");
                    String voice = ctx.queryParam("voice");
                    String text = ctx.queryParam("text");
                    ctx.contentType(ContentType.AUDIO_WAV);
                    ctx.result(GoogleCloudTTSService.generateTTS(nick, voice, text));
                })
                .start(7070);
    }
}