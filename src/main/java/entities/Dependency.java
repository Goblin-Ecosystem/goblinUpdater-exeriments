package entities;

public class Dependency {
    private String groupId;
    private String artifactId;
    private String version;
    private long timestamp = 0;

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String gav(){
        return groupId+":"+artifactId+":"+version;
    }

    @Override
    public String toString() {
        return gav();
    }
}
