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


    private ModChatBotIntegration modChatBotIntegration;

    public static boolean debug;


    @Override
    public void registerEventHandlers() {
        StormLogger.info(String.format("Registering %s as event handler", getClass().getName()));
        StormEventDispatcher.registerEventHandler(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> TwitchChatBotClient.shutdownClient()));
    }

    @SubscribeEvent
    public void handleGameStart(OnGameStartEvent gameStartEvent) {
        modChatBotIntegration = new ModChatBotIntegration();
        modChatBotIntegration.startTwitchChat();
    }


    @SubscribeEvent
    public void handleGameStop(OnMainScreenRenderEvent mainScreenRenderEvent) {
        if (modChatBotIntegration != null) {
            modChatBotIntegration.stopTwitchChat();
            modChatBotIntegration = null;
        }
    }

    @SubscribeEvent
    public void handleZombieUpdate(OnZombieUpdateEvent zombieUpdateEvent) {
        IsoZombie zombie = zombieUpdateEvent.zombie;
        if (modChatBotIntegration != null) {
            TalkingZombie talkingZombie = ZombieStore.getInstance().getOrCreate(zombie.getUID(), zombie);
            ZombieStore.getInstance().assignOrRemoveAssignableZombie(talkingZombie);
            talkingZombie.sayTwitchChat();
        }
    }
    @SubscribeEvent
    public void handleZombie(OnAIStateChangeEvent aiStateChange) {
        IsoGameCharacter character = aiStateChange.character;
        if (character != null
                && character instanceof IsoZombie
                && modChatBotIntegration != null) {
            zombie.characters.IsoZombie zombie = (IsoZombie) character;
            if(debug) {
                StormLogger.debug(String.format("OnAIStateChangeEvent received for Zombie ZID:%s UID:%s; prevState:%s newState:%s",
                        zombie.ZombieID, zombie.getUID(), aiStateChange.prevState != null ? aiStateChange.prevState.getName() : null, aiStateChange.newState.getName()));
            }

            TalkingZombie talkingZombie = ZombieStore.getInstance().getOrCreate(zombie.getUID(), zombie);

            ZombieStore.getInstance().assignOrRemoveAssignableZombie(talkingZombie);
        }

    }


    @SubscribeEvent
    public void handleZombieDeath(OnZombieDeadEvent zombieDeadEvent) {
        if (zombieDeadEvent.zombie != null) {
            ZombieStore.getInstance().remove(zombieDeadEvent.zombie.getUID());
        }

    }


}
