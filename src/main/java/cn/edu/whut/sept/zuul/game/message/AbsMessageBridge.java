package cn.edu.whut.sept.zuul.game.message;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 消息桥接抽象类 — 解耦游戏逻辑与消息输出方式。
 *
 * <p>v1.0 使用 ConsoleMessageBridge 控制台输出，
 * v2.0 将扩展 GameMessageBridge 实现 WebSocket 推送。</p>
 *
 * @author gmy
 * @since 1.0
 */
public abstract class AbsMessageBridge {

    public abstract void send(IMessage message);

    public abstract void send(IMessage message, Player player);
}
