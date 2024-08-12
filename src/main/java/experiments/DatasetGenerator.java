package experiments;

import entities.Project;
import helpers.*;
import workers.ProjectGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class DatasetGenerator{
    private static final String infoProjectsCsvPath = ConstantProperties.outDirectory+"infoProjectsDataSet.csv";

    public static void run() {
        Set<Project> projects = ProjectGenerator.largeProjectDatasetFromFolder();
        try (PrintWriter writer = new PrintWriter(new FileWriter(infoProjectsCsvPath))){
            writer.println("name,gitUrl,commitID,repoName,clone,pomExist,hasHierarchicalPom,lastCommitDate");
            int i=0;
            int projectSize = projects.size();
            for(Project project : projects) {
                i++;
                System.out.println(i+"/"+projectSize);
                boolean clone = false;
                boolean pomExist = false;
                boolean hasHierarchicalPom = true;
                String commitId = "";
                String lastCommitDate = "";
                if (GitHelper.gitClone(project) == 0) {
                    clone = true;
                    String projectPath = ConstantProperties.experienceTmpDir + "/" + project.getRepoName();
                    commitId = GitHelper.getCommitId(project.getRepoName());
                    if (MavenHelper.checkPomExist(projectPath)) {
                        pomExist = true;
                        if(!MavenHelper.hasModules(projectPath)) {
                            hasHierarchicalPom = false;
                            lastCommitDate = GitHelper.getLastCommitDate(project.getRepoName());
                        }
                    }
                }
                String line = project.getName() + "," +
                        project.getGitUrl() + "," +
                        commitId + "," +
                        project.getRepoName() + "," +
                        clone + "," +
                        pomExist + "," +
                        hasHierarchicalPom + "," +
                        lastCommitDate;
                writer.println(line);
                writer.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
