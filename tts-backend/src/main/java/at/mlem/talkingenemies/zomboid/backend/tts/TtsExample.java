package at.mlem.talkingenemies.zomboid.backend.tts;
// Imports the Google Cloud client library

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Google Cloud TextToSpeech API sample application. Example usage: mvn package exec:java
 * -Dexec.mainClass='com.example.texttospeech.QuickstartSample'
 */
public class TtsExample {

    /** Demonstrates using the Text-to-Speech API. */
    public static void main(String... args) throws Exception {
        // Instantiates a client
        System.out.println("Starting ttsclient");
        long startTimeTts = System.currentTimeMillis();
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            long estimatedTimeTts = System.currentTimeMillis() - startTimeTts;
            System.out.println("Finished ttsclient init after " + estimatedTimeTts + " ms");
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText("Hallo, mlem! Wie geht's dir?")
                    .build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("de-DE")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.LINEAR16)
                            .setSpeakingRate(1.5)
                            .build();

            System.out.println("Starting request");
            long startTime = System.currentTimeMillis();
// ... do something ...
            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            long estimatedTime = System.currentTimeMillis() - startTime;
            System.out.println("Finished request after " + estimatedTime + " ms");
            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();


            // Write the response to the output file.
            try (OutputStream out = new FileOutputStream("output.wav")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \"output.wav\"");
            }

            playClip(audioContents.newInput());
        }
    }

    private static void playClip(InputStream clipFile) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        class AudioListener implements LineListener {
            private boolean done = false;
            @Override public synchronized void update(LineEvent event) {
                LineEvent.Type eventType = event.getType();
                if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }
            public synchronized void waitUntilDone() throws InterruptedException {
                while (!done) { wait(); }
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