package cn.edu.whut.sept.zuul.game;

import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 玩家类 — 管理玩家属性、背包、房间移动历史和战斗属性。
 *
 * <p>对外接口供 #5（GoCommand/BackCommand）、#6（Take/Drop/Use）、#7（传送）调用。</p>
 *
 * @author lfk
 * @since 1.0
 */
public class Player {

    private final int userId;
    private String playerName;
    private Room currentRoom;

    // 背包
    private final List<AbstractItem> bag;
    private int maxCapacity;

    // 移动历史
    private final Stack<Room> previousRooms;

    // 战斗属性（v1.0 预留，v2.0 #11 启用）
    private int attack;
    private int defense;
    private int currentHealth;
    private int maxHealth;

    // 在线状态（v2.0 #9 启用）
    private boolean isOnline;

    // 事件监听（v2.0 #11 启用）
    private final List<IPlayerListener> listeners;
    private long lastAttackTime;

    public Player(int userId, String playerName, Room startingRoom) {
        this.userId = userId;
        this.playerName = playerName;
        this.currentRoom = startingRoom;
        this.bag = new ArrayList<>();
        this.maxCapacity = 50;
        this.previousRooms = new Stack<>();
        this.attack = 10;
        this.defense = 5;
        this.currentHealth = 100;
        this.maxHealth = 100;
        this.isOnline = false;
        this.listeners = new ArrayList<>();
        this.lastAttackTime = 0;
    }

    // ========== 基础 ==========

    public int getUserId() {
        return userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    // ========== 背包 ==========

    public List<AbstractItem> getBag() {
        return new ArrayList<>(bag);
    }

    public int getCurrentLoad() {
        int total = 0;
        for (AbstractItem item : bag) {
            total += item.getWeight();
        }
        return total;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean canCarry(AbstractItem item) {
        return getCurrentLoad() + item.getWeight() <= maxCapacity;
    }

    // ========== 物品操作 ==========

    public boolean takeItem(AbstractItem item) {
        if (!canCarry(item)) {
            return false;
        }
        bag.add(item);
        item.takenBy(this);
        return true;
    }

    public boolean dropItem(AbstractItem item) {
        if (!bag.remove(item)) {
            return false;
        }
        item.droppedBy(this);
        return true;
    }

    public boolean useItem(AbstractItem item) {
        if (!bag.contains(item)) {
            return false;
        }
        item.usedBy(this);
        return true;
    }

    // ========== 移动 ==========

    public void moveTo(Room room) {
        previousRooms.push(this.currentRoom);
        this.currentRoom = room;
    }

    public Room goBack() {
        if (previousRooms.isEmpty()) {
            return null;
        }
        this.currentRoom = previousRooms.pop();
        return this.currentRoom;
    }

    public Stack<Room> getPreviousRooms() {
        return previousRooms;
    }

    // ========== 战斗属性 ==========

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void hurtBy(int damage) {
        this.currentHealth = Math.max(0, this.currentHealth - damage);
    }

    public boolean isDead() {
        return this.currentHealth <= 0;
    }

    // ========== 在线状态 ==========

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    // ========== 事件监听器 ==========

    public List<IPlayerListener> getListeners() {
        return listeners;
    }

    public void addListener(IPlayerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPlayerListener listener) {
        listeners.remove(listener);
    }

    public void notifyHurt(AttackEvent event) {
        for (IPlayerListener l : new ArrayList<>(listeners)) {
            l.onHurt(this, event);
        }
    }

    public void notifyDeath(DeathEvent event) {
        for (IPlayerListener l : new ArrayList<>(listeners)) {
            l.onDeath(this, event);
        }
    }

    public void notifyFightWin(FightWinEvent event) {
        for (IPlayerListener l : new ArrayList<>(listeners)) {
            l.onFightWin(this, event);
        }
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }
}
