package ws.rlns.parkour.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.util.Vector;
import ws.rlns.parkour.Parkour;

import java.util.UUID;
import java.util.function.Consumer;

@UtilityClass
public class Utils {

    public String format(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }



    public static String getSerializedLocation(Location loc) { //Converts location -> String
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch()
                + ";";
        //feel free to use something to split them other than semicolons (Don't use periods or numbers)
    }

    public static Location getDeserializedLocation(String s) {//Converts String -> Location
        String[] parts = s.split(";"); //If you changed the semicolon you must change it here too
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        float yaw = Float.parseFloat(parts[3]);
        float pitch = Float.parseFloat(parts[4]);
        World w = Bukkit.getServer().getWorld("world");
        return new Location(w, x, y, z, yaw, pitch); //can return null if the world no longer exists
    }


    public void drawLine(Location point1, Location point2, double space, Particle particle) {
        World world = point1.getWorld();
        Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), 1);
            length += space;
        }
    }

}
