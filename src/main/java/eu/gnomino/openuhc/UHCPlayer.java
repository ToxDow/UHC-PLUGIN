package eu.gnomino.openuhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Gnomino on 4/24/15.
 */
public class UHCPlayer {
    public UUID getUuid() {
        return uuid;
    }

    private UUID uuid;
    private String name;
    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    private boolean inGame;
    public UHCPlayer(Player p, boolean inGame) {
        this.uuid = p.getUniqueId();
        this.inGame = inGame;
        this.name = p.getName();
    }
    public Player getBukkitPlayer() {
        if(Bukkit.getOnlineMode())
            return Bukkit.getPlayer(this.uuid);
        else
            return Bukkit.getPlayer(this.name);
    }
}
