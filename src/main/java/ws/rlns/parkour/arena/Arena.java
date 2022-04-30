package ws.rlns.parkour.arena;

import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ws.rlns.parkour.Parkour;
import ws.rlns.parkour.database.data.CheckPoint;
import ws.rlns.parkour.utils.Reference;
import ws.rlns.parkour.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Getter
public class Arena {

    private final Parkour parkour;
    private final Reference ref;

    private List<Location> checkPointLocations;
    private HashSet<ArmorStand> armorStands;
    private Location startLocation, endLocation;
    final long startTime;
    public Arena(Parkour parkour) {
        this.parkour = parkour;
        ref = parkour.getReference();
        checkPointLocations = new ArrayList<>();
        armorStands = new HashSet<>();
        loadLocations();
        startTime = System.currentTimeMillis();
    }

    private void loadLocations() {

        int j = 1;
        for (String s : ref.getCheckPointPositions()) {

            Location loc = Utils.getDeserializedLocation(s);
            checkPointLocations.add(loc);
            parkour.getLogger().warning("Loaded " + checkPointLocations.size() + " checkpoints!");

            ArmorStand checkPoint = (ArmorStand) loc.getWorld().spawn(loc.toCenterLocation(), ArmorStand.class);
            checkPoint.setGravity(false);
            checkPoint.setCanPickupItems(false);
            checkPoint.setCustomName(Utils.format(ref.getCheckPointName()));
            checkPoint.setCustomNameVisible(false);
            checkPoint.setVisible(false);
            checkPoint.setCanMove(false);
            checkPoint.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
            armorStands.add(checkPoint);
            Bukkit.getScheduler().runTaskTimer(parkour, bukkitTask -> {
                Location rotatingLoc = checkPoint.getLocation().clone();
                float yaw = rotatingLoc.getYaw() + 4;
                if (yaw >= 180)
                    yaw *= -1;
                rotatingLoc.setYaw(yaw);
                double timeSeconds = Math.floor((double) System.currentTimeMillis() - (double) this.startTime / 1000D);
                checkPoint.teleport(rotatingLoc.add(0,(Math.sin(timeSeconds) * .05), 0));
            }, 0, 1);
            new CheckPoint(j, loc);
            j++;
        }
        startLocation = Utils.getDeserializedLocation(ref.getStartPosition());
        endLocation = Utils.getDeserializedLocation(ref.getEndPosition());


        ArmorStand start = (ArmorStand) startLocation.getWorld().spawn(startLocation.toCenterLocation(), ArmorStand.class);
        start.setGravity(false);
        start.setCanPickupItems(false);
        start.setCustomName(Utils.format(ref.getStartName()));
        start.setCustomNameVisible(false);
        start.setVisible(false);
        start.setCanMove(false);
        start.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
        armorStands.add(start);


        Bukkit.getScheduler().runTaskTimer(parkour, bukkitTask -> {

            Location rotatingLoc = start.getLocation().clone();
            float yaw = rotatingLoc.getYaw() + 4;
            if (yaw >= 180)
                yaw *= -1;
            rotatingLoc.setYaw(yaw);
            double timeSeconds = Math.floor((double) System.currentTimeMillis() - (double) this.startTime / 1000D);
            start.teleport(rotatingLoc.add(0,(Math.sin(timeSeconds) * .05), 0));
        }, 0, 1);

        ArmorStand end = (ArmorStand) endLocation.getWorld().spawn(endLocation.toCenterLocation(), ArmorStand.class);
        end.setGravity(false);
        end.setCanPickupItems(false);
        end.setCustomName(Utils.format(ref.getEndName()));
        end.setCustomNameVisible(false);
        end.setVisible(false) ;
        end.setCanMove(false);
        end.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
        armorStands.add(end);
        Bukkit.getScheduler().runTaskTimer(parkour, bukkitTask -> {
            Location rotatingLoc = end.getLocation().clone();
            float yaw = rotatingLoc.getYaw() + 4;
            if (yaw >= 180)
                yaw *= -1;
            rotatingLoc.setYaw(yaw);
            double timeSeconds = Math.floor((double) System.currentTimeMillis() - (double) this.startTime / 1000D);
            end.teleport(rotatingLoc.add(0,(Math.sin(timeSeconds) * .05), 0));
        }, 0, 1);


        parkour.getLogger().warning("Loaded both end and start points");


        Bukkit.getScheduler().scheduleSyncRepeatingTask(parkour, () -> {
            for (int i = 0; i < parkour.getReference().getParticleLocations().size() -1; i++) {
                    Utils.drawLine(Utils.getDeserializedLocation(parkour.getReference().getParticleLocations().get(i)), Utils.getDeserializedLocation(parkour.getReference().getParticleLocations().get((i + 1))), 1.5,Particle.valueOf(ref.getParticleTrail()));
            }
        }, 20L, 0);

    }





}
