package eu.gnomino.openuhc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Gnomino on 4/24/15.
 */
public class OpenUHC extends JavaPlugin {
    private FileConfiguration conf;

    public Game getGame() {
        return game;
    }

    private Game game;
    private ConfigurationSection messages;
    private void setUpWorld(World w, int size) {
        Block hb = w.getHighestBlockAt(0, 0);
        if(hb.isLiquid()) {
            hb.setType(Material.GLASS);
        }
        w.setSpawnLocation(0, hb.getY()+1, 0);
        WorldBorder wb = w.getWorldBorder();
        wb.setCenter(0, 0);
        wb.setSize(size);
        wb.setDamageAmount(1);
        w.setGameRuleValue("naturalRegeneration", "false");
    }
    @Override
    public void onEnable() {
        saveDefaultConfig();
        conf = getConfig();
        getServer().getPluginManager().registerEvents(new NetworkListener(this), this);
        game = new Game(this);
        messages = conf.getConfigurationSection("messages");
        getServer().getPluginManager().registerEvents(new SurvivalListener(this), this);
        setUpWorld(getServer().getWorld(conf.getString("overworld")), conf.getInt("world_size"));
        setUpWorld(getServer().getWorld(conf.getString("nether")), (int) Math.floor(conf.getInt("world_size") / 8));
    }
    public String _(String message) {
        return _(message, true);
    }
    public String _(String message, boolean usePrefix) {
        if (usePrefix)
            return ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "[OpenUHC] ") + messages.getString(message, message));
        else
            return ChatColor.translateAlternateColorCodes('&', messages.getString(message, message));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("uhcstart")) {
            if(getGame().getStatus() == GameStatus.WAITING) {
                try {
                    getGame().start();
                } catch (GameStartedException e) {
                    e.printStackTrace();
                }
            }
            else {
                sender.sendMessage(org.bukkit.ChatColor.RED + "The game is starting or already started !");
            }
            return true;
        }
        return false;
    }
}
