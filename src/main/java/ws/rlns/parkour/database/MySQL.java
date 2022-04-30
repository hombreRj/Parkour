package ws.rlns.parkour.database;

import ws.rlns.parkour.Parkour;
import ws.rlns.parkour.database.data.ProfileDAO;
import ws.rlns.parkour.utils.Reference;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class MySQL {


    private Parkour parkour;
    private Reference ref;

    private final String user, pass, host, database;
    private final int port;

    private Connection connection;

    private SimpleDateFormat simpleDateFormat;


    public MySQL(Parkour parkour) {
        this.parkour = parkour;
        ref = parkour.getReference();
        user = ref.getUser();
        pass = ref.getPass();
        host = ref.getHost();
        database = ref.getDatabase();
        port = ref.getPort();
        simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");

        try{
            connect();
            parkour.getLogger().log(Level.WARNING, "Successfully connected to MySQl");
            if (!tableExists("parkour_data11")){
                createTable();
            }
        }catch (Exception e){
            parkour.getLogger().log(Level.SEVERE, "FAILED TO CONNECT TO DATABASE | SHUTTING DOWN");
            parkour.getPluginLoader().disablePlugin(parkour);
            e.printStackTrace();
        }
    }


    public boolean isConnected() {
        return (connection != null);
    }

    private void connect() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" +
        host + ":" + port + "/" + database + "?useSSL=" + (ref.isMySQLUseSSL() ? "true" : "false"),
                user, pass);
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    private boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[] {"TABLE"});

        return resultSet.next();
    }

    public boolean insertPlayerDAO (ProfileDAO profile) throws SQLException {
        try {
             PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO parkour_data11(uuid, bestTime, bestSplits) VALUES(?, ?, ?);"
        );
            stmt.setString(1, profile.getUuid());
            stmt.setString(2, profile.getBestCompletedTime());
            stmt.setString(3, profile.getSplits());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            parkour.getLogger().log(Level.SEVERE, "Failed inserting profile " + profile.getUuid() + " into the SQL database");
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean userExists(ProfileDAO profileDAO){
        try{
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM parkour_data11 WHERE uuid=?");
            stmt.setString(1, profileDAO.getUuid());
            ResultSet set = stmt.executeQuery();
            if (set.next()){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            parkour.getLogger().severe("Failed loading player...");
        }
        return false;
    }

    public Optional<String> getTime(UUID uuid) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "select bestTime from parkour_data11 where uuid = ?;;");
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("bestTime").toString());
            }
            return Optional.empty();
        } catch (SQLException e) {
            parkour.getLogger().severe("Could not get user time");
            return Optional.empty();
        }
    }


    public Optional<List<UUID>> getTopTimes(int amt) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT uuid, bestTime FROM parkour_data11 ORDER BY bestTime ASC LIMIT ?;");
            stmt.setInt(1, amt);
            ResultSet resultSet = stmt.executeQuery();
            List<UUID> topPlayers = new LinkedList<>();
            while (resultSet.next()) {
                topPlayers.add(UUID.fromString(resultSet.getString("uuid")));
                parkour.getLeaderboard().getLeaderboard().put(UUID.fromString(resultSet.getString("uuid")), resultSet.getString("bestTime"));
            }
            return Optional.of(topPlayers);
        } catch (SQLException e) {
            parkour.getLogger().severe("Could not get user time");
            return Optional.empty();
        }
    }



    public Optional<String> getTime(ProfileDAO dao) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "select bestTime from parkour_data11 where uuid = ?;;");
            stmt.setString(1, dao.getUuid().toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("bestTime").toString());
            }
            return Optional.empty();
        } catch (SQLException e) {
            parkour.getLogger().severe("Could not get user time");
            return Optional.empty();
        }
    }

    public Optional<String> getSplits(ProfileDAO dao) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "select bestSplits from parkour_data11 where uuid = ?;;");
            stmt.setString(1, dao.getUuid().toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                dao.setBestCheckpointSplits(resultSet.getString("bestSplits"));
                return Optional.of(resultSet.getString("bestSplits").toString());
            }
            return Optional.empty();
        } catch (SQLException e) {
            parkour.getLogger().severe("Could not get user time");
            return Optional.empty();
        }
    }

    public Optional<String> getSplits(String s) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "select bestSplits from parkour_data11 where uuid = ?;;");
            stmt.setString(1, s);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("bestSplits").toString());
            }
            return Optional.empty();
        } catch (SQLException e) {
            parkour.getLogger().severe("Could not get user time");
            return Optional.empty();
        }
    }

    private void createTable() {
        String parkourTable = "CREATE TABLE parkour_data11 ("
                + "uuid CHAR(36) NOT NULL,"
                + "bestTime CHAR(100),"
                + "bestSplits TEXT(600))";
        try {
            Statement statement = connection.createStatement();
            //This line has the issue
            statement.executeUpdate(parkourTable);
            System.out.println("Table Created");
        }
        catch (SQLException e ) {
            System.out.println("An error has occurred on Table Creation");
            e.printStackTrace();
        }
    }

    public boolean updatePlayerDAO (ProfileDAO profile) throws SQLException {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE parkour_data11 SET bestTime = ? bestSplits = ? WHERE uuid = ?"
            );
            stmt.setString(1, profile.getBestCompletedTime());
            stmt.setString(2, profile.getBestCheckpointSplits());
            stmt.setString(3, profile.getUuid());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            parkour.getLogger().log(Level.SEVERE, "Failed updating profile " + profile.getUuid() + " into the SQL database");
            e.printStackTrace();
        }
        return false;
    }


}
