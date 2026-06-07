package cn.edu.whut.sept.zuul.game.message;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 控制台消息桥接 — 输出到 System.out。
 *
 * @author lfk
 * @since 1.0
 */
public class ConsoleMessageBridge extends AbsMessageBridge {

    @Override
    public void send(IMessage message) {
        System.out.println(message.getContent());
    }

    @Override
    public void send(IMessage message, Player player) {
        System.out.println("[" + player.getPlayerName() + "] " + message.getContent());
    }
}
