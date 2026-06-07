package cn.edu.whut.sept.zuul.game.message;

import cn.edu.whut.sept.zuul.game.Player;
import org.springframework.stereotype.Component;

@Component
public class GameMessageBridge extends AbsMessageBridge {

    private ConsoleMessageBridge fallback;

    public GameMessageBridge() {
        this.fallback = new ConsoleMessageBridge();
    }

    public GameMessageBridge(ConsoleMessageBridge fallback) {
        this.fallback = fallback;
    }

    @Override
    public void send(IMessage message) {
        fallback.send(message);
    }

    @Override
    public void send(IMessage message, Player player) {
        fallback.send(message, player);
    }
}
