package at.mlem.talkingenemies.zomboid.tts;

import at.mlem.talkingenemies.zomboid.TwitchChatParser;
import at.mlem.talkingenemies.zomboid.TwitchChatter;
import at.mlem.talkingenemies.zomboid.command.BotCommandHandler;
import io.pzstorm.storm.logging.StormLogger;
import zombie.ZomboidFileSystem;
import zombie.core.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class TTSBotCommandHandler implements BotCommandHandler {


    private Map<String, Voice> genderMap = new HashMap<>();
    private Map<String, TwitchChatter> chatterMap;

    @Override
    public String commandPrefix() {
        return "!tts";
    }

    private Voice determineVoice(String nick, String part) {
        Voice voice = Voice.findVoice(part);
        if(voice == Voice.UNDEFINED) {
            Voice previousVoice = genderMap.get(nick);
            if(previousVoice!=null) {
                return previousVoice;
            } else {
                genderMap.put(nick, Voice.GER_F_LOW);
            }
        } else {
            genderMap.put(nick, voice);
        }
        return genderMap.get(nick);
    }

    @Override
    public void init(Map<String, TwitchChatter> chatterMap) {
        this.chatterMap = chatterMap;
    }

    @Override
    public void handle(TwitchChatParser.Message message) {
        String nick = message.source.nick;
        StormLogger.info("handling message of " + nick);
        if(nick == null) {
            return;
        }
        if(message.command.botCommandParams == null) {
            return;
        }
        String[] textParts = message.command.botCommandParams.split(" ");
        if(textParts == null) {
            return;
        }
        String ttsText;
        if(Voice.isVoice(textParts[0])) {
            ttsText = Arrays.stream(textParts).skip(1).collect(Collectors.joining(" "));
        } else {
            ttsText = message.command.botCommandParams;
        }
        if(ttsText.isEmpty()) {
            return;
        }

        Voice voice = determineVoice(nick, textParts[0]);

        try {
            playTts(nick, voice, ttsText);
        } catch (Exception e) {
            StormLogger.error("Couldn't TTS text of " + nick + ". Text: " + ttsText, e);
        }

    }

    public void playTts(String nick, Voice voice, String text) throws Exception {
        // make backend call to tts-backend.jar
        // examples can be found in tts-test.http
        // GET http://localhost:7070/tts/mlem86?voice=USA_M_LOW&text=Brainz
        Color randomColor = Color.random();
        TwitchChatter twitchChatter = chatterMap.computeIfAbsent(nick, x -> new TwitchChatter(nick,
                new at.mlem.talkingenemies.zomboid.Color(randomColor.r, randomColor.g, randomColor.b)));

        StormLogger.info("creating future for long running request - once finished, it will invoke the necessary methods");
        FutureTask<File> futureTask = new FutureTask<>(() -> {
            String textHash = Integer.toHexString((voice+text).hashCode());
            Path path = Path.of("tts", "media", "sound", textHash + ".wav");
            File outputFile = path.toFile();
            String key = "media/sound/" + textHash + ".wav";
            String getUrl = "http://localhost:7070/tts/" + nick
                    + "?voice=" + voice.name()
                    + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

            StormLogger.info("Creating TTS for " + key);
            try(InputStream inputStream = URI.create(getUrl).toURL().openStream();) {
                ZomboidFileSystem.ensureFolderExists(outputFile.getParentFile());
                ZomboidFileSystem.instance.ActiveFileMap.put(key, outputFile.getPath());

                try(FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    inputStream.transferTo(fileOutputStream);
                    StormLogger.debug("Transfering sound to " + outputFile);
                } catch (Exception e) {
                    StormLogger.error("Failed to transfer sound to output stream " + outputFile, e);
                }
                twitchChatter.addNextSound(textHash);
                return outputFile;
            } catch (Exception e) {
                StormLogger.error("error while processing sound", e);
                return null;
            }
        });
        futureTask.run();
    }

}
