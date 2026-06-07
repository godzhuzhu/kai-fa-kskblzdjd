package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.game.Player;

public class QuitCommand extends Command
{
    public boolean execute(Game game, Player player)
    {
        if(hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        else {
            return true;  // signal that we want to quit
        }
    }
}
