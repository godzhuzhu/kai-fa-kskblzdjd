package cn.edu.whut.sept.zuul.game.websocket.vo;

import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.List;

public class RoomVO {

    private String roomName;
    private String description;
    private List<BagItemVO> items;
    private List<String> exits;
    private List<RoomPlayerVO> players;
    private boolean portal;

    public static RoomVO from(Room r, List<RoomPlayerVO> players) {
        RoomVO vo = new RoomVO();
        vo.roomName = r.getShortDescription();
        vo.description = r.getShortDescription();
        vo.portal = r.isPortal();
        vo.items = new ArrayList<>();
        for (AbstractItem item : r.getItems()) {
            vo.items.add(new BagItemVO(item.getName(), item.getWeight()));
        }
        vo.exits = new ArrayList<>(r.getExits());
        vo.players = players;
        return vo;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<BagItemVO> getItems() { return items; }
    public void setItems(List<BagItemVO> items) { this.items = items; }
    public List<String> getExits() { return exits; }
    public void setExits(List<String> exits) { this.exits = exits; }
    public List<RoomPlayerVO> getPlayers() { return players; }
    public void setPlayers(List<RoomPlayerVO> players) { this.players = players; }
    public boolean isPortal() { return portal; }
    public void setPortal(boolean portal) { this.portal = portal; }
}
