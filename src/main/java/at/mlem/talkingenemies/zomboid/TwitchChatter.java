package at.mlem.talkingenemies.zomboid;

import zombie.characters.TalkingZombie;
import zombie.core.textures.ColorInfo;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class TwitchChatter {

    private final ColorInfo colorInfo;
    private String name;
    private boolean debug;

    private Queue<String> messageBacklog = new ArrayDeque<>();

    private TalkingZombie assignedZombie;
    private Random random = new Random();

    public TwitchChatter(String name, boolean debug) {
        this.name = name;
        this.debug = debug;
        this.colorInfo = new ColorInfo(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                0.9f);
    }

    public String getName() {
        return name;
    }

    public void addMessage(String message) {
        if (assignedZombie != null) {
            assignedZombie.addMessage(message);
        } else {
            messageBacklog.add(message);
        }
    }

    public boolean hasZombie() {
        return assignedZombie != null;
    }

    public void assign(TalkingZombie zombie) {
        assignedZombie = zombie;
        if (debug) {
            assignedZombie.say("Assigning " + name);
        }
        while(!messageBacklog.isEmpty()) {
            assignedZombie.addMessage(messageBacklog.poll());
        }
        zombie.assignTwitchUser(this);
    }

    public void unassign() {
        if (assignedZombie != null) {
            if (debug) {
                assignedZombie.say("Unassigning " + name);
            }
            assignedZombie = null;
        }
    }

    public ColorInfo getColorInfo() {
        return colorInfo;
    }
}
