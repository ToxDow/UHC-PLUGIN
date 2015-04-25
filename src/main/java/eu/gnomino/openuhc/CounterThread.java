package eu.gnomino.openuhc;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

/**
 * Created by Gnomino on 4/25/15.
 */
public class CounterThread extends BukkitRunnable{
    private OpenUHC pl;
    private int secs;
    public CounterThread(int time, OpenUHC openUHC) {
        pl = openUHC;
        secs = time;
    }
    private String parseTime(int seconds) {
        String s = "";
        int hours = (int) Math.floor(seconds/3600);
        if(hours > 0) {
            s += hours + ":";
            seconds -= 3600*hours;
        }
        int minutes = (int) Math.floor(seconds/60);
        seconds -= 60*minutes;
        s += minutes + ":" + seconds;
        return s;
    }
    public void run() {
        Objective o = pl.getGame().getScoreboard().getObjective(DisplaySlot.SIDEBAR);
        o.setDisplayName(pl._("scoreboard_title", false).replace("{TIME}", parseTime(secs)));
        secs++;
    }
}
