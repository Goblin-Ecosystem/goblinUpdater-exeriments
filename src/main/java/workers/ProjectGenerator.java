package workers;

import entities.Project;
import helpers.ConstantProperties;
import helpers.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ProjectGenerator {
    private static final String LARGE_PROJECT_DATASET = ConstantProperties.largeProjectDatasetPath;
    private static final String LARGE_PROJECT_COMMIT_ID_FILE = "COMMIT";

    public static Set<Project> largeProjectDatasetFromFolder(){
        Set<Project> projectSet = new HashSet<>();
        File inputProjectsFolder = new File(LARGE_PROJECT_DATASET);

        if (!inputProjectsFolder.exists() || !inputProjectsFolder.isDirectory()) {
            Logger.error(LARGE_PROJECT_DATASET + " is not a directory.");
            return projectSet;
        }

        File[] groupFolders = inputProjectsFolder.listFiles(File::isDirectory);
        for (File groupFolder : groupFolders) {
            File[] repoFolders = groupFolder.listFiles(File::isDirectory);
            for (File repoFolder : repoFolders) {
                String repoName = repoFolder.getName();
                String groupName = groupFolder.getName();
                String commitId = readFile(LARGE_PROJECT_DATASET+"/"+groupName+"/"+repoName+"/"+LARGE_PROJECT_COMMIT_ID_FILE);
                String gitHubUrl = createGitHubUrl(groupName, repoName);
                Project project = new Project(groupName + ":" + repoName, gitHubUrl, commitId, repoName);
                projectSet.add(project);
            }
        }
        return projectSet;
    }

    public static Set<Project> compileProjectDatasetFromCsv(){
        return importFromCsv(ConstantProperties.datasetDir+"finalDatasetCompile.csv");
    }

    public static Set<Project> activeProjectDatasetFromCsv(){
        return importFromCsv(ConstantProperties.datasetDir+"finalDataset.csv");
    }

    private static Set<Project> importFromCsv(String filePath) {
        Set<Project> projects = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 4) {
                    String name = data[0].trim();
                    String gitUrl = data[1].trim();
                    String commitId = data[2].trim();
                    String repoName = data[3].trim();

                    Project project = new Project(name, gitUrl, commitId, repoName);
                    projects.add(project);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projects;
    }

    private static String createGitHubUrl(String groupName, String repoName){
        return "https://github.com/"+groupName+"/"+repoName+".git";
    }

    private static String readFile(String filePath){
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            Logger.warn("ProjectsFiller: error reading file: " + e.getMessage());
            return null;
        }
    }
}
