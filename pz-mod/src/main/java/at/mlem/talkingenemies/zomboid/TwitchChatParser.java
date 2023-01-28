package at.mlem.talkingenemies.zomboid;

import org.checkerframework.checker.units.qual.A;

import java.util.*;


/**
 * Copied over from https://dev.twitch.tv/docs/irc/example-parser and adapted to run in java
 */
public class TwitchChatParser {

    // Parses an IRC message and returns a JSON object with the message's
// component parts (tags, source (nick and host), command, parameters). 
// Expects the caller to pass a single message. (Remember, the Twitch 
// IRC server may send one or more IRC messages in a single message.)

    public static Message toMessage(String message) {
        return new TwitchChatParser().parseMessage(message);
    }

    public Message parseMessage(String message) {

        Message parsedMessage = new Message();

        // The start index. Increments as we parse the IRC message.

        int idx = 0;

        // The raw components of the IRC message.

        String rawTagsComponent = null;
        String rawSourceComponent = null;
        String rawCommandComponent = null;
        String rawParametersComponent = null;

        // If the message includes tags, get the tags component of the IRC message.

        if (message.substring(idx).startsWith("@")) {  // The message includes tags.
            int endIdx = message.indexOf(" ");
            rawTagsComponent = message.substring(1, endIdx);
            idx = endIdx + 1; // Should now point to source colon (:).
        }

        // Get the source component (nick and host) of the IRC message.
        // The idx should point to the source part; otherwise, it"s a PING command.

        if (message.substring(idx).startsWith(":")) {
            idx += 1;
            int endIdx = message.indexOf(" ", idx);
            rawSourceComponent = message.substring(idx, endIdx);
            idx = endIdx + 1;  // Should point to the command part of the message.
        }

        // Get the command component of the IRC message.

        int endIdx = message.indexOf(":", idx);  // Looking for the parameters part of the message.
        if (-1 == endIdx) {                      // But not all messages include the parameters part.
            endIdx = message.length();
        }

        rawCommandComponent = message.substring(idx, endIdx).trim();

        // Get the parameters component of the IRC message.

        if (endIdx != message.length()) {  // Check if the IRC message contains a parameters component.
            idx = endIdx + 1;            // Should point to the parameters part of the message.
            rawParametersComponent = message.substring(idx);
        }

        // Parse the command component of the IRC message.

        parsedMessage.command = parseCommand(rawCommandComponent);

        // Only parse the rest of the components if it"s a command
        // we care about; we ignore some messages.

        if (null == parsedMessage.command) {  // Is null if it"s a message we don"t care about.
            return null;
        } else {
            if (null != rawTagsComponent) {  // The IRC message contains tags.
                parsedMessage.tags = parseTags(rawTagsComponent);
            }

            parsedMessage.source = parseSource(rawSourceComponent);

            parsedMessage.parameters = rawParametersComponent;
            if (rawParametersComponent != null && rawParametersComponent.startsWith("!")) {
                // The user entered a bot command in the chat window.            
                parsedMessage.command = parseParameters(rawParametersComponent, parsedMessage.command);
            }
        }

        return parsedMessage;
    }

// Parses the tags component of the IRC message.

    Tags parseTags(String tags) {
        // badge-info=;badges=broadcaster/1;color=#0000FF;...

        List<String> tagsToIgnore = List.of("client-nonce", "flags");  // List of tags to ignore.

        Map dictParsedTags = new HashMap<>();  // Holds the parsed list of tags.
        // The key is the tag"s name (e.g., color).
        List<String> parsedTags = Arrays.asList(tags.split(";"));

        parsedTags.forEach(tag -> {
            String[] parsedTag = tag.split("=");  // Tags are key/value pairs.
            String tagValue = (parsedTag.length < 2 || parsedTag[1] == null || parsedTag[1].isEmpty()) ? null : parsedTag[1];

            switch (parsedTag[0]) {  // Switch on tag name
                case "badges":
                case "badge-info":
                    // badges=staff/1,broadcaster/1,turbo/1;

                    if (tagValue != null) {
                        Map<String, String> dict = new HashMap();  // Holds the list of badge objects.
                        // The key is the badge"s name (e.g., subscriber).
                        List<String> badges = Arrays.asList(tagValue.split(","));
                        badges.forEach(pair -> {
                            String[] badgeParts = pair.split("/");
                            dict.put(badgeParts[0], badgeParts[1]);
                        });
                        dictParsedTags.put(parsedTag[0], dict);
                    } else {
                        dictParsedTags.put(parsedTag[0], null);
                    }
                    break;
                case "emotes":
                    // emotes=25:0-4,12-16/1902:6-10

                    if (tagValue != null) {
                        Map<String, List<Position>> dictEmotes = new HashMap<>();  // Holds a list of emote objects.
                        // The key is the emote"s ID.
                        List<String> emotes = Arrays.asList(tagValue.split("/"));
                        emotes.forEach(emote -> {
                            String[] emoteParts = emote.split(":");

                            List<Position> textPositions = new ArrayList<>();  // The list of position objects that identify
                            // the location of the emote in the chat message.
                            List<String> positions = Arrays.asList(emoteParts[1].split(","));
                            positions.forEach(position -> {
                                String[] positionParts = position.split("-");
                                textPositions.add(new Position(positionParts[0], positionParts[1]));
                            });
                            dictEmotes.put(emoteParts[0], textPositions);
                        });

                        dictParsedTags.put(parsedTag[0], dictEmotes);
                    } else {
                        dictParsedTags.put(parsedTag[0], null);
                    }

                    break;
                case "emote-sets":
                    // emote-sets=0,33,50,237

                    String[] emoteSetIds = tagValue.split(",");  // Array of emote set IDs.
                    dictParsedTags.put(parsedTag[0], emoteSetIds);
                    break;
                default:
                    // If the tag is in the list of tags to ignore, ignore
                    // it; otherwise, add it.

                    if (tagsToIgnore.contains(parsedTag[0])) {
                        ;
                    } else {
                        dictParsedTags.put(parsedTag[0], tagValue);
                    }
            }
        });

        return new Tags(dictParsedTags);
    }

// Parses the command component of the IRC message.

    Command parseCommand(String rawCommandComponent) {
        Command parsedCommand = null;
        String[] commandParts = rawCommandComponent.split(" ");

        switch (commandParts[0]) {
            case "JOIN":
            case "PART":
            case "NOTICE":
            case "CLEARCHAT":
            case "HOSTTARGET":
            case "PRIVMSG":
                parsedCommand = new Command(commandParts[0], commandParts[1]);
                break;
            case "PING":
                parsedCommand = new Command(commandParts[0], null);
                break;
            case "CAP":
                parsedCommand = new Command(commandParts[0], null);
                parsedCommand.isCapRequestEnabled = ("ACK".equals(commandParts[2])) ? true : false;
                // The parameters part of the messages contains the
                // enabled capabilities.
                break;
            case "GLOBALUSERSTATE":  // Included only if you request the /commands capability.
                // But it has no meaning without also including the /tags capability.
                parsedCommand = new Command(commandParts[0], null);
                break;
            case "USERSTATE":   // Included only if you request the /commands capability.
            case "ROOMSTATE":   // But it has no meaning without also including the /tags capabilities.
                parsedCommand = new Command(commandParts[0], commandParts[1]);
                break;
            case "RECONNECT":
                System.out.println("The Twitch IRC server is about to terminate the connection for maintenance.");
                parsedCommand = new Command(commandParts[0], null);
                break;
            case "421":
                System.out.println("Unsupported IRC command: " + commandParts[2]);
                return null;
            case "001":  // Logged in (successfully authenticated). 
                parsedCommand = new Command(commandParts[0], commandParts[1]);
                break;
            case "002":  // Ignoring all other numeric messages.
            case "003":
            case "004":
            case "353":  // Tells you who else is in the chat room you"re joining.
            case "366":
            case "372":
            case "375":
            case "376":
                System.out.println("numeric message: " + commandParts[0]);
                return null;
            default:
                System.out.println("\nUnexpected command: " + commandParts[0] + "\n");
                return null;
        }

        return parsedCommand;
    }

// Parses the source (nick and host) components of the IRC message.

    private Source parseSource(String rawSourceComponent) {
        if (null == rawSourceComponent) {  // Not all messages contain a source
            return null;
        } else {
            String[] sourceParts = rawSourceComponent.split("!");
            return new Source((sourceParts.length == 2) ? sourceParts[0] : null,
                    (sourceParts.length == 2) ? sourceParts[1] : sourceParts[0]
            );
        }
    }

// Parsing the IRC parameters component if it contains a command (e.g., !dice).

    Command parseParameters(String rawParametersComponent, Command command) {
        int idx = 0;
        String commandParts = rawParametersComponent.substring(idx).trim();
        int paramsIdx = commandParts.indexOf(" ");

        if (-1 == paramsIdx) { // no parameters
            command.botCommand = commandParts;
        } else {
            command.botCommand = commandParts.substring(0, paramsIdx);
            command.botCommandParams = commandParts.substring(paramsIdx).trim();
            // TODO: remove extra spaces in parameters string
        }

        return command;
    }

    public static class Message {
        public Command command;
        public Tags tags;
        public Source source;
        public String parameters;
    }

    public class Command {
        public String botCommand;
        public String botCommandParams;
        public boolean isCapRequestEnabled;
        private String command;
        private String channel;

        public Command(String command, String channel) {
            this.command = command;
            this.channel = channel;
        }

        public String getCommand() {
            return command;
        }
    }

    public class Tags {

        //@badge-info=;
        // badges=;
        // client-nonce=abcdef0123456798;
        // color=#FF59AC;
        // display-name=mlem86;
        // emotes=;
        // first-msg=0;
        // flags=;
        // id=7efe2921-72fb-4f9c-b0e2-4775fc4d1a7e;
        // mod=0;
        // returning-chatter=0;
        // room-id=205373393;
        // subscriber=0;
        // tmi-sent-ts=1663180558417;
        // turbo=0;
        // user-id=1234;
        // user-type=

        public final static String COLOR = "color";
        private Map dictParsedTags;

        public Tags(Map dictParsedTags) {
            this.dictParsedTags = dictParsedTags;
        }

        public String getColor() {
            return (String) dictParsedTags.getOrDefault("color", "");
        }
    }

    public class Position {
        private final String startPosition;
        private final String endPosition;

        public Position(String startPosition, String endPosition) {

            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    public static class Source {
        public final String nick;
        final String host;

        public Source(String nick, String host) {

            this.nick = nick;
            this.host = host;
        }
    }
}
