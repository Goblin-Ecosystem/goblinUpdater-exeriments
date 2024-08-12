package experiments;

import entities.Configuration;
import entities.Dependency;
import entities.Project;
import helpers.*;
import org.apache.maven.shared.invoker.MavenInvocationException;
import workers.ProjectGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RQ1
 */
public class CompileAndTestWorker {
    static String resultHeader = "name,gitUrl,commitID,repoName,compile,passTest,CompileAfterUpdate,passTestAfterUpdate";

    public static void run(Configuration config) {
        String exportOutputPath = ConstantProperties.outDirectory+"compileAndTest/"+config.getName()+"/";
        String exportCsvPath = exportOutputPath+"compileAndTest.csv";
        FileHelper.createDirectory(exportOutputPath);


        Set<Project> projects = ProjectGenerator.compileProjectDatasetFromCsv();
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportCsvPath))){
            writer.println(resultHeader);
            int i=0;
            int projectSize = projects.size();
            for(Project project : projects) {
                i++;
                System.out.println(i+"/"+projectSize +" - "+project.getName());
                String projectPath = ConstantProperties.experienceTmpDir+"/"+project.getRepoName();
                if (GitHelper.gitClone(project) == 0) {
                    if (GitHelper.gitCheckout(project.getCommitId(), projectPath) == 0) {
                        if (MavenHelper.checkPomExist(projectPath)) {
                            //Build and test before change
                            Map<String, Boolean> beforeChangeResult = MavenHelper.runBuildAndTest(projectPath);
                            Map<String, Boolean> afterChangeResult = new HashMap<>();
                            if(beforeChangeResult.get("build") && beforeChangeResult.get("test")) {
                                //Edit pom.xml
                                Set<Dependency> dependencies = UpdaterHelper.readUpdaterChangeFor(project, config);
                                MavenHelper.replaceAllDependencies(dependencies, projectPath);
                                //Build and test after change
                                afterChangeResult = MavenHelper.runBuildAndTest(projectPath);
                            }
                            else {
                                afterChangeResult.put("build", false);
                                afterChangeResult.put("test", false);
                            }
                            //Export
                            String projectCsvLine = project.getName() + "," +
                                    project.getGitUrl() + "," +
                                    project.getCommitId() + "," +
                                    project.getRepoName() + ",";
                            writer.println(projectCsvLine+beforeChangeResult.get("build")+","+beforeChangeResult.get("test")+","+afterChangeResult.get("build")+","+afterChangeResult.get("test"));
                            writer.flush();
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
