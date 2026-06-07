package cn.edu.whut.sept.zuul.game.message;

/**
 * 单人消息 — 仅发送给单个玩家的消息。
 *
 * @author lfk
 * @since 1.0
 */
public class SinglePlayerMessage implements IMessage {

    private final String content;

    public SinglePlayerMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getType() {
        return "single";
    }
}
