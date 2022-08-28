package zombie.characters;

import at.mlem.talkingenemies.zomboid.TwitchChatter;
import io.pzstorm.storm.logging.StormLogger;
import zombie.core.textures.ColorInfo;
import zombie.ui.TextDrawObject;
import zombie.ui.UIFont;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class TalkingZombie {

    public static final int MAX_DISTANCE = 15;
    private IsoZombie zombie;

    private Queue<String> messages = new ArrayDeque<>();

    private TwitchChatter twitchChatter;
    private long lastSpoken = 0;

    public TalkingZombie(IsoZombie zombie) {

        this.zombie = zombie;
    }

    /**
     * Should be used only by TwitchChatter
     *
     * @param twitchChatter
     */
    public void assignTwitchUser(TwitchChatter twitchChatter) {
        if (zombie != null && IsoPlayer.getInstance() != null) {
            StormLogger.info(String.format("Assigning user %s to Zombie %s, isRendered(): %s, zombie.DistTo(IsoPlayer.getInstance()): %s",
                    twitchChatter.getName(), getZombieID(), isRendered(), zombie.DistTo(IsoPlayer.getInstance())));
        } else {
            StormLogger.info("Assigning twitch chatter " + twitchChatter.getName());
        }
           this.twitchChatter = twitchChatter;
        ColorInfo colorInfo = colorInfo();
        if (this.zombie.getName() != null) {
            zombie.userName.setDefaultColors(colorInfo.r, colorInfo.g, colorInfo.b, colorInfo.a);
        }
    }


    public void initTextObjects() {
        zombie.initTextObjects();
        zombie.chatElement.setMaxChatLines(5);

        if (this.zombie.getName() != null) {
            zombie.userName = new TextDrawObject();
            zombie.userName.setAllowAnyImage(true);
            zombie.userName.setDefaultFont(UIFont.Small);
        }

    }


    public void say(String message) {
        ColorInfo colorInfo = colorInfo();
        zombie.Say(message, colorInfo.r, colorInfo.g, colorInfo.b, null, 180f, "radio");
        zombie.setLastSpokenLine(message);
    }

    private ColorInfo colorInfo() {
        ColorInfo colorInfo = null;
        if (twitchChatter != null) {
            colorInfo = twitchChatter.getColorInfo();
        } else {
            Random random = new Random();
            colorInfo = new ColorInfo(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.9f);
        }
        return colorInfo;
    }

    public void sayTwitchChat() {
        if (twitchChatter != null) {
            twitchChatter.addMessagesFromBacklog();
            if (!messages.isEmpty() && lastSpoken < Instant.now().getEpochSecond()) {
                say(twitchChatter.getName() + ": " + messages.poll());
                lastSpoken = Instant.now().getEpochSecond();
            }
        }
    }


    public boolean isInRangeOfPlayer() {
        if (zombie != null && IsoPlayer.getInstance() != null) {
            return (isRendered()) && zombie.DistTo(IsoPlayer.getInstance()) < MAX_DISTANCE;
        }
        return false;
    }

    private boolean isRendered() {
        return (zombie.getAlpha() > 0.2f);
    }

    public void unassign() {
        if (twitchChatter != null) {
            if (zombie != null && IsoPlayer.getInstance() != null) {
                StormLogger.info(String.format("unassigning user %s from Zombie %s, isRendered(): %s, zombie.DistTo(IsoPlayer.getInstance()): %s",
                        twitchChatter.getName(), getZombieID(), isRendered(), zombie.DistTo(IsoPlayer.getInstance())));
            } else {
                StormLogger.info(String.format("unassigning user %s from Zombie %s", twitchChatter.getName(), getZombieID()));
            }
            // returning messages to backlog
            twitchChatter.unassign(messages);
            twitchChatter = null;
        }
        if (zombie != null) {
            this.zombie.setHighlighted(false);
            this.zombie.setOutlineHighlight(false);
        }
        messages = new ArrayDeque<>();
    }

    public boolean isAssigned() {
        return twitchChatter != null;
    }

    public void addMessage(String message) {
        if (twitchChatter != null) {
            StormLogger.info(String.format("Adding message to zombie(%s): %s: %s", getZombieID(), twitchChatter.getName(), message));
        } else {
            StormLogger.info(String.format("Adding message to zombie(%s): %s", getZombieID(), message));
        }
        messages.add(message);
    }

    public Integer getZombieID() {
        if (zombie != null) {
            return zombie.ZombieID;
        } else {
            return -1;
        }
    }

    public void clearChatter() {
        twitchChatter = null;
    }
}

