package entities;

public class UpdaterResult {
    private int directDepNumber = -1;
    private int releaseSize = -1;
    private int artifactSize = -1;
    private int dependencySize = -1;
    private int versionSize = -1;
    private int changeEdgeSize = -1;
    private long generateGraphTime = -1;
    private long generateChangeEdgeTime = -1;
    private long normalizeTime = -1;
    private long solveTime = -1;
    private int MaxMemoryUsage = -1;
    private double initialQuality = -1.0;
    private double finalQuality = -1.0;
    private double updateCost = -1.0;
    private long totalTime = -1;

    public void setDirectDepNumber(int directDepNumber) {
        this.directDepNumber = directDepNumber;
    }

    public void setReleaseSize(int releaseSize) {
        this.releaseSize = releaseSize;
    }

    public void setArtifactSize(int artifactSize) {
        this.artifactSize = artifactSize;
    }

    public void setDependencySize(int dependencySize) {
        this.dependencySize = dependencySize;
    }

    public void setVersionSize(int versionSize) {
        this.versionSize = versionSize;
    }

    public void setChangeEdgeSize(int changeEdgeSize) {
        this.changeEdgeSize = changeEdgeSize;
    }

    public void setGenerateGraphTime(long generateGraphTime) {
        this.generateGraphTime = generateGraphTime;
    }

    public void setGenerateChangeEdgeTime(long generateChangeEdgeTime) {
        this.generateChangeEdgeTime = generateChangeEdgeTime;
    }

    public void setNormalizeTime(long normalizeTime) {
        this.normalizeTime = normalizeTime;
    }

    public void setSolveTime(long solveTime) {
        this.solveTime = solveTime;
    }

    public void setMaxMemoryUsage(int maxMemoryUsage) {
        MaxMemoryUsage = maxMemoryUsage;
    }

    public void setInitialQuality(double initialQuality) {
        this.initialQuality = initialQuality;
    }

    public void setFinalQuality(double finalQuality) {
        this.finalQuality = finalQuality;
    }

    public void setUpdateCost(double updateCost) {
        this.updateCost = updateCost;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public double getInitialQuality() {
        return initialQuality;
    }

    public String toCSVLine() {
        return directDepNumber + "," + releaseSize + "," + artifactSize + "," + dependencySize + "," + versionSize + "," + changeEdgeSize + "," + generateGraphTime + "," + generateChangeEdgeTime + "," + normalizeTime + "," + solveTime + "," + MaxMemoryUsage + "," + initialQuality + "," + finalQuality + "," + updateCost + "," +totalTime;
    }
}
