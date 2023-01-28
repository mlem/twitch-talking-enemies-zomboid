package at.mlem.talkingenemies.zomboid.tts;

import at.mlem.talkingenemies.zomboid.TwitchChatParser;
import at.mlem.talkingenemies.zomboid.TwitchChatter;
import at.mlem.talkingenemies.zomboid.command.BotCommandHandler;
import io.pzstorm.storm.logging.StormLogger;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TTSBotCommandHandler implements BotCommandHandler {


    private Map<String, Voice> genderMap = new HashMap<>();

    @Override
    public String commandPrefix() {
        return "!tts";
    }

    private Voice determineVoice(String nick, String botCommandParams) {
        Voice voice = findVoice(botCommandParams);
        genderMap.put(nick, voice);
        return genderMap.get(nick);
    }

    private Voice findVoice(String botCommandParams) {
        String[] paramTokens = botCommandParams.split(" ");
        if(paramTokens.length <= 1) {
            return Voice.GER_M_NORM;
        } else {
            try{
                return Voice.valueOf(paramTokens[0]);
            } catch (IllegalArgumentException e) {
                return Voice.GER_F_LOW;
            }
        }
    }

    @Override
    public void init(Map<String, TwitchChatter> chatterMap) {

    }

    @Override
    public void handle(TwitchChatParser.Message message) {
        String nick = message.source.nick;
        StormLogger.info("handling message of " + nick);
        if(nick == null) {
            return;
        }
        String[] textParts = message.command.botCommandParams.split(" ");
        if(textParts == null) {
            return;
        }
        String ttsText;
        if(textParts[0].equals("m") || textParts[0].equals("w")) {
            ttsText = Arrays.stream(textParts).skip(1).collect(Collectors.joining(" "));
        } else {
            ttsText = message.command.botCommandParams;
        }
        if(ttsText.isEmpty()) {
            return;
        }

        Voice voice = determineVoice(nick, textParts[0]);

        try {
            StormLogger.info("playing message of " + nick + ": " + ttsText);
            playTts(nick, voice, ttsText);
        } catch (Exception e) {
            StormLogger.error("Couldn't TTS text of " + nick + ". Text: " + ttsText, e);
        }

    }


    public static void playTts(String nick, Voice voice, String text) throws Exception {
        // make backend call to tts-backend.jar
        // examples can be found in tts-test.http
        // GET http://localhost:7070/tts/mlem86?voice=USA_M_LOW&text=Brainz

        String getUrl = "http://localhost:7070/tts/" + nick + "?voice=" + voice.name() + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        InputStream inputStream = URI.create(getUrl).toURL().openStream();
        playClip(inputStream);

    }

    private static void playClip(InputStream clipFile) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        class AudioListener implements LineListener {
            private boolean done = false;

            @Override
            public synchronized void update(LineEvent event) {
                LineEvent.Type eventType = event.getType();
                if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }

            public synchronized void waitUntilDone() throws InterruptedException {
                while (!done) {
                    wait();
                }
            }
        }
        AudioListener listener = new AudioListener();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(listener);
            clip.open(audioInputStream);
            try {
                clip.start();
                System.out.println("Starting playing clip");
                listener.waitUntilDone();
            } finally {
                System.out.println("Finished playing clip");
                clip.close();
            }
        } finally {
            audioInputStream.close();
        }
    }
}
