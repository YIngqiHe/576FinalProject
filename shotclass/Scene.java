package shotclass;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Scene extends ShotBase {

    @JsonProperty("shots")
    private List<Shot> shots;

    public Scene() {
        super();
        this.shots = new ArrayList<>();
    }

    public Scene(String startTime, int frameId, long numTime) {
        super(startTime, frameId, numTime);
        this.shots = new ArrayList<>();
    }

    public void addShot(Shot shot) {
        this.shots.add(shot);
    }

    public void addSubshot(Subshot subshot) {
        Shot shot = this.shots.get(shots.size() - 1);
        String start_time = shot.getStartTime();
        int frame_id = shot.getFrameId();
        long num_time = shot.getNumTime();
        if (shot.getSubshots().size() == 0) {
           Subshot firstSubshot = new Subshot(start_time, frame_id, num_time);
           this.shots.get(shots.size() - 1).addSubshot(firstSubshot);
           
        }
        this.shots.get(shots.size() - 1).addSubshot(subshot);
    }

    public List<Shot> getShots() {
        return shots;
    }

    public void setShots(List<Shot> shots) {
        this.shots = shots;
    }

}
