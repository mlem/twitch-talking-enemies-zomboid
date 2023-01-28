package at.mlem.talkingenemies.zomboid.command;

import at.mlem.talkingenemies.zomboid.TwitchChatParser;
import at.mlem.talkingenemies.zomboid.TwitchChatter;
import io.pzstorm.storm.logging.StormLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BotCommandHandlers {

    private static List<BotCommandHandler> botCommandHandlers = new ArrayList<>();

    public static void registerCommandHandler(BotCommandHandler commandHandler) {
        botCommandHandlers.add(commandHandler);
    }

    public static void init(Map<String, TwitchChatter> twitchChatters) {
        botCommandHandlers.forEach(handler -> handler.init(twitchChatters));
    }

    public static void handleBotCommand(TwitchChatParser.Message message) {
        StormLogger.info("received message \""+ message.command.botCommand + " "
                + message.command.botCommandParams + "\"");
        botCommandHandlers.stream()
                .filter(handler -> message.command.botCommand != null)
                .filter(handler -> message.command.botCommand.toLowerCase()
                        .startsWith(handler.commandPrefix().toLowerCase()))
                .forEach(handler -> handler.handle(message));

    }
}
