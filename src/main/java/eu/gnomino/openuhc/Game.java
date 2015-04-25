package eu.gnomino.openuhc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

/**
 * Created by Gnomino on 4/24/15.
 */
public class Game {
    public GameStatus getStatus() {
        return status;
    }

    private OpenUHC pl;
    private GameStatus status;
    private HashMap<UUID, UHCPlayer> players;
    private Random r;
    private Objective objective;
    private BukkitTask timerTask;
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    private Scoreboard scoreboard;

    public Game(OpenUHC openUHC) {
        status = GameStatus.WAITING;
        players = new HashMap<UUID, UHCPlayer>();
        pl = openUHC;
    }

    public void addPlayer(UHCPlayer uhcPlayer) throws GameStartedException {
        if (status == GameStatus.PLAYING || status == GameStatus.FINISHED) {
            throw new GameStartedException();
        }
        players.put(uhcPlayer.getUuid(), uhcPlayer);
        if (status == GameStatus.WAITING) {
            if (pl.getConfig().getBoolean("enable_auto_start") && players.size() >= pl.getConfig().getInt("auto_start")) {
                startCountdown();
            }
        }
    }

    private void startCountdown() throws GameStartedException {
        if (status != GameStatus.WAITING)
            throw new GameStartedException();
        status = GameStatus.COUNTDOWN;
        GameStartCountdownThread gameStartCountdownThread = new GameStartCountdownThread(pl.getConfig().getInt("countdown"), this, pl);
        gameStartCountdownThread.run();
    }

    public void start() throws GameStartedException {
        if (status == GameStatus.COUNTDOWN || status == GameStatus.WAITING) {
            status = GameStatus.PLAYING;
            broadcastMessage(pl._("generating_spawn_areas").replace("{TOTAL}", "" + playersNb()).replace("{DONE}", "0"));
        } else {
            throw new GameStartedException();
        }
        scoreboard = pl.getServer().getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("OpenUHC", "dummy");
        objective.getScore(pl._("scoreboard_players", false)).setScore(playersNb());
        objective.setDisplayName(pl._("scoreboard_initializing", false));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Objective health = scoreboard.registerNewObjective("lives", "health");
        health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        int wSize = pl.getConfig().getInt("world_size");
        int minX = 0 - (wSize / 2);
        int maxX = wSize / 2;
        int minZ = 0 - (wSize / 2);
        int maxZ = wSize / 2;
        int areasGenerated = 0;
        HashMap<Player, Location> spawnLocations = new HashMap<Player, Location>();
        for (Map.Entry<UUID, UHCPlayer> entry : players.entrySet()) {
            Player p = entry.getValue().getBukkitPlayer();
            p.setScoreboard(scoreboard);
            p.setGameMode(GameMode.SURVIVAL);
            p.setLevel(0);
            p.setExp(0);
            p.setScoreboard(getScoreboard());
            p.setHealth(p.getMaxHealth());
            Block b;
            double x, y, z;
            do {
                x = randomBetween(minX, maxX);
                z = randomBetween(minZ, maxZ);
                b = p.getWorld().getHighestBlockAt((int) x, (int) z);
                if (x < 0) {
                    x -= 0.5;
                } else {
                    x += 0.5;
                }
                if (z < 0) {
                    z -= 0.5;
                } else {
                    z += 0.5;
                }
                y = b.getY() + 3;
            }
            while (b.isLiquid());
            Chunk c = b.getChunk();
            int viewDistance = pl.getServer().getViewDistance();
            for (int cx = c.getX() - viewDistance; cx <= c.getX() + viewDistance; cx++) {
                for (int cz = c.getZ() - viewDistance; cz <= c.getZ() + viewDistance; cz++) {
                    c.getWorld().regenerateChunk(cx, cz);
                }
            }
            areasGenerated++;
            broadcastMessage(pl._("generating_spawn_areas").replace("{TOTAL}", "" + playersNb()).replace("{DONE}", "" + areasGenerated));
            spawnLocations.put(p, new Location(b.getWorld(), x, y, z));
        }
        broadcastMessage(pl._("game_start"));
        for (Map.Entry<Player, Location> ple : spawnLocations.entrySet()) {
            ple.getKey().teleport(ple.getValue());
        }
        timerTask = (new CounterThread(0, pl)).runTaskTimer(pl, 0, 20);
    }

    public void broadcastMessage(String message) {
        pl.getServer().broadcastMessage(message);
    }

    public int playersNb() {
        return players.size();
    }

    public void stopCountdown() {
        broadcastMessage(pl._("countdown_cancelled"));
        status = GameStatus.WAITING;
    }

    public int randomBetween(int min, int max) {
        if (r == null)
            r = new Random();
        return (int) ((r.nextDouble() * (max - min)) + min);
    }

    public boolean isJoinable() {
        return (status != GameStatus.PLAYING && status != GameStatus.FINISHED);
    }

    public void kill(UHCPlayer up) {
        Player p = up.getBukkitPlayer();
        World w = p.getLocation().getWorld();
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType() != Material.AIR)
                w.dropItemNaturally(p.getLocation(), is);
        }
        p.getInventory().clear();
    }

    public void removePlayer(Player p) {
        UHCPlayer up = players.get(p.getUniqueId());
        if (up.isInGame()) {
            kill(up);
            if(playersNb() > 2)
                broadcastMessage(pl._("left_in_game").replace("{PLAYER}", p.getDisplayName()).replace("{LEFT}", "" + (playersNb() - 1)));
        }
        players.remove(p.getUniqueId());
        objective.getScore("Players").setScore(playersNb());
        if (playersNb() == 1) {
            for (Map.Entry<UUID, UHCPlayer> lastPlayerEntry : players.entrySet()) {
                UHCPlayer lastPlayer = lastPlayerEntry.getValue();
                broadcastMessage(pl._("won").replace("{NAME}", lastPlayer.getBukkitPlayer().getDisplayName()));
                timerTask.cancel();
                status = GameStatus.FINISHED;
                int delayInSecondsBeforeShutdown = pl.getConfig().getInt("commands_at_end_delay");
                if(delayInSecondsBeforeShutdown >= 0) {
                    (new ShutdownTask(pl)).runTaskLater(pl, 20*delayInSecondsBeforeShutdown);
                }
                break;
            }
        }
    }

    public void died(Player p) {
        UHCPlayer up = players.get(p.getUniqueId());
        up.setInGame(false);
        if(playersNb() > 2)
            broadcastMessage(pl._("died").replace("{PLAYER}", p.getDisplayName()).replace("{LEFT}", "" + (playersNb() - 1)));
        removePlayer(p);
        p.sendMessage(pl._("spectator"));
        p.setGameMode(GameMode.SPECTATOR);
    }
}
