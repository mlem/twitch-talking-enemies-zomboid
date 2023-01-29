package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.logging.StormLogger;
import zombie.characters.IsoZombie;
import zombie.characters.TalkingZombie;

import java.util.*;

import static at.mlem.talkingenemies.zomboid.ModChatBotIntegration.RANDOM;

public class ZombieStore {

    private static ZombieStore instance;
    private Map<String, TalkingZombie> globalZombies;
    private List<TalkingZombie> assignableZombies;

    private Queue<TwitchChatter> twitchChatterQueue;

    private ZombieStore() {
        globalZombies = new HashMap<>();
        assignableZombies = new ArrayList<>();
        twitchChatterQueue = new ArrayDeque<>();
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
            removed.unassign();
        }
        assignableZombies.remove(removed);
    }

    public void addToAssignableZombies(TalkingZombie talkingZombie) {
        if (!talkingZombie.isAssigned()) {
            if(!twitchChatterQueue.isEmpty()) {
                TwitchChatter twitchChatter = twitchChatterQueue.poll();
                if(!twitchChatter.hasZombie()) {
                    twitchChatter.assign(talkingZombie);
                }
            } else {
                if(!assignableZombies.contains(talkingZombie)) {
                    StormLogger.info(String.format("Adding Zombie %s to assignable Zombies (Size: %d)",
                            talkingZombie.getZombieID(), assignableZombies.size()));
                    assignableZombies.add(talkingZombie);
                }
            }
        }
    }

    public void removeFromAssignableZombies(TalkingZombie talkingZombie) {
        if (assignableZombies.contains(talkingZombie)) {
            StormLogger.info(String.format("Removing Zombie %s from assignable Zombies (Size: %d)",
                    talkingZombie.getZombieID(), assignableZombies.size()));
            assignableZombies.remove(talkingZombie);
            talkingZombie.unassign();
        }

    }

    public void assignZombie(TwitchChatter twitchChatter) {
        if (!assignableZombies.isEmpty()) {
            TalkingZombie zombie = assignableZombies.get(RANDOM.nextInt(assignableZombies.size()));
            assignableZombies.remove(zombie);
            if(!zombie.isAssigned()) {
                twitchChatter.assign(zombie);

            }
        } else {
            if(!twitchChatterQueue.contains(twitchChatter)) {
                twitchChatterQueue.add(twitchChatter);
                StormLogger.info(String.format("adding user %s to queue", twitchChatter.getName()));
            }

        }
    }

    public void addToChatterQueue(TwitchChatter twitchChatter) {
        if(twitchChatterQueue!= null && !twitchChatterQueue.contains(twitchChatter)) {
            twitchChatterQueue.add(twitchChatter);
        }
    }

    public void assignOrRemoveAssignableZombie(TalkingZombie talkingZombie) {
        if (talkingZombie.isInRangeOfPlayer()) {
            addToAssignableZombies(talkingZombie);
        } else {
            removeFromAssignableZombies(talkingZombie);
            talkingZombie.unassign();
        }
    }
}
