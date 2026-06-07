package cn.edu.whut.sept.zuul.game.combat.event;

import cn.edu.whut.sept.zuul.game.Player;

public class DeathEvent {

    private final Player killer;
    private final Player victim;

    public DeathEvent(Player killer, Player victim) {
        this.killer = killer;
        this.victim = victim;
    }

    public Player getKiller() { return killer; }
    public Player getVictim() { return victim; }
}
