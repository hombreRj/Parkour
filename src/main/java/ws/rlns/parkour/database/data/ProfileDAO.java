package ws.rlns.parkour.database.data;


import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
public class ProfileDAO {
    private String uuid;
    private String bestCompletedTime, currentCompleteTime;
    private String splits;
    private String bestCheckpointSplits, currentCheckpointSplits;
    private List<String> deserializedSplits;
    SimpleDateFormat simpleDateFormat;


    public ProfileDAO(String uuid, String currentCompleteTime, String splits) {
        this.uuid = uuid;
        this.currentCompleteTime = currentCompleteTime;
        this.splits = splits;
        deserializedSplits = new ArrayList<>();
        simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");

    }

    public boolean isBetter() throws ParseException {
        if (simpleDateFormat.parse(currentCompleteTime).before(simpleDateFormat.parse(bestCompletedTime))){
            return true;
        }
        return false;
    }

    public void deserialize(){
        String[] temp = splits.split(";");
        deserializedSplits.addAll(Arrays.asList(temp));
    }

//    public String serialize(){
//        StringBuilder temp = new StringBuilder();
//        bestCheckpointSplits
//        return temp.toString();
//    }



}
