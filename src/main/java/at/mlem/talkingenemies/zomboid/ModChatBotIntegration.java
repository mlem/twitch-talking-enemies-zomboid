package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;

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
    private List<String> blacklist;


    public void startTwitchChat() {
        twitchChatters = new HashMap<>();
        ZombieStore.resetStore();
        chatListener = new ModChatListener(twitchChatters);
        ModProperties arguments = new ModProperties();
        Mod.debug = arguments.getDebug();
        blacklist = arguments.getBlacklist();
        TwitchChatBotClient.listenToTwitchChat(
                arguments,
                chatListener);
        chatListener.start();
    }


    public void stopTwitchChat() {
        twitchChatters = new HashMap<>();
        ZombieStore.resetStore();
        TwitchChatBotClient.shutdownClient();
        if (chatListener != null) {
            chatListener.stop();
            chatListener = null;

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
            if(blacklist.contains(user)) {
                return;
            }
            TwitchChatter twitchChatter = twitchChatters.computeIfAbsent(user, u -> new TwitchChatter(user));

            if (listenToMessages) {
                if (!twitchChatter.hasZombie()) {
                    ZombieStore.getInstance().assignZombie(twitchChatter);
                }
                twitchChatter.addMessage(message);
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