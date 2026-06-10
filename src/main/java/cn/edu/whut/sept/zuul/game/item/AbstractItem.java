package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 物品抽象基类 — 所有物品的公共父类。
 *
 * <p>子类通过重写 {@link #takenBy(Player)}、{@link #droppedBy(Player)}、
 * {@link #usedBy(Player)} 实现各自的物品效果。</p>
 *
 * <p>具体子类（Issue #4，gmy）：
 *   Sword、DragonscaleBulwark、StormCleaver、
 *   MagicCookie、BloodVial、StonehideElixir</p>
 *
 * @author gmy
 * @since 1.0
 */
public abstract class AbstractItem {

    private final String name;
    private final String displayName;
    private final String description;
    private final int weight;

    // 攻击属性
    private int attackRange = 0;       // 0=非武器, 1=近战, 2+=远程
    private int attackCooldown = 0;    // 冷却毫秒
    private String attackType = "none"; // "melee","ranged","aoe","none"

    public AbstractItem(String name, String displayName, String description, int weight) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.weight = weight;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }

    public int getAttackRange() { return attackRange; }
    public void setAttackRange(int r) { this.attackRange = r; }
    public int getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(int cd) { this.attackCooldown = cd; }
    public String getAttackType() { return attackType; }
    public void setAttackType(String t) { this.attackType = t; }
    public boolean isWeapon() { return attackRange > 0; }

    /**
     * 被玩家拾取时的效果回调。
     */
    public abstract void takenBy(Player player);

    /**
     * 被玩家丢弃时的效果回调。
     */
    public abstract void droppedBy(Player player);

    /**
     * 被玩家使用时的效果回调。
     */
    public abstract void usedBy(Player player);
}
