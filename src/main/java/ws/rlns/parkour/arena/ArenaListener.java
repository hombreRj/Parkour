package ws.rlns.parkour.arena;

import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ws.rlns.parkour.Parkour;
import ws.rlns.parkour.database.MySQL;
import ws.rlns.parkour.database.data.CheckPoint;
import ws.rlns.parkour.database.data.ProfileDAO;
import ws.rlns.parkour.utils.Reference;
import ws.rlns.parkour.utils.Utils;

import java.awt.*;
import java.sql.Ref;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ArenaListener implements Listener {

    private Parkour parkour;
    private Arena arena;
    private Reference ref;
    private final MySQL sql;

    private HashSet<UUID> activePlayers;
    private HashMap<UUID, Location> checkPoints;
    private HashMap<UUID, Long> times;
    private HashMap<UUID, String> splits;
    public static HashMap<UUID, ProfileDAO> profileMap;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");

    public ArenaListener(Parkour parkour) {
        this.parkour = parkour;
        this.arena = parkour.getArena();
        this.ref = parkour.getReference();
        sql = parkour.getMySQL();
        activePlayers = new HashSet<>();
        checkPoints = new HashMap<>();
        times = new HashMap<>();
        profileMap = new HashMap<>();
        splits = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (activePlayers.contains(player.getUniqueId())) {
            if (player.isFlying()) {
                activePlayers.remove(player.getUniqueId());
                checkPoints.remove(player.getUniqueId());
                times.remove(player.getUniqueId());
                player.sendMessage(Utils.format(ref.getFlyingMessage()));
                player.getInventory().remove(Material.CLOCK);
                player.getInventory().remove(Material.WHITE_CARPET);
                player.getInventory().remove(Material.RED_BED);
            }
        }

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial() == Material.GOLD_BLOCK) {
            if (player.getLocation().getBlockX() == arena.getStartLocation().getBlockX()) {
                if (!activePlayers.contains(player.getUniqueId())) {
                    player.sendMessage(Utils.format(parkour.getReference().getStartMessage()));
                    player.playSound(player.getLocation(), Sound.valueOf(ref.getSound()), 10, 10);
                    activePlayers.add(player.getUniqueId());
                    checkPoints.put(player.getUniqueId(), arena.getStartLocation());
                    times.put(player.getUniqueId(), System.currentTimeMillis());
                    ItemStack itemStack = new ItemStack(Material.CLOCK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Utils.format(ref.getItemName()));
                    itemStack.setItemMeta(itemMeta);
                    ItemStack restartItem = new ItemStack(Material.WHITE_CARPET);
                    ItemMeta restartMeta = itemStack.getItemMeta();
                    restartMeta.setDisplayName(Utils.format(ref.getRestartItemName()));
                    restartItem.setItemMeta(restartMeta);
                    ItemStack quitItem = new ItemStack(Material.RED_BED);
                    ItemMeta quitMeta = itemStack.getItemMeta();
                    quitMeta.setDisplayName(Utils.format(ref.getQuitItemName()));
                    quitItem.setItemMeta(quitMeta);

                    player.getInventory().setItem(ref.getItemSlot() - 1, itemStack);
                    player.getInventory().setItem(ref.getRestartItemSlot() - 1, restartItem);
                    player.getInventory().setItem(ref.getQuitItemSlot() - 1, quitItem);
                } else {
                    times.remove(player.getUniqueId());
                    times.put(player.getUniqueId(), System.currentTimeMillis());
                    player.sendMessage(Utils.format(parkour.getReference().getStartMessage()));
                }
            } else if (player.getLocation().getBlockX() == arena.getEndLocation().getBlockX() && activePlayers.contains(player.getUniqueId())) {
                long startTime = times.get(player.getUniqueId());
                long endTime = System.currentTimeMillis();
                long msTime = endTime - startTime;
                Date end = new Date(msTime);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
                activePlayers.remove(player.getUniqueId());
                checkPoints.remove(player.getUniqueId());
                player.sendMessage(Utils.format(parkour.getReference().getEndMessage()).replaceAll("%time%", simpleDateFormat.format(end)));
                times.remove(player.getUniqueId());
                ProfileDAO profileDAO = new ProfileDAO(player.getUniqueId().toString(), simpleDateFormat.format(end).toString(), splits.get(player.getUniqueId()));
                if (sql.userExists(profileDAO)) {
                    profileDAO.setBestCompletedTime(sql.getTime(profileDAO).get());
                    try {
                        if (profileDAO.isBetter()) {
                            try {
                                player.sendMessage(Utils.format(ref.getBeatTime())
                                        .replaceAll("%oldTime%", profileDAO.getBestCompletedTime())
                                        .replaceAll("%newTime%", profileDAO.getCurrentCompleteTime()));
                                profileDAO.setBestCompletedTime(simpleDateFormat.format(end));
                                profileDAO.setBestCheckpointSplits(splits.get(player.getUniqueId()));
                                sql.updatePlayerDAO(profileDAO);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    profileDAO.setBestCompletedTime(profileDAO.getCurrentCompleteTime());
                    profileDAO.setBestCheckpointSplits(splits.get(player.getUniqueId()));
                    try {
                        sql.insertPlayerDAO(profileDAO);
                        player.sendMessage("You first for the first time! Saving to our records");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return;
            } else {
                arena.getCheckPointLocations().forEach(location -> {
                    if (player.getLocation().getBlockX() == location.getBlockX() && activePlayers.contains(player.getUniqueId()) && checkPoints.get(player.getUniqueId()).getBlockX() != location.getBlockX()) {
                        checkPoints.remove(player.getUniqueId());
                        checkPoints.put(player.getUniqueId(), location);
                        long startTime = times.get(player.getUniqueId());
                        long endTime = System.currentTimeMillis();
                        long msTime = endTime - startTime;
                        Date end = new Date(msTime);
                        if (splits.containsKey(player.getUniqueId())) {
                            String currentSplit = splits.get(player.getUniqueId());
                            currentSplit += simpleDateFormat.format(end) + ";";
                            splits.remove(player.getUniqueId());
                            splits.put(player.getUniqueId(), currentSplit);
                        } else {
                            splits.put(player.getUniqueId(), (simpleDateFormat.format(end) + ";"));
                        }

                        @NotNull int checkPointNumber =  CheckPoint.getCheckPointFromLocation(player.getLocation());

                        try{
                            String splits = parkour.getMySQL().getSplits(player.getUniqueId().toString()).get();
                            String[] split = splits.split(";");
                            System.out.println("player.getLocation() = " + player.getLocation());
                            player.sendMessage(Utils.format(ref.getCheckPointReachedWithSplits()).replaceAll("%cpnumber%", checkPointNumber + "")
                                    .replaceAll("%Currenttime%", simpleDateFormat.format(end))
                                    .replaceAll("%bestTime%", split[checkPointNumber-1]));
                        }catch (NoSuchElementException e){
                            player.sendMessage(Utils.format(ref.getCheckPointReached()).replaceAll("%cpnumber%", checkPointNumber + "")
                                    .replaceAll("%Currenttime%", simpleDateFormat.format(end)));

                        }
                        launchFirework(player.getLocation());

                        parkour.getArena().getArmorStands().forEach(armorStand -> {
                            if (armorStand.getLocation().getBlockX() == player.getLocation().getBlockX()) {
                                armorStand.setHelmet(null);
                                System.out.println("test");
                                Bukkit.getScheduler().runTaskLater(parkour, bukkitTask -> {
                                    armorStand.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
                                }, 20 * 5);
                            }
                        });


                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem().getItemMeta().getDisplayName().equals(Utils.format(ref.getItemName()))) {
                if (activePlayers.contains(player.getUniqueId())) {
                    player.teleport(checkPoints.get(player.getUniqueId()));
                    player.sendMessage(Utils.format(ref.getTeleportLastCheckPoint()));
                }
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.format(ref.getRestartItemName()))) {
                player.teleport(parkour.getArena().getStartLocation());
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.format(ref.getQuitItemName()))) {
                activePlayers.remove(player.getUniqueId());
                times.remove(player.getUniqueId());
                checkPoints.remove(player.getUniqueId());
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage(Utils.format(ref.getQuitMessage()));
                player.getInventory().remove(Material.CLOCK);
                player.getInventory().remove(Material.WHITE_CARPET);
                player.getInventory().remove(Material.RED_BED);
            }
        }
    }


    private void launchFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.GREEN).with(FireworkEffect.Type.STAR).trail(true).build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }

}
