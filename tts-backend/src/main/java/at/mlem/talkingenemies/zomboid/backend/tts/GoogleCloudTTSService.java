package at.mlem.talkingenemies.zomboid.backend.tts;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GoogleCloudTTSService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudTTSService.class);

    public static InputStream generateTTS(String nick, String voiceParam, String text) throws Exception {
        // Instantiates a client

        Voices voiceGender;
        if (voiceParam == null) {
            logger.info("no voice sent, setting gender to neutral");
            voiceGender = Voices.GER_M_NORM;
        } else {
            try {
                voiceGender = Voices.valueOf(voiceParam);
                logger.info("Found voice " + voiceGender.name());
            } catch (IllegalArgumentException e) {
                logger.info("Couldn't resolve " + voiceParam + ". Setting voice to GER_M_LOW");
                voiceGender = Voices.GER_M_LOW;
            }
        }
        if (text == null) {
            logger.warn("no text found, returning empty inputstream");
            return new ByteArrayInputStream(new byte[0]);
        }


        String resultFileName = voiceGender.name() + "_"
                + text.replaceAll("\\W+", "_")
                + ".wav";
        File finalFile = new File(nick, resultFileName);

        if (finalFile.exists()) {
            logger.info("found already a similar request, returning existing file " + finalFile.getName());
            return new FileInputStream(finalFile);
        } else {
            File nickDir = new File(nick);
            if (!nickDir.exists()) {
                nickDir.mkdir();
            }
        }

        logger.info("Starting ttsclient");
        long startTimeTts = System.currentTimeMillis();
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            long estimatedTimeTts = System.currentTimeMillis() - startTimeTts;
            logger.info("Finished ttsclient init after " + estimatedTimeTts + " ms");
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(voiceGender.getLanguageCode())
                            .setName(voiceGender.getName())
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.LINEAR16)
                            .setSpeakingRate(voiceGender.getSpeakingRate())
                            .setPitch(voiceGender.getPitch())
                            .setVolumeGainDb(-0.5)
                            .build();

            logger.info("Starting request");
            long startTime = System.currentTimeMillis();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            long estimatedTime = System.currentTimeMillis() - startTime;
            logger.info("Finished request after " + estimatedTime + " ms");
            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            try {
                return audioContents.newInput();
            } finally {
                // Write the response to the output file.
                try (OutputStream out = new FileOutputStream(finalFile)) {
                    out.write(audioContents.toByteArray());
                    logger.info("Audio content written to file \"" + resultFileName + "\"");
                }
            }

        }
    }

}
