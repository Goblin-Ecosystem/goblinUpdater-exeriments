package experiments;

import entities.Configuration;
import entities.Dependency;
import entities.Project;
import entities.UpdaterResult;
import helpers.*;
import workers.ProjectGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

/**
 * RQ2
 * Naive approach max, max minor and max patch
 * compute quality for each and compare with our
 * initial quality and our quality compute on PerformanceAndScalability exp
 */
public class NaiveUpdateWorker{
    static String resultHeader = "name,gitUrl,commitID,repoName,initialQuality,maxAllQuality,maxAllCost,maxAllCompile,maxAllTest,maxMinorQuality,maxMinorCost,maxMinorCompile,maxMinorTest,maxPatchQuality,maxPatchCost,maxPatchCompile,maxPatchTest";

    public static void run(Configuration config) {
        String exportOutputPath = ConstantProperties.outDirectory+"naiveUpdate/"+config.getName()+"/";
        String exportCsvPath = exportOutputPath+"naiveUpdate.csv";
        FileHelper.createDirectory(exportOutputPath);

        Set<Project> projects = ProjectGenerator.compileProjectDatasetFromCsv();
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportCsvPath))){
            writer.println(resultHeader);
            int i=0;
            int projectSize = projects.size();
            for(Project project : projects) {
                i++;
                System.out.println(i+"/"+projectSize +" - "+project.getName());
                if (GitHelper.gitClone(project) == 0) {
                    String projectPath = ConstantProperties.experienceTmpDir + "/" + project.getRepoName();
                    if (GitHelper.gitCheckout(project.getCommitId(), projectPath) == 0) {
                        if (MavenHelper.checkPomExist(projectPath)) {
                            Set<Dependency> directDependencies = MavenHelper.getProjectDirectDependencies(Path.of(projectPath));
                            double initialQuality = UpdaterHelper.getInitialQuality(project, config);
                            Map<String, Set<Dependency>> replacementMap = getNaiveUpdates(directDependencies);
                            // Max version
                            Set<Dependency> maxMajorGraph = WeaverHelper.getDependencyGraph(replacementMap.get("maxMajor"));
                            Map<String, Double> maxMajorResult = UpdaterHelper.getGraphQualityAndCost(project, replacementMap.get("maxMajor"), maxMajorGraph, config);
                            MavenHelper.changeDependenciesVersion(replacementMap.get("maxMajor"), projectPath);
                            Map<String, Boolean> maxMajorCompileAndTest = MavenHelper.runBuildAndTest(projectPath);
                            // Max minor
                            Set<Dependency> maxMinorGraph = WeaverHelper.getDependencyGraph(replacementMap.get("maxMinor"));
                            Map<String, Double> maxMinorResult = UpdaterHelper.getGraphQualityAndCost(project, replacementMap.get("maxMinor"), maxMinorGraph, config);
                            MavenHelper.changeDependenciesVersion(replacementMap.get("maxMinor"), projectPath);
                            Map<String, Boolean> maxMinorCompileAndTest = MavenHelper.runBuildAndTest(projectPath);
                            // Max patch
                            Set<Dependency> maxPatchGraph = WeaverHelper.getDependencyGraph(replacementMap.get("maxPatch"));
                            Map<String, Double> maxPatchResult = UpdaterHelper.getGraphQualityAndCost(project, replacementMap.get("maxPatch"), maxPatchGraph, config);
                            MavenHelper.changeDependenciesVersion(replacementMap.get("maxPatch"), projectPath);
                            Map<String, Boolean> maxPatchCompileAndTest = MavenHelper.runBuildAndTest(projectPath);
                            // Export
                            String projectCsvLine = project.getName() + "," +
                                    project.getGitUrl() + "," +
                                    project.getCommitId() + "," +
                                    project.getRepoName() + ",";
                            writer.println(projectCsvLine+initialQuality+","
                                    +maxMajorResult.get("quality")+","+maxMajorResult.get("cost")+","+maxMajorCompileAndTest.get("build")+","+maxMajorCompileAndTest.get("test")
                                    +","+maxMinorResult.get("quality")+","+maxMinorResult.get("cost")+","+maxMinorCompileAndTest.get("build")+","+maxMinorCompileAndTest.get("test")
                                    +","+maxPatchResult.get("quality")+","+maxPatchResult.get("cost")+","+maxPatchCompileAndTest.get("build")+","+maxPatchCompileAndTest.get("test")
                            );
                            writer.flush();
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Set<Dependency>> getNaiveUpdates(Set<Dependency> directDependencies){
        Set<Dependency> latestVersionsReplacement = new HashSet<>();
        Set<Dependency> latestMajorVersionReplacement = new HashSet<>();
        Set<Dependency> latestMinorVersionReplacement = new HashSet<>();
        for(Dependency dependency : directDependencies){
            Set<Dependency> artifactRelease = WeaverHelper.getArtifactRelease(dependency.getGroupId(), dependency.getArtifactId());
            // Last version
            Dependency latestVersion = artifactRelease.stream()
                    .max(Comparator.comparingLong(Dependency::getTimestamp))
                    .orElse(null);
            String[] currentVersionParts = dependency.getVersion().split("\\.");
            latestVersion = latestVersion == null ? dependency : latestVersion;
            if(!currentVersionParts[0].matches("\\d+")){
                latestVersionsReplacement.add(latestVersion);
                latestMajorVersionReplacement.add(latestVersion);
                latestMinorVersionReplacement.add(latestVersion);
                continue;
            }
            int currentMajor = Integer.parseInt(currentVersionParts[0]);

            // Last version without change major
            Dependency latestMajorVersion = artifactRelease.stream()
                    .filter(dep -> {
                        String[] versionParts = dep.getVersion().split("\\.");
                        String majorVersionPart = versionParts[0].split("-")[0];
                        boolean isMajorNumeric = majorVersionPart.matches("\\d+");
                        if (!isMajorNumeric) {
                            return false;
                        }
                        return Integer.parseInt(majorVersionPart) == currentMajor;
                    })
                    .max(Comparator.comparingLong(Dependency::getTimestamp))
                    .orElse(null);
            latestMajorVersion = latestMajorVersion == null ? dependency : latestMajorVersion;
            if(currentVersionParts.length>1) {
                int currentMinor = Integer.parseInt(currentVersionParts[1].split("-")[0]);
                // Last version without change major and minor
                Dependency latestMinorVersion = artifactRelease.stream()
                        .filter(dep -> {
                            String[] versionParts = dep.getVersion().split("\\.");
                            String majorVersionPart = versionParts[0].split("-")[0];
                            boolean isMajorNumeric = majorVersionPart.matches("\\d+");
                            if (!isMajorNumeric) {
                                return false;
                            }
                            int majorVersion = Integer.parseInt(majorVersionPart);
                            String minorVersionPart = versionParts[1].split("-")[0];
                            boolean isMinorNumeric = minorVersionPart.matches("\\d+");
                            if (!isMinorNumeric) {
                                return false;
                            }
                            int minorVersion = Integer.parseInt(minorVersionPart);
                            return majorVersion == currentMajor && minorVersion == currentMinor;
                        })
                        .max(Comparator.comparingLong(Dependency::getTimestamp))
                        .orElse(null);
                latestMinorVersion = latestMinorVersion == null ? dependency : latestMinorVersion;
                latestVersionsReplacement.add(latestVersion);
                latestMajorVersionReplacement.add(latestMajorVersion);
                latestMinorVersionReplacement.add(latestMinorVersion);
            } else{
                latestVersionsReplacement.add(latestVersion);
                latestMajorVersionReplacement.add(latestVersion);
                latestMinorVersionReplacement.add(latestVersion);
            }
        }
        Map<String, Set<Dependency>> replacementMap = new HashMap<>();
        replacementMap.put("maxMajor", latestVersionsReplacement);
        replacementMap.put("maxMinor", latestMajorVersionReplacement);
        replacementMap.put("maxPatch", latestMinorVersionReplacement);
        return replacementMap;
    }
}
