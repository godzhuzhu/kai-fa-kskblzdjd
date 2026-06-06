package cn.edu.whut.sept.zuul.game.message;

/**
 * 全局广播消息 — 所有玩家可见。
 *
 * @author lfk
 * @since 1.0
 */
public class GlobalMessage implements IMessage {

    private final String content;

    public GlobalMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getType() {
        return "global";
    }
}
