package eu.gnomino.openuhc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Created by Gnomino on 4/24/15.
 */
public class SurvivalListener implements Listener {
    private OpenUHC pl;
    public SurvivalListener(OpenUHC openUHC) {
        pl = openUHC;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        pl.getGame().died(e.getEntity());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
            if(pl.getGame().getStatus() != GameStatus.PLAYING) {
                e.setCancelled(true);
            }
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(pl.getGame().getStatus() != GameStatus.PLAYING) {
            e.setCancelled(true);
        }
    }
}
