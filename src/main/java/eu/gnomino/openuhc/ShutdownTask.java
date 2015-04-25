package eu.gnomino.openuhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Gnomino on 4/25/15.
 */
public class ShutdownTask extends BukkitRunnable {
    private OpenUHC pl;
    public ShutdownTask(OpenUHC openUHC) {
        pl = openUHC;
    }
    public void run() {
        for(String cmd : pl.getConfig().getStringList("commands_at_end")) {
            pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), cmd);
        }
    }
}
