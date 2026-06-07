package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class BerserkerTotem extends AbstractItem implements IPlayerListener {

    private boolean active = false;
    private Player owner;
    private Thread revertThread;

    public BerserkerTotem() {
        super("BerserkerTotem", "A totem that fuels rage (+5 attack for 10s on hurt)", 5);
    }

    @Override
    public void takenBy(Player player) {
        this.owner = player;
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.removeListener(this);
        this.owner = null;
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
        if (!active && owner == player) {
            active = true;
            player.setAttack(player.getAttack() + 5);
            revertThread = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                if (active && player.getBag().contains(this)) {
                    player.setAttack(player.getAttack() - 5);
                    active = false;
                }
            });
            revertThread.start();
        }
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
