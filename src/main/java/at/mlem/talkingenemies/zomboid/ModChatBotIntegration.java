package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;
import zombie.characters.TalkingZombie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ModChatBotIntegration {

    public static final Random RANDOM = new Random();
    private TwitchChatBotClient.ChatListener chatListener;

    private Map<String, TwitchChatter> twitchChatters = new HashMap<>();

    private List<TalkingZombie> assignableZombies = new ArrayList<>();

    private List<Integer> takenZombies = new ArrayList<>();
    private boolean debug;

    public void startTwitchChat(boolean debug) {
        this.debug = debug;

        Properties properties = loadProperties();
        twitchChatters = new HashMap<>();
        assignableZombies = new ArrayList<>();
        chatListener = new ModChatListener(twitchChatters);
        TwitchChatBotClient.Args arguments = new TwitchChatBotClient.Args(properties);
        Mod.debug = arguments.getDebug();
        TwitchChatBotClient.listenToTwitchChat(
                arguments,
                chatListener);
        chatListener.start();
    }

    private static Path getUserHomePath() {
        return Paths.get(System.getProperty("user.home"));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            File propertiesFile = Paths.get(getUserHomePath().toString(), "Zomboid", "mods", "twitch-talking-enemies", "app.properties").toFile();
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public void stopTwitchChat() {
        twitchChatters = new HashMap<>();
        assignableZombies = new ArrayList<>();
        TwitchChatBotClient.shutdownClient();
        if (chatListener != null) {
            chatListener.stop();
            chatListener = null;

        }
    }

    public void addToAssignableZombies(TalkingZombie talkingZombie) {
        if (!assignableZombies.contains(talkingZombie) && !takenZombies.contains(talkingZombie.getZombieID())) {
            StormLogger.info(String.format("Adding Zombie %s to assignable Zombies",
                    talkingZombie.getZombieID()));
            assignableZombies.add(talkingZombie);
            takenZombies.add(talkingZombie.getZombieID());

        }
    }

    public void removeFromAssignableZombies(TalkingZombie talkingZombie) {
        if(talkingZombie.unassign()) {
            assignableZombies.remove(talkingZombie);
            takenZombies.remove(talkingZombie.getZombieID());
        }
    }


    private class ModChatListener implements TwitchChatBotClient.ChatListener {

        private boolean listenToMessages;
        private Map<String, TwitchChatter> twitchChatters;

        public ModChatListener(Map<String, TwitchChatter> twitchChatters) {
            this.twitchChatters = twitchChatters;
        }


        @Override
        public void onText(String user, String message) {
            TwitchChatter twitchChatter = twitchChatters.computeIfAbsent(user, u -> new TwitchChatter(user, debug));

            if (listenToMessages) {
                if (!twitchChatter.hasZombie()) {
                    if (!assignableZombies.isEmpty()) {
                        TalkingZombie zombie = assignableZombies.get(RANDOM.nextInt(assignableZombies.size()));
                        takenZombies.add(zombie.getZombieID());
                        assignableZombies.remove(zombie);
                        twitchChatter.assign(zombie);
                        StormLogger.info("assigned user " + twitchChatter.getName() + " to zombie " + zombie);
                    }
                }
                if (twitchChatter.hasZombie()) {
                    twitchChatter.addMessage(message);
                    StormLogger.info("Adding message: " + user + ": " + message);
                }
            }
        }

        public void start() {
            StormLogger.info("Starting to listen to messages");
            listenToMessages = true;
        }

        public void stop() {
            StormLogger.info("Stopping to listen to messages");
            listenToMessages = false;

        }
    }
}