package at.mlem.talkingenemies.zomboid;

import at.mlem.talkingenemies.zomboid.command.BotCommandHandlers;
import io.pzstorm.storm.logging.StormLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * to get a token, call https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=f1sjgf9y0ytdx2re1oapwfs7l11lh3&redirect_uri=http://localhost&scope=chat%3Aread+chat%3Aedit
 * <p>
 * After you press "Authorize" you can copy the access_token from the url
 * example of such an url:
 * http://localhost/#access_token=asdfljkasfdafds&scope=chat%3Aread+chat%3Aedit&token_type=bearer
 */
public class TwitchChatBotClient {

    private static HttpClient client;
    private static WebSocketListener listener;
    private static Timer timer;
    private static CompletableFuture<WebSocket> webSocketCompletableFuture;

    public static void shutdownClient() {
        client = null;
        if (listener != null) {
            listener.shutdown();
        }
        if(webSocketCompletableFuture != null) {
            try {
                WebSocket websocket = webSocketCompletableFuture.get();
                websocket.abort();
            } catch (InterruptedException | ExecutionException e) {
                StormLogger.error("failed to get websocket from future", e);
            }
            webSocketCompletableFuture.cancel(true);
            webSocketCompletableFuture = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public interface ChatListener {
        void onText(String user, String message, Color color);

        void start();

        void stop();

        void init(Map<String, TwitchChatter> twitchChatters);
    }

    public static class ChatListeners {
        private static List<ChatListener> chatListeners = new ArrayList<>();

        public static void register(ChatListener chatListener) {
            chatListeners.add(chatListener);
        }

        public static void init(Map<String, TwitchChatter> twitchChatters) {
            chatListeners.forEach(listener -> listener.init(twitchChatters));
        }

        public static void handleOnText(String nick, String message, Color color) {
            chatListeners.forEach(listener -> listener.onText(nick, message, color));

        }

        public static void start() {
            chatListeners.forEach(ChatListener::start);
        }

        public static void stop() {
            chatListeners.forEach(ChatListener::stop);
            chatListeners.clear();
        }
    }

    public static void listenToTwitchChat(ModProperties arguments, Map<String, TwitchChatter> twitchChatters) {
        if (client == null) {
            client = createClient();

            listener = new WebSocketListener(arguments, twitchChatters);
            ChatListeners.start();
            webSocketCompletableFuture = client.newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://irc-ws.chat.twitch.tv:80"),
                            listener
                    );


            timer = new Timer("check Twitchchat connectivity", true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    StormLogger.debug("Checking websocket connection to twitch.");
                    try {
                        WebSocket webSocket1 = webSocketCompletableFuture.get();

                        if (webSocket1 == null || webSocket1.isInputClosed()) {
                            StormLogger.warn("No Twitch chat connection. Retrying connection.");
                            listenToTwitchChat(arguments, twitchChatters);
                        }
                    } catch (Exception e) {
                        StormLogger.error("Couldn't get websocket from completable future.", e);
                        throw new RuntimeException(e);
                    }
                }
            }, 10000, 10000);
        }
    }

    private static HttpClient createClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    private static class WebSocketListener implements WebSocket.Listener {
        private final String channelName;
        private String oauthToken;
        private final String botName;
        private final boolean debug;
        private boolean shutdown;

        public WebSocketListener(ModProperties modProperties,
                                 Map<String, TwitchChatter> twitchChatters) {
            this.channelName = modProperties.getChannelName();
            this.botName = modProperties.getBotName();
            this.oauthToken = modProperties.getOauthToken();
            this.debug = modProperties.getDebug();
            BotCommandHandlers.init(twitchChatters);
            ChatListeners.init(twitchChatters);
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            if (shutdown) {
                webSocket.abort();
                return;
            }
            WebSocket.Listener.super.onOpen(webSocket);
            System.out.println("WebSocket Client Connected");
            sendText(webSocket, "CAP REQ :twitch.tv/membership twitch.tv/tags twitch.tv/commands");
            sendText(webSocket, "PASS oauth:" + oauthToken);
            sendText(webSocket, "NICK " + botName);
            sendText(webSocket, "JOIN #" + channelName);

        }


        private CompletableFuture<WebSocket> sendText(WebSocket webSocket, String s) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            CompletableFuture<WebSocket> webSocketCompletableFuture = webSocket.sendText(s, true);
            StormLogger.info("Sending " + s);
            return webSocketCompletableFuture;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {

            if (shutdown) {
                webSocket.abort();
                return null;
            }
            if (debug) {
                StormLogger.info(String.format("Received Binary over WebSocket: %s", data));
            }
            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            CompletionStage<?> completionStage = WebSocket.Listener.super.onText(webSocket, data, last);
            String[] messages = data.toString().split("\\r\\n");
            for (String receivedMessage : messages) {
                System.out.println(receivedMessage);
                try {
                    TwitchChatParser.Message message = TwitchChatParser.toMessage(receivedMessage);

                    if (message == null) {
                        return completionStage;
                    }
                    String command = message.command.getCommand();
                    if ("PRIVMSG".equals(command)) {
                        if (message.command.botCommand != null) {
                            BotCommandHandlers.handleBotCommand(message);
                            // here comes the botCommand handling
                        } else {
                            if (message.parameters != null) {
                                ChatListeners.handleOnText(message.source.nick, message.parameters, ColorParser.parseFromHex(message.tags.getColor()));
                            }
                        }
                    } else if ("PING".equals(command)) {
                        int indexOfLastColon = receivedMessage.lastIndexOf(":");
                        String responsePong = "PONG " + receivedMessage.substring(indexOfLastColon, receivedMessage.length() - 1);
                        webSocket.sendText(responsePong, true);
                        if (debug)
                            StormLogger.info("answering ping with: " + responsePong);
                    } else if ("NOTICE".equals(command)) {
                        StormLogger.warn(String.format("Received NOTICE with following text: %s", receivedMessage));
                    } else if ("PART".equals(command)) {
                        StormLogger.warn(String.format("Received PART (The channel must have banned (/ban) the bot) with following text: %s", receivedMessage));
                    } else if ("001".equals(command)) {
                        StormLogger.info(String.format("Received 001 which means successfully logged in: %s", receivedMessage));
                    }
                } catch (Exception e) {
                    StormLogger.error(String.format("Problem with the message \"%s\"", receivedMessage), e);
                }
            }
            return completionStage;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            CompletionStage<?> completionStage = WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            StormLogger.info(String.format("Closing WebSocket: %s ; StatusCode: %s", reason, statusCode));
            return completionStage;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (shutdown) {
                webSocket.abort();
                return;
            }
            StormLogger.error("error in chat", error);
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            StormLogger.info(String.format("Received Ping over WebSocket: %s", message));
            return WebSocket.Listener.super.onPing(webSocket, message);
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            StormLogger.info(String.format("Received Pong over WebSocket: %s", message));
            return WebSocket.Listener.super.onPong(webSocket, message);
        }

        public void shutdown() {
            shutdown = true;
        }

    }

}
