package at.mlem.talkingenemies.zomboid;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.joining;

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

    public static void shutdownClient() {
        client = null;
        if (listener != null) {
            listener.shutdown();
        }
    }

    public interface ChatListener {
        void onText(String user, String message);

        void start();

        void stop();
    }

    public static void listenToTwitchChat(Args arguments, ChatListener chatListener) {
        if (client == null) {
            client = createClient();

            listener = new WebSocketListener(arguments, chatListener);
            client.newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://irc-ws.chat.twitch.tv:80"),
                            listener
                    ).join();
        }
    }

    private static HttpClient createClient() {
            //SSLContext instance = getSslContext();

            return HttpClient.newBuilder()
                   // .sslContext(instance)
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
    }

    private static class WebSocketListener implements WebSocket.Listener {
        private final String channelName;
        private final String oauthToken;
        private final String botName;
        private final boolean debug;
        private ChatListener chatListener;
        private boolean shutdown;

        public WebSocketListener(Args arguments, ChatListener chatListener) {
            this.channelName = arguments.channelName;
            this.botName = arguments.botName;
            this.oauthToken = arguments.oauthToken;
            this.debug = arguments.debug;
            this.chatListener = chatListener;
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
            System.out.println("Sending " + s);
            return webSocketCompletableFuture;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {

            if (shutdown) {
                webSocket.abort();
                return null;
            }
            if (debug) {
                System.out.println(String.format("Received Binary over WebSocket: %s", data));
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
            String receivedMessage = data.toString();
            if (receivedMessage.contains("PRIVMSG")) {
                PrivMsg privMsg = new PrivMsg(receivedMessage);
                if(privMsg.message != null) {
                    chatListener.onText(privMsg.displayName, privMsg.message.messageString);
                }
            } else if (receivedMessage.contains("PING")) {
                int indexOfLastDoppelpunkt = receivedMessage.lastIndexOf(":");
                String responsePong = "PONG " + receivedMessage.substring(indexOfLastDoppelpunkt, receivedMessage.length() - 1);
                webSocket.sendText(responsePong, true);
                if (debug)
                    System.out.println("answering ping with: " + responsePong);
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
            System.out.println(String.format("Closing WebSocket: %s ; StatusCode: %s", reason, statusCode));
            return completionStage;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (shutdown) {
                webSocket.abort();
                return;
            }
            error.printStackTrace();
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            System.out.println(String.format("Received Ping over WebSocket: %s", message));
            return WebSocket.Listener.super.onPing(webSocket, message);
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            if (shutdown) {
                webSocket.abort();
                return null;
            }
            System.out.println(String.format("Received Pong over WebSocket: %s", message));
            return WebSocket.Listener.super.onPong(webSocket, message);
        }

        public void shutdown() {
            shutdown = true;
        }

        private class PrivMsg {
            private Message message;
            private String userType;
            private String userId;
            private String turbo;
            private String sentTimestamp;
            private String subscriber;
            private String roomId;
            private String mod;
            private String id;
            private String flags;
            private String firstMsg;
            private String emotes;
            private String displayName;
            private String color;
            private String clientNonce;
            private String badges;
            private String badgeInfo;

            public PrivMsg(String receivedMessage) {
                String[] split = receivedMessage.split(";");
                Arrays.stream(split).forEach(part -> {
                    if (part.startsWith("@badge-info")) {
                        badgeInfo = extractValue(part);
                    } else if (part.startsWith("badges")) {
                        badges = extractValue(part);

                    } else if (part.startsWith("client-nonce")) {
                        clientNonce = extractValue(part);

                    } else if (part.startsWith("color")) {
                        color = extractValue(part);

                    } else if (part.startsWith("display-name")) {
                        displayName = extractValue(part);

                    } else if (part.startsWith("emotes")) {
                        emotes = extractValue(part);

                    } else if (part.startsWith("first-msg")) {
                        firstMsg = extractValue(part);

                    } else if (part.startsWith("flags")) {
                        flags = extractValue(part);

                    } else if (part.startsWith("id")) {
                        id = extractValue(part);

                    } else if (part.startsWith("mod")) {
                        mod = extractValue(part);

                    } else if (part.startsWith("room-id")) {
                        roomId = extractValue(part);

                    } else if (part.startsWith("subscriber")) {
                        subscriber = extractValue(part);

                    } else if (part.startsWith("tmi-sent-ts")) {
                        sentTimestamp = extractValue(part);

                    } else if (part.startsWith("turbo")) {
                        turbo = extractValue(part);

                    } else if (part.startsWith("user-id")) {
                        userId = extractValue(part);

                    } else if (part.startsWith("user-type")) {
                        userType = extractValue(part);
                        message = new Message(userType);

                    }
                });
            }

            private String extractValue(String part) {
                String[] split = part.split("=");
                if (split.length > 1) {
                    return split[1];
                } else {
                    return null;
                }
            }

            private class Message {
                private final String messageString;

                public Message(String userType) {
                    String interestingPart = userType.substring(userType.indexOf("PRIVMSG"), userType.length() - 1);
                    messageString = interestingPart.substring(interestingPart.indexOf(":") + 1, interestingPart.length() - 1);
                }
            }
        }
    }

    public static class Args {

        private Boolean debug;
        private String botName;
        private String channelName;
        private String oauthToken;

        public Args(Boolean debug, String botName, String channelName, String oauthToken) {
            this.debug = debug;
            this.botName = botName;
            this.channelName = channelName(channelName);
            this.oauthToken = oauthToken;
        }

        public Args(String[] args) {
            System.out.println(
                    String.format(
                            "Command line args passed: %s",
                            Arrays.stream(args).collect(joining(" ; "))
                    )
            );
            if (args.length >= 3) {
                channelName = channelName(args[0]);
                oauthToken = args[1];
                botName = args[2];
                if (args.length >= 4) {
                    debug = debug(args[3]);
                } else {
                    debug = Boolean.FALSE;
                }
            }
        }

        private static Boolean debug(String debugString) {
            return Boolean.valueOf(debugString);
        }

        private static String channelName(String channelUrl) {
            String[] split = channelUrl.split("/");
            return split[split.length - 1];
        }

        public Args(Properties properties) {
            this(
                    debug(properties.getProperty("debug")),
                    properties.getProperty("botName"),
                    channelName(properties.getProperty("channelName")),
                    properties.getProperty("oauthToken"));
        }
    }
}
