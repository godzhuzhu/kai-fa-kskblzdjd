package cn.edu.whut.sept.zuul.game.message;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 控制台消息桥接 — 输出到 System.out。
 *
 * @author lfk
 * @since 1.0
 */
public class ConsoleMessageBridge extends AbsMessageBridge {

    private String lastMessage = "";

    @Override
    public void send(IMessage message) {
        lastMessage = message.getContent();
        System.out.println(message.getContent());
    }

    @Override
    public void send(IMessage message, Player player) {
        lastMessage = message.getContent();
        System.out.println("[" + player.getPlayerName() + "] " + message.getContent());
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
