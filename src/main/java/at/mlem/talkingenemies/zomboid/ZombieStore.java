package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;
import zombie.characters.IsoZombie;
import zombie.characters.TalkingZombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static at.mlem.talkingenemies.zomboid.ModChatBotIntegration.RANDOM;

public class ZombieStore {

    private static ZombieStore instance;
    private Map<String, TalkingZombie> globalZombies;
    private List<TalkingZombie> assignableZombies;

    private ZombieStore() {
        globalZombies = new HashMap<>();
        assignableZombies = new ArrayList<>();
    }

    public static ZombieStore getInstance() {
        if (instance == null) {
            instance = new ZombieStore();
        }
        return instance;
    }


    public static ZombieStore resetStore() {
        instance = new ZombieStore();
        StormLogger.info("Resetting ZombieStore");
        return instance;
    }

    public boolean contains(String zombieUID) {
        return globalZombies.containsKey(zombieUID);
    }

    public TalkingZombie get(String zombieUID) {
        return globalZombies.get(zombieUID);
    }

    private TalkingZombie createNewTalkingZombie(IsoZombie zombie) {
        TalkingZombie newTalkingZombie = new TalkingZombie(zombie);
        newTalkingZombie.initTextObjects();
        return newTalkingZombie;
    }

    public TalkingZombie getOrCreate(String zombieUID, IsoZombie zombie) {
        return globalZombies.computeIfAbsent(zombieUID, s -> createNewTalkingZombie(zombie));
    }

    public void remove(String uid) {
        TalkingZombie removed = globalZombies.remove(uid);
        if (removed != null) {
            removed.forceUnassign();
        }
        assignableZombies.remove(removed);
    }

    public void addToAssignableZombies(TalkingZombie talkingZombie) {
        if (!assignableZombies.contains(talkingZombie) && !talkingZombie.isAssigned()) {
            StormLogger.info(String.format("Adding Zombie %s to assignable Zombies (Size: %d)",
                    talkingZombie.getZombieID(), assignableZombies.size()));
            assignableZombies.add(talkingZombie);

        }
    }

    public void removeFromAssignableZombies(TalkingZombie talkingZombie) {
        if (assignableZombies.contains(talkingZombie)) {
            StormLogger.info(String.format("Removing Zombie %s from assignable Zombies (Size: %d)",
                    talkingZombie.getZombieID(), assignableZombies.size()));
            assignableZombies.remove(talkingZombie);
            talkingZombie.forceUnassign();
        }

    }

    public void assignZombie(TwitchChatter twitchChatter) {
        if (!assignableZombies.isEmpty()) {
            TalkingZombie zombie = assignableZombies.get(RANDOM.nextInt(assignableZombies.size()));
            assignableZombies.remove(zombie);
            twitchChatter.assign(zombie);
            StormLogger.info(String.format("assigned user %s to zombie %s", twitchChatter.getName(), zombie.getZombieID()));
        }
    }
}
