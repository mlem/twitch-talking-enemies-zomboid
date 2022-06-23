package at.mlem.talkingenemies.zomboid;

import zombie.characters.TalkingZombie;

public class TwitchChatter {

    private String name;


    private TalkingZombie assignedZombie;

    public TwitchChatter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addMessage(String message) {
        if(assignedZombie != null) {
            assignedZombie.addMessage(message);
        }
    }

    public boolean hasZombie() {
        return assignedZombie != null;
    }

    public void assign(TalkingZombie zombie) {
        assignedZombie = zombie;
        zombie.assignTwitchUser(this);
    }

    public void unassign() {
        if(assignedZombie != null) {
            assignedZombie = null;
        }
    }
}
