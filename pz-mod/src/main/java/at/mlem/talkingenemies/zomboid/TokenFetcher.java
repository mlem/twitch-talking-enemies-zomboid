package at.mlem.talkingenemies.zomboid;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.pzstorm.storm.logging.StormLogger;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TokenFetcher {

    public static void main(String[] args) throws IOException {
        AtomicReference<Boolean> isRunning = new AtomicReference<>();
        isRunning.set(true);
        ModProperties properties = new ModProperties();
        TwitchChatBotClient.ChatListener chatListener = new TwitchChatBotClient.ChatListener() {

            @Override
            public void onText(String user, String message, Color color) {
                System.out.println(user + ": " + message);
            }

            @Override
            public void start() {
                System.out.println("started");

            }

            @Override
            public void stop() {
                System.out.println("stopped");
                isRunning.set(false);
            }

            @Override
            public void init(Map<String, TwitchChatter> twitchChatters) {
                // ignore this method
            }
        };
        TwitchChatBotClient.listenToTwitchChat(properties, Map.of());
        while(isRunning.get()) {

        }
        TwitchChatBotClient.shutdownClient();

    }


    public static String fetchNewToken() {
        AtomicReference<URI> requestURIRef = new AtomicReference<>();
        HttpServer socket = null;
        try {
            socket = startAndHandleServer(requestURIRef);
            socket.start();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=f1sjgf9y0ytdx2re1oapwfs7l11lh3&redirect_uri=http://localhost&scope=chat%3Aread+chat%3Aedit"));
            }
            while (requestURIRef.get() == null) {
                // wait for a valid token
            }
            socket.stop(0);
            String token = requestURIRef.get().toASCIIString().split("&")[0].substring(14);
            return token;
        } catch (IOException e) {
            StormLogger.error("problems fetching a new token", e);
        }
        return null;
    }

    private static HttpServer startAndHandleServer(AtomicReference<URI> requestURIRef) throws IOException {
        return socket(exchange -> {
            System.out.println("HttpHandler invoked");
            URI requestURI = exchange.getRequestURI();
            if (requestURI.getPath() != null && !requestURI.getPath().isEmpty() && !requestURI.getPath().equals("/")) {
                requestURIRef.set(requestURI);
                System.out.println(requestURI.getPath());
            } else {
                System.out.println("sending html which redirects token here");
                URL htmlToShow = TwitchChatBotClient.class.getClassLoader().getResource("./sendUrlToBackend.html");
                writeMessageToResponse(exchange, htmlToShow.openStream().readAllBytes());
            }

        });
    }

    private static void writeMessageToResponse(HttpExchange exchange, byte[] msg) throws IOException {
        exchange.sendResponseHeaders(200, msg.length);
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(exchange.getResponseBody())) {
            bufferedOutputStream.write(msg);
        }
    }

    private static HttpServer socket(HttpHandler httpHandler) throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", 80);
        HttpServer localhost = HttpServer.create(address, 1);
        localhost.createContext("/", httpHandler);
        return localhost;
    }

}
