package experiments;

import entities.Configuration;
import entities.Project;
import entities.UpdaterResult;
import helpers.*;
import workers.ProjectGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * RQ3 & RQ4
 */
public class GoblinUpdaterWorker{
    static String resultHeader = "name,gitUrl,commitID,repoName,pomDepNumber,directDepNumber,releaseSize,artifactSize,dependencySize,versionSize,changeEdgeSize,generateGraphTime,generateChangeEdgeTime,normalizeTime,solveTime,MaxMemoryUsage,initialQuality,finalQuality,updateCost,totalTime";


    public static void executeUpdater(Configuration config){
        String exportOutputPath = ConstantProperties.outDirectory+config.getName()+"/";
        String exportCsvPath = exportOutputPath+"executionsData.csv";
        FileHelper.createDirectory(exportOutputPath);

        Set<Project> projects = ProjectGenerator.activeProjectDatasetFromCsv();
        Logger.info("---- "+config.getName()+" ----");
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportCsvPath))){
            writer.println(resultHeader);
            int i=0;
            int projectSize = projects.size();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for(Project project : projects) {
                i++;
                if(!FileHelper.fileExist(ConstantProperties.resultDirectory+"execution/"+config.getName()+"/"+project.getRepoName()+".log")) {
                    System.out.println(i + "/" + projectSize + " - " + project.getName() + " - " + LocalDateTime.now().format(formatter));
                    if (GitHelper.gitClone(project) == 0) {
                        String projectPath = ConstantProperties.experienceTmpDir + "/" + project.getRepoName();
                        if (GitHelper.gitCheckout(project.getCommitId(), projectPath) == 0) {
                            if (MavenHelper.checkPomExist(projectPath)) {
                                int pomDepNumber = MavenHelper.countDependencies(projectPath);
                                if(config.cleanWeaver()){
                                    WeaverHelper.removeAddedValues();
                                }
                                UpdaterResult result = UpdaterHelper.runUpdate(projectPath, exportOutputPath + project.getRepoName() + ".log", config.getPath());
                                String projectCsvLine = project.getName() + "," +
                                        project.getGitUrl() + "," +
                                        project.getCommitId() + "," +
                                        project.getRepoName() + ",";
                                writer.println(projectCsvLine + pomDepNumber + "," + result.toCSVLine());
                                writer.flush();
                            }
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
