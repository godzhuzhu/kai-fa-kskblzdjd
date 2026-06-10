package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BerserkerTotem extends AbstractItem implements IPlayerListener {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
    private final AtomicBoolean active = new AtomicBoolean(false);
    private Player owner;

    public BerserkerTotem() {
        super("BerserkerTotem", "狂战士图腾", "激发怒火的图腾 (受伤后+8攻击持续12秒)", 5);
    }

    @Override
    public void takenBy(Player player) {
        this.owner = player;
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.removeListener(this);
        active.set(false);
        this.owner = null;
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
        if (!active.get() && owner == player) {
            active.set(true);
            player.setAttack(player.getAttack() + 8);
            SCHEDULER.schedule(() -> {
                if (active.get() && player.getBag().contains(this)) {
                    synchronized (player) {
                        if (active.compareAndSet(true, false)) {
                            player.setAttack(player.getAttack() - 8);
                        }
                    }
                }
            }, 12, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
