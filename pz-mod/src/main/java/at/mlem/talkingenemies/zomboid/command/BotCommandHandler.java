package at.mlem.talkingenemies.zomboid.command;

import at.mlem.talkingenemies.zomboid.TwitchChatParser;
import at.mlem.talkingenemies.zomboid.TwitchChatter;

import java.util.Map;

public interface BotCommandHandler {

    String commandPrefix();

    void init(Map<String, TwitchChatter> chatterMap);

    void handle(TwitchChatParser.Message message);
}
