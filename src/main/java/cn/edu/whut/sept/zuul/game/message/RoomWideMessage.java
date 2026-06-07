package cn.edu.whut.sept.zuul.game.message;

/**
 * 房间内广播消息 — 同一房间内所有玩家可见。
 *
 * @author lfk
 * @since 1.0
 */
public class RoomWideMessage implements IMessage {

    private final String content;

    public RoomWideMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getType() {
        return "room";
    }
}
