package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ModProperties {

    public static final String DEBUG_PROPERTY = "debug";
    public static final String BOT_NAME_PROPERTY = "botName";
    public static final String CHANNEL_NAME_PROPERTY = "channelName";
    public static final String OAUTH_TOKEN_PROPERTY = "oauthToken";
    public static final String BLACKLIST_PROPERTY = "blacklist";
    public static final String TTS_ACTIVE_PROPERTY = "tts.active";
    private Boolean debug;
    private String botName;
    private String channelName;
    private String oauthToken;

    private List<String> blacklist;

    private static Properties properties;
    private Boolean ttsActive = false;


    public ModProperties() {
        properties = loadProperties();
        if (properties == null) {
            properties = new Properties();
            this.debug = false;
            this.botName = "IIRC-Botname";
            this.channelName = "";
            this.oauthToken = "";
            this.blacklist = List.of();
            this.ttsActive = false;

        } else {
            this.debug = toBoolean(properties.getProperty(DEBUG_PROPERTY));
            this.botName = properties.getProperty(BOT_NAME_PROPERTY);
            this.channelName = channelName(properties.getProperty(CHANNEL_NAME_PROPERTY));
            this.oauthToken = properties.getProperty(OAUTH_TOKEN_PROPERTY);
            this.blacklist = Arrays.stream(properties.getProperty(BLACKLIST_PROPERTY).split(",")).toList();
            this.ttsActive = toBoolean(properties.getProperty(TTS_ACTIVE_PROPERTY));
        }
    }

    private static Boolean toBoolean(String debugString) {
        return Boolean.valueOf(debugString);
    }

    private static String channelName(String channelUrl) {
        String[] split = channelUrl.split("/");
        return split[split.length - 1];
    }

    private static Path getUserHomePath() {
        return Paths.get(System.getProperty("user.home"));
    }

    private static Properties loadProperties() {
        Properties properties = null;
        try {
            File propertiesFile = propertiesFile();
            if (propertiesFile.exists() && propertiesFile.canRead()) {
                properties = new Properties();
                properties.load(new FileInputStream(propertiesFile));
                StormLogger.info("loaded properties from " + propertiesFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;


    }

    private static File propertiesFile() {
        return Paths.get(getUserHomePath().toString(), "Zomboid", "twitch.properties").toFile();
    }

    public Boolean getDebug() {
        return debug;
    }


    public List<String> getBlacklist() {
        return blacklist;
    }

    public String getBotName() {
        return botName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public void saveProperties() {
        properties.setProperty(DEBUG_PROPERTY, Boolean.toString(debug));
        properties.setProperty(BOT_NAME_PROPERTY, botName);
        properties.setProperty(CHANNEL_NAME_PROPERTY, channelName);
        properties.setProperty(OAUTH_TOKEN_PROPERTY, oauthToken);
        properties.setProperty(BLACKLIST_PROPERTY, blacklist.stream().collect(Collectors.joining(",")));
        properties.setProperty(TTS_ACTIVE_PROPERTY, Boolean.toString(ttsActive));

        File propertiesFile = propertiesFile();
        try {
            properties.store(new FileOutputStream(propertiesFile), null);
            StormLogger.info("Saved properties to " + propertiesFile.getAbsolutePath());
        } catch (IOException e) {
            StormLogger.error("couldn't write properties back to " + propertiesFile.getAbsolutePath(), e);
        }
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isTtsActive() {
        return ttsActive;
    }

    public void setTtsActive(boolean ttsActive) {
        this.ttsActive = ttsActive;
    }
}
