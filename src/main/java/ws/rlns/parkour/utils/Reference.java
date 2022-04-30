package ws.rlns.parkour.utils;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import ws.rlns.parkour.Parkour;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Reference {

    private final String quitItemName;
    private Parkour parkour;

    private String flyingMessage, quitMessage, restartItemName,particleTrail, leaderboardTop, finishedSetup, addCheckPoint, setEndMessage ,setMessage, leaderboardBottom, beatTime, leaderboardFormat, user, pass, host, database, noPB, pB, startPosition, endPosition, worldName, startMessage, sound, itemName, teleportLastCheckPoint, endMessage, checkPointName, startName, endName, checkPointReachedWithSplits, checkPointReached;
    private int port, itemSlot, restartItemSlot, quitItemSlot;
    private boolean mySQLUseSSL;
    private List<String> parkourHelp, particleLocations;


    private List<String> checkPointPositions;
    public Reference(Parkour parkour) {
        this.parkour = parkour;
        FileConfiguration cfg = parkour.getConfig();

        //Arena
        checkPointPositions = new ArrayList<>();
        startPosition = cfg.getString("Arena.startPosition");
        endPosition = cfg.getString("Arena.endPosition");
        checkPointPositions = cfg.getStringList("Arena.checkPoints");
        worldName = cfg.getString("Arena.world");

        //MySQL
        user = cfg.getString("MySql.user");
        pass = cfg.getString("MySql.pass");
        host = cfg.getString("MySql.host");
        database = cfg.getString("MySql.database");
        port = cfg.getInt("MySql.port");
        mySQLUseSSL = cfg.getBoolean("MySql.useSSL");

        //sounds
        sound = cfg.getString("sound.sound");

        //items
        itemSlot = cfg.getInt("item.itemSlot");
        itemName = cfg.getString("item.itemName");
        restartItemName = cfg.getString("item.restartItemName");
        restartItemSlot = cfg.getInt("item.restartItemSlot");
        quitItemName = cfg.getString("item.quitItemName");
        quitItemSlot = cfg.getInt("item.quitItemSlot");

        //particles
        particleTrail = cfg.getString("Particles.particleTrail");
        particleLocations = cfg.getStringList("Particles.path");

        //Messages
        startMessage = cfg.getString("Messages.startMessage");
        endMessage = cfg.getString("Messages.endMessage");
        teleportLastCheckPoint = cfg.getString("Messages.teleportLastCheckPoint");
        checkPointName = cfg.getString("Messages.checkPointName");
        endName = cfg.getString("Messages.endName");
        startName = cfg.getString("Messages.startName");
        checkPointReached = cfg.getString("Messages.checkPointReached");
        checkPointReachedWithSplits = cfg.getString("Messages.checkPointReachedWithSplits");
        noPB = cfg.getString("Messages.noPB");
        pB = cfg.getString("Messages.pb");
        leaderboardTop = cfg.getString("Messages.leaderboardTop");
        leaderboardBottom= cfg.getString("Messages.leaderboardBottom");
        leaderboardFormat= cfg.getString("Messages.leaderboardFormat");
        beatTime = cfg.getString("Messages.beatTime");
        parkourHelp = cfg.getStringList("Messages.parkourHelp");
        setMessage = cfg.getString("Messages.setMessage");
        setEndMessage = cfg.getString("Messages.setEndMessage");
        addCheckPoint = cfg.getString("Messages.addCheckPoint");
        finishedSetup = cfg.getString("Messages.finishedSetup");
        flyingMessage = cfg.getString("Messages.flyingNotAllowed");
        quitMessage = cfg.getString("Messages.quitMessage");
    }
}
