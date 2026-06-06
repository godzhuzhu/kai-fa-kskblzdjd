package cn.edu.whut.sept.zuul.game;

import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 房间类 — 表示游戏地图中的一个房间，支持物品存放、传送和多人游戏。
 *
 * <p>对外接口供 #5（GoCommand/BackCommand）、#6（Take/Drop）、#7（传送）调用。</p>
 *
 * @author lfk
 * @since 1.0
 */
public class Room {

    private final String name;
    private final String description;
    private boolean portal;
    private final HashMap<String, Room> exits;
    private final List<AbstractItem> items;

    /**
     * 创建一个房间。
     *
     * @param name        房间名称（唯一标识）
     * @param description 房间描述
     */
    public Room(String name, String description) {
        this.name = name;
        this.description = description;
        this.portal = false;
        this.exits = new HashMap<>();
        this.items = new ArrayList<>();
    }

    // ========== 基本信息 — #5 #6 调用 ==========

    /** @return 房间名称 */
    public String getName() {
        return name;
    }

    /** @return 房间简短描述 */
    public String getShortDescription() {
        return description;
    }

    /**
     * 获取房间完整描述，拼接名称、描述、物品列表和出口。
     *
     * <pre>
     * 物品列表格式：Items: Sword(8kg), BloodVial(2kg)
     * 出口格式：  Exits: east, west
     * </pre>
     *
     * @return 完整描述字符串
     */
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n").append(description);

        if (!items.isEmpty()) {
            sb.append("\nItems: ");
            List<String> itemDescs = new ArrayList<>();
            for (AbstractItem item : items) {
                itemDescs.add(item.getName() + "(" + item.getWeight() + "kg)");
            }
            sb.append(String.join(", ", itemDescs));
        }

        Set<String> exitSet = exits.keySet();
        if (!exitSet.isEmpty()) {
            sb.append("\nExits: ").append(String.join(", ", exitSet));
        }

        return sb.toString();
    }

    // ========== 出口 — #5 #7 调用 ==========

    /**
     * 设置房间出口方向。
     *
     * @param direction 方向（如 "east", "west"）
     * @param neighbor  相邻房间
     */
    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    /**
     * 获取指定方向的相邻房间。
     *
     * @param direction 方向名称
     * @return 相邻房间，无则返回 null
     */
    public Room getExit(String direction) {
        if (portal) {
            // 传送房间：忽略方向，随机返回一个出口
            List<Room> values = new ArrayList<>(exits.values());
            if (values.isEmpty()) {
                return null;
            }
            return values.get((int) (Math.random() * values.size()));
        }
        return exits.get(direction);
    }

    /** @return 所有出口方向集合 */
    public Set<String> getExits() {
        return exits.keySet();
    }

    /** @return 出口 Map（方向 → 房间）的副本 */
    public HashMap<String, Room> getExitMap() {
        return new HashMap<>(exits);
    }

    // ========== 物品 — #6 调用 ==========

    /** @return 房间内物品列表（不可变副本） */
    public List<AbstractItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * 按名称查找物品。
     *
     * @param name 物品名称
     * @return 物品对象，未找到返回 null
     */
    public AbstractItem getItem(String name) {
        for (AbstractItem item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 添加物品到房间。
     *
     * @param item 要添加的物品
     */
    public void addItem(AbstractItem item) {
        items.add(item);
    }

    /**
     * 按名称移除物品。
     *
     * @param name 物品名称
     * @return 移除的物品，未找到返回 null
     */
    public AbstractItem removeItem(String name) {
        AbstractItem item = getItem(name);
        if (item != null) {
            items.remove(item);
        }
        return item;
    }

    /**
     * 随机生成 0~2 件物品到房间。
     * <p>
     * 依赖 Issue #4 的 {@code Items.generateRandomItem()}。
     * 当 #4 合并后，取消内部注释即可生效。
     * </p>
     */
    public void addRandomItems() {
        int count = (int) (Math.random() * 3); // 0, 1, or 2
        for (int i = 0; i < count; i++) {
            // TODO: 当 #4 Items 系统合并后，取消下行注释
            // AbstractItem item = Items.generateRandomItem();
            // if (item != null) items.add(item);
        }
    }

    /** @return 房间内所有物品的总重量 */
    public int getItemsTotalWeight() {
        int total = 0;
        for (AbstractItem item : items) {
            total += item.getWeight();
        }
        return total;
    }

    // ========== 传送 — #7 调用 ==========

    /** @return 是否为传送房间 */
    public boolean isPortal() {
        return portal;
    }

    /** @param portal 设为传送房间 */
    public void setPortal(boolean portal) {
        this.portal = portal;
    }

    /**
     * 从所有房间中随机返回一个非自身、非传送房间的目标。
     *
     * @param allRooms 所有房间列表
     * @return 随机目标房间，无可选则返回 null
     */
    public Room getRandomExit(List<Room> allRooms) {
        List<Room> candidates = new ArrayList<>();
        for (Room r : allRooms) {
            if (r != this && !r.isPortal()) {
                candidates.add(r);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get((int) (Math.random() * candidates.size()));
    }
}
