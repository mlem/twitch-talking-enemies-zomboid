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

    public static final ColorInfo ASSIGNABLE_COLOR = new ColorInfo(237 / 255f, 107 / 255f, 244 / 255f, 0.5f);
    public static final ColorInfo ASSIGNED_COLOR = new ColorInfo(191 / 255f, 148 / 255f, 1.0f, 0.5f);
    public static final int MAX_DISTANCE_UNTIL_UNASSIGN = 10;
    private IsoZombie zombie;

    private ColorInfo colorInfo;

    private Queue<String> messages = new ArrayDeque<>();

    private TwitchChatter twitchChatter;
    private long lastSeenSecond;

    public TalkingZombie(IsoZombie zombie) {

        this.zombie = zombie;
        Random random = new Random();
        this.colorInfo = new ColorInfo(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat());
        this.zombie.setHighlightColor(ASSIGNABLE_COLOR);
        this.zombie.setHighlighted(true);
        this.zombie.setOutlineHighlightCol(ASSIGNABLE_COLOR);
        this.zombie.setOutlineHighlight(true);
        this.zombie.setOutlineHlAttached(false);
    }

    public void assignTwitchUser(TwitchChatter twitchChatter) {
        System.out.println("Assigning twitch chatter " + twitchChatter.getName());
        this.twitchChatter = twitchChatter;
        this.zombie.setHighlightColor(ASSIGNED_COLOR);
        this.zombie.setHighlighted(true);
        this.zombie.setOutlineHighlightCol(ASSIGNED_COLOR);
        this.zombie.setOutlineHighlight(true);
        this.zombie.setOutlineHlAttached(false);
        this.lastSeenSecond = Instant.now().getEpochSecond();
    }


    public void initTextObjects() {
        zombie.initTextObjects();
        zombie.chatElement.setMaxChatLines(5);

        if (this.zombie.getName() != null) {
            zombie.userName = new TextDrawObject();
            zombie.userName.setAllowAnyImage(true);
            zombie.userName.setDefaultFont(UIFont.Small);
            zombie.userName.setDefaultColors(colorInfo.r, colorInfo.g, colorInfo.b, colorInfo.a);
        }

    }


    public void say(String message) {
        zombie.Say(message, colorInfo.r, colorInfo.g, colorInfo.b, null, 180, "radio");
    }

    public void sayTwitchChat() {
        if (!messages.isEmpty() && twitchChatter != null) {
            say(twitchChatter.getName() + ": " + messages.poll());
        }
    }


    public boolean unassign() {
        if(lastSeenSecond < Instant.now().getEpochSecond()-30 || !isInRangeOfPlayer()) {
            forceUnassign();
            return true;
        } else {
            return false;
        }
    }

    public boolean isInRangeOfPlayer() {
        return zombie.DistTo(IsoPlayer.getInstance()) < MAX_DISTANCE_UNTIL_UNASSIGN;
    }

    public void forceUnassign() {
        if (twitchChatter != null) {
            StormLogger.info("unassigning user " + twitchChatter.getName());
            twitchChatter.unassign();
            twitchChatter = null;
        }
        this.zombie.setHighlighted(false);
        this.zombie.setOutlineHighlight(false);
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

