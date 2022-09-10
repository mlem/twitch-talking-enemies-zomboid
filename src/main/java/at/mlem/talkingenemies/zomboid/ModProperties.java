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
    private Boolean debug;
    private String botName;
    private String channelName;
    private String oauthToken;

    private List<String> blacklist;

    private static Properties properties;

    {
        properties = loadProperties();
    }
    public ModProperties(Boolean debug, String botName, String channelName, String oauthToken, String blacklist) {
        this.debug = debug;
        this.botName = botName;
        this.channelName = channelName(channelName);
        this.oauthToken = oauthToken;
        this.blacklist = Arrays.stream(blacklist.split(",")).toList();
    }


    public ModProperties() {
        this(
                debug(properties.getProperty(DEBUG_PROPERTY)),
                properties.getProperty(BOT_NAME_PROPERTY),
                channelName(properties.getProperty(CHANNEL_NAME_PROPERTY)),
                properties.getProperty(OAUTH_TOKEN_PROPERTY),
                properties.getProperty(BLACKLIST_PROPERTY));
    }

    private static Boolean debug(String debugString) {
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
        Properties properties = new Properties();
        try {
            File propertiesFile = propertiesFile();
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;


    }

    private static File propertiesFile() {
        return Paths.get(getUserHomePath().toString(), "Zomboid", "mods", "twitch-talking-enemies", "app.properties").toFile();
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

        try {
            properties.store(new FileOutputStream(propertiesFile()), null);
        } catch (IOException e) {
            StormLogger.error("couldn't write properties back to " + propertiesFile().getAbsolutePath(), e);
        }
    }
}
