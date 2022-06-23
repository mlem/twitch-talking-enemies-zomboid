package at.mlem.talkingenemies.zomboid;

import io.pzstorm.storm.event.OnMainScreenRenderEvent;
import io.pzstorm.storm.event.StormEventDispatcher;
import io.pzstorm.storm.event.SubscribeEvent;
import io.pzstorm.storm.event.lua.OnAIStateChangeEvent;
import io.pzstorm.storm.event.lua.OnGameStartEvent;
import io.pzstorm.storm.event.lua.OnZombieDeadEvent;
import io.pzstorm.storm.event.lua.OnZombieUpdateEvent;
import io.pzstorm.storm.logging.StormLogger;
import io.pzstorm.storm.mod.ZomboidMod;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.characters.TalkingZombie;

import java.util.*;

public class Mod implements ZomboidMod {

    private static final List<String> IDLE_STATE_NAMES = List.of("ZombieIdleState", "ZombieEatBodyState");

    private Random random = new Random();

    private Map<String, TalkingZombie> zombies;
    private ModChatBotIntegration modChatBotIntegration;


    @Override
    public void registerEventHandlers() {
        StormLogger.info(String.format("Registering %s as event handler", getClass().getName()));
        StormEventDispatcher.registerEventHandler(this);
    }

    @SubscribeEvent
    public void handleGameStart(OnGameStartEvent gameStartEvent) {
        zombies = new HashMap<>();
        modChatBotIntegration = new ModChatBotIntegration();
        modChatBotIntegration.startTwitchChat();
    }


    @SubscribeEvent
    public void handleGameStop(OnMainScreenRenderEvent mainScreenRenderEvent) {
        zombies = new HashMap<>();
        if (modChatBotIntegration != null) {
            modChatBotIntegration.stopTwitchChat();
        }
    }

    @SubscribeEvent
    public void handleZombieUpdate(OnZombieUpdateEvent zombieUpdateEvent) {
        IsoZombie zombie = zombieUpdateEvent.zombie;
        if (zombies != null
                && modChatBotIntegration != null) {
        if (zombies.containsKey(zombie.getUID())) {
            TalkingZombie talkingZombie = zombies.get(zombie.getUID());
            talkingZombie.sayTwitchChat();
        }}
    }

    @SubscribeEvent
    public void handleZombie(OnAIStateChangeEvent aiStateChange) {
        IsoGameCharacter character = aiStateChange.character;
        if (character != null
                && character instanceof IsoZombie
                && zombies != null
                && modChatBotIntegration != null) {
            zombie.characters.IsoZombie zombie = (IsoZombie) character;
            StormLogger.debug(String.format("OnAIStateChangeEvent received for Zombie ZID:%s UID:%s; prevState:%s newState:%s",
                    zombie.ZombieID, zombie.getUID(), aiStateChange.prevState != null ? aiStateChange.prevState.getName() : null, aiStateChange.newState.getName()));

            TalkingZombie talkingZombie = zombies.computeIfAbsent(zombie.getUID(), s -> createNewTalkingZombie(zombie));

            if (!IDLE_STATE_NAMES.contains(aiStateChange.newState.getName())) {
                if (!talkingZombie.isAssigned()  && talkingZombie.isInRangeOfPlayer()) {
                    StormLogger.info(String.format("Adding Zombie %s to assignable Zombies",
                            zombie.ZombieID));
                    modChatBotIntegration.addToAssignableZombies(talkingZombie);
                }
            } else if (IDLE_STATE_NAMES.contains(aiStateChange.newState.getName())) {
                modChatBotIntegration.unassign(talkingZombie);
            }


        }

    }

    public TalkingZombie createNewTalkingZombie(IsoZombie zombie) {
        TalkingZombie newTalkingZombie = new TalkingZombie(zombie);
        newTalkingZombie.initTextObjects();
        return newTalkingZombie;
    }

    @SubscribeEvent
    public void handleZombieDeath(OnZombieDeadEvent zombieDeadEvent) {
        if (zombieDeadEvent.zombie != null) {
            TalkingZombie removedZombie = zombies.remove(zombieDeadEvent.zombie.getUID());

            removedZombie.forceUnassign();

        }

    }


}
