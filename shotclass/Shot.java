package shotclass;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Shot extends ShotBase{

    @JsonProperty("subshots")
    private List<Subshot> subshots;

    public Shot() {
        super();
        this.subshots = new ArrayList<>();
    }

    public Shot(String startTime, int frameId, long numTime) {
        super(startTime, frameId, numTime);
        this.subshots = new ArrayList<>();
    }

    public void addSubshot(Subshot subshot) {
        this.subshots.add(subshot);
    }
    
    public List<Subshot> getSubshots() {
        return subshots;
    }

    public void setSubshots(List<Subshot> subshots) {
        this.subshots = subshots;
    }

}
