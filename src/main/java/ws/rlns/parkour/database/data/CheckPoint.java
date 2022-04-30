package ws.rlns.parkour.database.data;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Getter
public class CheckPoint {

    public static ArrayList<CheckPoint> checkPoints = new ArrayList<>();

    @Getter private int checkPointNumber;
    @Getter private Location location;

    public CheckPoint(int checkPointNumber, Location location) {
        this.checkPointNumber = checkPointNumber;
        this.location = location;
        checkPoints.add(this);
    }



    public static CheckPoint getCheckPointFromNumber(int num){
        return checkPoints.get(num);
    }

    public static @Nullable Integer getCheckPointFromLocation(Location location) {
        for (CheckPoint checkPoint : checkPoints) {
            System.out.println("checkPoint = " + checkPoint.getCheckPointNumber());
            if (checkPoint.getLocation().getBlockX() == (location.getBlockX())){
                return checkPoint.getCheckPointNumber();
            }
        }
        return null;
    }
}
