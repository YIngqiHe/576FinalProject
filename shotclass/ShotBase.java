package shotclass;


import com.fasterxml.jackson.annotation.JsonProperty;
public class ShotBase {
    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("frame_id")
    private int frameId;

    @JsonProperty("num_time")
    private long numTime;

    public ShotBase() {
        this.startTime = "00:00:00.000";
        this.frameId = 0;
        this.numTime = 0;
    }

    public ShotBase(String startTime, int frameId, long numTime) {
        this.startTime = startTime;
        this.frameId = frameId;
        this.numTime = numTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public long getNumTime() {
        return numTime;
    }

    public void setNumTime(long numTime) {
        this.numTime = numTime;
    }


}
