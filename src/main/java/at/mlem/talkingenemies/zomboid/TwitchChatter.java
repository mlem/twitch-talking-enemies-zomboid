package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;
import zombie.characters.TalkingZombie;
import zombie.core.textures.ColorInfo;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchChatter {

    private final ColorInfo colorInfo;
    private String name;

    private Queue<String> messageBacklog = new ArrayDeque<>();

    private TalkingZombie assignedZombie;
    private Random random = new Random();

    public TwitchChatter(String name) {
        this.name = name;
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
        if (assignedZombie != null && assignedZombie.isInRangeOfPlayer()) {
            assignedZombie.addMessage(message);
             } else {
            messageBacklog.add(message);
            StormLogger.info(String.format("Adding message to backlog: %s: %s", name, message));

        }
    }

    public boolean hasZombie() {
        return assignedZombie != null && assignedZombie.isInRangeOfPlayer();
    }

    public void assign(TalkingZombie zombie) {
        if(assignedZombie != null) {
            assignedZombie.unassign();
            StormLogger.info("Re-assigning Zombie for " + name);
        }
        assignedZombie = zombie;
        StormLogger.info(String.format("assigned user %s to zombie %s", getName(), zombie.getZombieID()));
        addMessagesFromBacklog();
        zombie.assignTwitchUser(this);
    }

    public void addMessagesFromBacklog() {
        while(assignedZombie != null && !messageBacklog.isEmpty()) {
            assignedZombie.addMessage(messageBacklog.poll());
        }
    }

    public void unassign(Queue<String> messages) {
        if (assignedZombie != null) {
            messageBacklog = new ArrayDeque<>(Stream.concat(messages.stream(), messageBacklog.stream()).collect(Collectors.toList()));
            assignedZombie.clearChatter();
            assignedZombie = null;
        }
    }

    public ColorInfo getColorInfo() {
        return colorInfo;
    }

    public TalkingZombie getZombie() {
        return assignedZombie;
    }
}
