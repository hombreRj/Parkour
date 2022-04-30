package ws.rlns.parkour.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ws.rlns.parkour.Parkour;
import ws.rlns.parkour.database.MySQL;
import ws.rlns.parkour.utils.Reference;
import ws.rlns.parkour.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ParkourCommand implements CommandExecutor {


    private final MySQL sql;
    private final Parkour parkour;
    private final Reference ref;
    private List<String> locationList;
    private List<String> particleList;
    private int taskID;

    public ParkourCommand(Parkour parkour) {
        this.parkour = parkour;
        this.sql = parkour.getMySQL();
        ref = parkour.getReference();
        locationList = new ArrayList<>();
        particleList = new ArrayList<String>();
        taskID = 0;

    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (!(args.length > 0)) {
                ref.getParkourHelp().forEach(msg -> {
                    player.sendMessage(Utils.format(msg));
                });
            } else if (args[0].equalsIgnoreCase("top")) {
                player.sendMessage(Utils.format(ref.getLeaderboardTop()));
                AtomicInteger i = new AtomicInteger(1);
                parkour.getLeaderboard().getLeaderboard().forEach((uuid, s1) -> {
                    getOfflinePlayer(uuid, offlinePlayer -> {
                        player.sendMessage(Utils.format(ref.getLeaderboardFormat())
                                .replaceAll("%number%", i.get() + "")
                                .replaceAll("%name%", offlinePlayer.getName())
                                .replaceAll("%time%", s1));
                    });


                    i.getAndIncrement();
                });
                player.sendMessage(Utils.format(ref.getLeaderboardBottom()));
            } else if (args[0].equalsIgnoreCase("best")) {
                if (sql.getTime(player.getUniqueId()).isEmpty()) {
                    player.sendMessage(Utils.format(ref.getNoPB()));
                } else {
                    player.sendMessage(Utils.format(ref.getPB()).replaceAll("%time%", sql.getTime(player.getUniqueId()).get()));
                }
            } else if (args[0].equalsIgnoreCase("setstart")) {
                if (player.hasPermission("parkour.updatepositions")) {
                    parkour.getConfig().set("Arena.startPosition", Utils.getSerializedLocation(player.getLocation()));
                    parkour.saveConfig();
                    player.sendMessage(Utils.format(ref.getSetMessage()));
                }

            } else if (args[0].equalsIgnoreCase("setend")) {
                if (player.hasPermission("parkour.updatepositions")) {
                    parkour.getConfig().set("Arena.endPosition", Utils.getSerializedLocation(player.getLocation()));
                    parkour.saveConfig();
                    player.sendMessage(Utils.format(ref.getSetMessage()));
                }
            } else if (args[0].equalsIgnoreCase("addcheckpoint")) {
                if (player.hasPermission("parkour.updatepositions")) {
                    locationList.add(Utils.getSerializedLocation(player.getLocation()));
                    player.sendMessage(Utils.format(ref.getAddCheckPoint()));
                }
            } else if (args[0].equalsIgnoreCase("finish")) {
                if (player.hasPermission("parkour.updatepositions")) {
                    parkour.getConfig().set("Arena.checkPoints",locationList);
                    parkour.saveConfig();
                    player.sendMessage(Utils.format(ref.getFinishedSetup()));

                }
            }else if (args[0].equalsIgnoreCase("setupparticles")){
                particleList.clear();
                player.sendMessage("Starting particle trial following. Once you finish the path. Run /parkour endparticles");

               taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(parkour, () -> {
                    particleList.add(Utils.getSerializedLocation(player.getLocation()));
                }, 20l, 20);
            } else if (args[0].equalsIgnoreCase("endparticles")) {
                Bukkit.getServer().getScheduler().cancelTask(taskID);
                parkour.getConfig().set("Particles.path", particleList);
                parkour.saveConfig();
                player.sendMessage(Utils.format(ref.getFinishedSetup()));
            }else if (args[0].equalsIgnoreCase("splits")) {
                player.sendMessage(parkour.getMySQL().getSplits(player.getUniqueId().toString()).get());
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to use this comnmand.");
        }
        return false;
    }

    private void getOfflinePlayer(UUID uuid, Consumer<OfflinePlayer> callback) {
        for (OfflinePlayer loop : Bukkit.getOfflinePlayers()) {
            if (loop.getUniqueId().equals(uuid)) {
                if (Bukkit.isPrimaryThread()) {
                    callback.accept(loop);
                } else {
                    Bukkit.getScheduler().runTask(parkour, () -> callback.accept(loop));
                }
                return;
            }
        }
        Runnable run = () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Bukkit.getScheduler().runTask(parkour, () -> callback.accept(player));
        };
        if (Bukkit.isPrimaryThread()) {
            new Thread(run).start();
        } else {
            run.run();
        }
    }
}
