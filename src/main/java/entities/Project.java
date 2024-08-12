package entities;

import java.util.Objects;

public class Project {
    private final String name;
    private final String gitUrl;
    private String commitId;
    private String repoName;

    public Project(String name, String gitUrl, String commitId, String repoName) {
        this.name = name;
        this.gitUrl = gitUrl;
        this.commitId = commitId;
        this.repoName = repoName;
    }

    public String getName() {
        return name;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return name.equals(project.name) && gitUrl.equals(project.gitUrl) && commitId.equals(project.commitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, gitUrl, commitId);
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", commitId='" + commitId + '\'' +
                ", repoName='" + repoName + '\'' +
                '}';
    }
}
