package experiments;

import entities.Configuration;
import entities.Dependency;
import entities.Project;
import helpers.*;
import workers.ProjectGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public class CompareConfQualityAndCost {
    static String resultHeader = "name,quality,cost";

    public static void run(Configuration config, Configuration referenceConfig){
        String exportOutputPath = ConstantProperties.outDirectory+"compareConfQualityAndCost/"+config.getName()+"/";
        String exportCsvPath = exportOutputPath+"compareWith_"+referenceConfig.getName()+".csv";
        FileHelper.createDirectory(exportOutputPath);

        Set<Project> projects = ProjectGenerator.compileProjectDatasetFromCsv();
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportCsvPath))){
            writer.println(resultHeader);
            int i=0;
            int projectSize = projects.size();
            for (Project project : projects) {
                i++;
                System.out.println(i + "/" + projectSize + " - " + project.getName());
                // Get config graph
                Set<Dependency> dependenciesGraph = UpdaterHelper.readUpdaterChangeFor(project, config);
                // Get reference quality and cost
                Map<String, Double> qualityAndCost = UpdaterHelper.getGraphQualityAndCost(project, dependenciesGraph, dependenciesGraph, referenceConfig);
                //Export
                String projectCsvLine = project.getName() + ",";
                writer.println(projectCsvLine + qualityAndCost.get("quality") + "," + qualityAndCost.get("cost"));
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
