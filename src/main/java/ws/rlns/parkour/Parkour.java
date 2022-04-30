package ws.rlns.parkour;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import ws.rlns.parkour.arena.Arena;
import ws.rlns.parkour.arena.ArenaListener;
import ws.rlns.parkour.commands.ParkourCommand;
import ws.rlns.parkour.database.MySQL;
import ws.rlns.parkour.database.data.Leaderboard;
import ws.rlns.parkour.utils.Reference;

import java.sql.SQLException;
import java.util.logging.Level;

public class Parkour extends JavaPlugin {

    @Getter
    private Reference reference;
    @Getter
    private MySQL mySQL;
    @Getter
    private Arena arena;

    @Getter
    private Leaderboard leaderboard;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveConfig();
        reference = new Reference(this);
        mySQL = new MySQL(this);
        arena = new Arena(this);
        leaderboard = new Leaderboard(this);

        loadListeners();
        loadCommands();



        getServer().getScheduler().runTaskTimer(this, bukkitTask -> {
            leaderboard.getLeaderboard().clear();
            mySQL.getTopTimes(10);
        },0, 300*20L);
    }

    @Override
    public void onDisable() {

        arena.getArmorStands().forEach(Entity::remove);
        try {
            mySQL.disconnect();
            getLogger().log(Level.WARNING, "Successfully closed MySQL Connection");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "FAILED TO DISCONNECT FROM DATABASE!!!");
        }
    }

    private void loadListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
    }
    private void loadCommands() {
        getCommand("parkour").setExecutor(new ParkourCommand(this));

    }
}
