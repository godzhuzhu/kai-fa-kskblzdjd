package cn.edu.whut.sept.zuul.game.message;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 控制台消息桥接 — v1.0 占位实现，输出到 System.out。
 *
 * <p>Issue #8 完成后，将由 AbsMessageBridge 体系替代。
 * 当前提供与最终接口一致的方法签名，确保 Command 代码无需后续改动。</p>
 *
 * @author lfk
 * @since 1.0
 */
public class ConsoleMessageBridge {

    /**
     * 发送全局消息（不绑定玩家）。
     *
     * @param message 消息对象
     */
    public void send(IMessage message) {
        System.out.println(message.getContent());
    }

    /**
     * 发送消息给指定玩家。
     *
     * @param message 消息对象
     * @param player  目标玩家
     */
    public void send(IMessage message, Player player) {
        System.out.println("[" + player.getPlayerName() + "] " + message.getContent());
    }
}
