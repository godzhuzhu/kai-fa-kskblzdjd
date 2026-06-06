package cn.edu.whut.sept.zuul.game;

import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 房间类 — 表示游戏地图中的一个房间。
 *
 * <p>完整实现在 Issue #3。本文件为 Player（Issue #2）提供最小编译桩。</p>
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

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
        this.portal = false;
        this.exits = new HashMap<>();
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return description;
    }

    /**
     * 获取房间完整描述。
     * 物品列表格式：Items: Sword(8kg), BloodVial(2kg)
     * 出口格式：Exits: east, west
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

    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public Room getExit(String direction) {
        return exits.get(direction);
    }

    public Set<String> getExits() {
        return exits.keySet();
    }

    public HashMap<String, Room> getExitMap() {
        return new HashMap<>(exits);
    }

    public List<AbstractItem> getItems() {
        return new ArrayList<>(items);
    }

    public AbstractItem getItem(String name) {
        for (AbstractItem item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public void addItem(AbstractItem item) {
        items.add(item);
    }

    public AbstractItem removeItem(String name) {
        AbstractItem item = getItem(name);
        if (item != null) {
            items.remove(item);
        }
        return item;
    }

    public void addRandomItems() {
        // TODO: Issue #4
    }

    public int getItemsTotalWeight() {
        int total = 0;
        for (AbstractItem item : items) {
            total += item.getWeight();
        }
        return total;
    }

    public boolean isPortal() {
        return portal;
    }

    public void setPortal(boolean portal) {
        this.portal = portal;
    }

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
