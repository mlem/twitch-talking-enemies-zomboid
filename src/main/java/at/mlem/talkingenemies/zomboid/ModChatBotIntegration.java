package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;

import java.util.*;

public class ModChatBotIntegration {

    public static final Random RANDOM = new Random();
    private TwitchChatBotClient.ChatListener chatListener;

    private Map<String, TwitchChatter> twitchChatters = new HashMap<>();
    private List<String> blacklist;


    public void startTwitchChat() {
        twitchChatters = new HashMap<>();
        ZombieStore.resetStore();

        ModProperties modProperties = new ModProperties();
        Mod.debug = modProperties.getDebug();
        blacklist = modProperties.getBlacklist();
        validateAndUpdateToken(modProperties);

        chatListener = new ModChatListener(twitchChatters);
        TwitchChatBotClient.listenToTwitchChat(
                modProperties,
                chatListener);
        chatListener.start();
    }

    private void validateAndUpdateToken(ModProperties modProperties) {
        String oauthToken = modProperties.getOauthToken();
        TokenValidator.ValidatorResult result = TokenValidator.validateToken(oauthToken);
        if (!result.isValid) {
            obtainToken(modProperties);
        }
    }

    public static void obtainToken(ModProperties modProperties) {
        String oauthToken = TokenFetcher.fetchNewToken();
        TokenValidator.ValidatorResult result = TokenValidator.validateToken(oauthToken);
        modProperties.setOauthToken(oauthToken);
        if (result.login != null) {
            modProperties.setChannelName(result.login);
        } else {
            modProperties.setChannelName("");
        }
        modProperties.saveProperties();
        StormLogger.info("saving properties");
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
        public void onText(String user, String message, Color color) {
            if(blacklist.contains(user)) {
                return;
            }
            TwitchChatter twitchChatter = twitchChatters.computeIfAbsent(user, u -> new TwitchChatter(user, color));

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