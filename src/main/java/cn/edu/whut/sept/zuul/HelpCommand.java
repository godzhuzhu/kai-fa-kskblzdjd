package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * @deprecated 已被 {@link cn.edu.whut.sept.zuul.game.command.HelpCommand} 替代。
 *             保留作为迁移参考。
 */
@Deprecated
public class HelpCommand extends Command
{
    private CommandWords commandWords;

    public HelpCommand(CommandWords words)
    {
        commandWords = words;
    }

    public boolean execute(Game game, Player player)
    {
        System.out.println("You are lost. You are alone. You wander");
        System.out.println("around at the university.");
        System.out.println();
        System.out.println("Your command words are:");
        commandWords.showAll();
        return false;
    }
}
