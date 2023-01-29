package at.mlem.talkingenemies.zomboid;

import at.mlem.talkingenemies.zomboid.command.BotCommandHandlers;
import at.mlem.talkingenemies.zomboid.tts.TTSBotCommandHandler;
import io.pzstorm.storm.logging.StormLogger;

import java.util.*;

public class ModChatBotIntegration {

    public static final Random RANDOM = new Random();
    private TwitchChatBotClient.ChatListener chatListener;


    private List<String> blacklist;


    public void startTwitchChat(Map<String, TwitchChatter> twitchChatters) {
        ZombieStore.resetStore();

        ModProperties modProperties = new ModProperties();
        Mod.debug = modProperties.getDebug();
        blacklist = modProperties.getBlacklist();
        validateAndUpdateToken(modProperties);

        chatListener = new ModChatListener();
        TwitchChatBotClient.ChatListeners.register(chatListener);
        BotCommandHandlers.registerCommandHandler(new TTSBotCommandHandler());
        TwitchChatBotClient.listenToTwitchChat(
                modProperties,
                twitchChatters);
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
        ZombieStore.resetStore();
        TwitchChatBotClient.shutdownClient();
        TwitchChatBotClient.ChatListeners.stop();
    }


    private class ModChatListener implements TwitchChatBotClient.ChatListener {

        private boolean listenToMessages;
        private Map<String, TwitchChatter> twitchChatters;

        public ModChatListener() {
        }

        public void init(Map<String, TwitchChatter> twitchChatters) {
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