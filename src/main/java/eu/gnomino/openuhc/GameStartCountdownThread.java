package eu.gnomino.openuhc;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Gnomino on 4/24/15.
 */
public class GameStartCountdownThread extends BukkitRunnable {
    private int time;
    private Game game;
    private OpenUHC pl;
    public GameStartCountdownThread(int secs, Game game, OpenUHC openUHC) {
        this.game = game;
        this.time = secs;
        this.pl = openUHC;
    }
    public void run() {
        if (game.playersNb() < pl.getConfig().getInt("auto-start")) {
            game.stopCountdown();
            return;
        }
        if (time == 0) {
            try {
                game.start();
            } catch (GameStartedException e) {
                e.printStackTrace();
            }
            return;
        }
        if (time % 5 == 0 || time < 5) {
            game.broadcastMessage(pl._("countdown").replace("{SECS}", "" + time));
        }
        GameStartCountdownThread newGameStartCountdownThread = new GameStartCountdownThread(time - 1, game, pl);
        newGameStartCountdownThread.runTaskLater(pl, 20);
    }
}
