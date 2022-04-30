package ws.rlns.parkour.database.data;


import lombok.Getter;
import org.bukkit.Bukkit;
import ws.rlns.parkour.Parkour;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class Leaderboard {

    private Parkour parkour;

    private HashMap<UUID, String> leaderboard;


    public Leaderboard(Parkour parkour) {
        this.parkour = parkour;
        leaderboard = new HashMap<>();
    }
}
