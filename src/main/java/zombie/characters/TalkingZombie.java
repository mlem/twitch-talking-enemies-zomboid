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

    public static final int MAX_DISTANCE_UNTIL_UNASSIGN = 6;
    private IsoZombie zombie;

    private Queue<String> messages = new ArrayDeque<>();

    private TwitchChatter twitchChatter;

    public TalkingZombie(IsoZombie zombie) {

        this.zombie = zombie;
    }

    public void assignTwitchUser(TwitchChatter twitchChatter) {
        StormLogger.info("Assigning twitch chatter " + twitchChatter.getName());
        this.twitchChatter = twitchChatter;
        ColorInfo colorInfo = colorInfo();
        zombie.userName.setDefaultColors(colorInfo.r, colorInfo.g, colorInfo.b, colorInfo.a);
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
        zombie.Say(message, colorInfo.r, colorInfo.g, colorInfo.b, null, 180, "radio");
        zombie.setLastSpokenLine(message);
    }

    private ColorInfo colorInfo() {
        ColorInfo colorInfo = null;
        if(twitchChatter != null) {
            colorInfo = twitchChatter.getColorInfo();
        } else {
            Random random = new Random();
            colorInfo = new ColorInfo(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.9f);
        }
        return colorInfo;
    }

    public void sayTwitchChat() {
        if (!messages.isEmpty() && twitchChatter != null) {
            say(twitchChatter.getName() + ": " + messages.poll());
        }
    }


    public boolean unassign() {
        if (!isInRangeOfPlayer()) {
            forceUnassign();
            return true;
        } else {
            return false;
        }
    }

    public boolean isInRangeOfPlayer() {
        if (zombie != null && IsoPlayer.getInstance() != null) {
            return zombie.DistTo(IsoPlayer.getInstance()) < MAX_DISTANCE_UNTIL_UNASSIGN;
        }
        return false;
    }

    public void forceUnassign() {
        if (twitchChatter != null) {
            StormLogger.info("unassigning user " + twitchChatter.getName());
            twitchChatter.unassign();
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
        messages.add(message);
    }

    public Integer getZombieID() {
        if (zombie != null) {
            return zombie.ZombieID;
        } else {
            return -1;
        }
    }
}

