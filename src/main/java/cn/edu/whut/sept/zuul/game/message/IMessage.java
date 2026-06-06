package cn.edu.whut.sept.zuul.game.message;

/**
 * 消息接口 — 定义消息类型和内容。
 *
 * <p>Issue #8 (gmy) 将提供完整的 AbsMessageBridge 实现体系。
 * 当前为 v1.0 占位实现，供 Command 使用。</p>
 *
 * @author lfk
 * @since 1.0
 */
public interface IMessage {

    /** @return 消息内容 */
    String getContent();

    /** @return 消息类型：global / room / single */
    String getType();
}
