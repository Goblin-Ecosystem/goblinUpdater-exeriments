package helpers;

import entities.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdaterHelper {

    public static UpdaterResult runUpdate(String projectPath, String outputExportPath, String configPath){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("java",
                "-DweaverUrl="+YmlConfReader.getInstance().weaverUrl(),
                "-DprojectPath="+projectPath,
                "-DconfFile="+configPath,
                "-jar",
                ConstantProperties.updaterJar);
        List<String> output = SystemHelper.execCommand(processBuilder);
        FileHelper.listStringToFile(output, outputExportPath);
        return parseUpdaterOutput(output);
    }

    public static double getInitialQuality(Project project, Configuration config){
        String logFilePath = ConstantProperties.resultDirectory+"execution/"+config.getName()+"/"+project.getRepoName()+".log";
        double initialQuality = -1.0;
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Initial graph quality")) {
                    Pattern pattern = Pattern.compile("Initial graph quality: (\\d+)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        initialQuality = Double.parseDouble(matcher.group(1));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return initialQuality;
    }

    public static Map<String, Double> getGraphQualityAndCost(Project project, Set<Dependency> directDependencies, Set<Dependency> dependencyTree, Configuration config){
        String logFilePath = ConstantProperties.resultDirectory+"execution/"+config.getName()+"/"+project.getRepoName()+".log";
        double quality = 0.0;
        double cost = 0.0;
        // No solution case
        if(directDependencies.size() == 0){
            quality = getInitialQuality(project, config);
            System.out.println("Initial quality: "+quality);
            Map<String, Double> resultMap = new HashMap<>();
            resultMap.put("quality", quality);
            resultMap.put("cost", cost);
            return resultMap;
        }
        for(Metric metric : config.getMetrics()) {
            if(metric.getName().equals("COST")){
                continue;
            }
            double metricQuality = 0.0;
            Map<String, Double> metricValueMap = getMetricValueMap(metric.getName(), logFilePath);
            for (Dependency dependency : dependencyTree) {
                String gav = dependency.gav();
                String modifiedGav = gav.replace("-", "_").replace(":", "_");
                if (metricValueMap.containsKey(modifiedGav)) {
                    metricQuality += metricValueMap.get(modifiedGav);
                }
            }
            quality += (metricQuality*metric.getCoef());
        }
        Map<String, Double> costMap = getCostValueMap(logFilePath);
        for (Dependency dependency : directDependencies) {
            String gav = dependency.gav();
            String modifiedGav = gav.replace("-", "_").replace(":", "_");
            if (costMap.containsKey(modifiedGav)) {
                cost += costMap.get(modifiedGav);
            }
        }
        Map<String, Double> resultMap = new HashMap<>();
        resultMap.put("quality", quality);
        resultMap.put("cost", cost);
        return resultMap;
    }

    private static Map<String, Double> getMetricValueMap(String metric, String logFilePath){
        boolean record = false;
        Map<String, Double> metricMap = new HashMap<>();
        Pattern p = Pattern.compile("([+-]?\\d*\\.?\\d+) \\(([^)]+)\\)");
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("Quality["+metric+"]_Constraint:")){
                    record = true;
                }
                if (record){
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        String gav = m.group(2);
                        double value = Double.parseDouble(m.group(1));
                        metricMap.put(gav, value);
                    }
                    if(line.contains("Quality["+metric+"]  =")){
                        record = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metricMap;
    }

    private static Map<String, Double> getCostValueMap(String logFilePath){
        boolean record = false;
        Map<String, Double> costMap = new HashMap<>();
        Pattern p = Pattern.compile("\\+([\\de.-]+) ROOT___([^\\(]+)");
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("COST_Constraint:")){
                    record = true;
                }
                if (record){
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        String gav = m.group(2).substring(0, m.group(2).length() - 1);;
                        double value = Double.parseDouble(m.group(1));
                        costMap.put(gav, value);
                    }
                    if(line.contains("COST  =")){
                        record = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return costMap;
    }

    public static Set<Dependency> readUpdaterChangeFor(Project project, Configuration config) {
        String logFilePath = ConstantProperties.resultDirectory+"execution/"+config.getName()+"/"+project.getRepoName()+".log";
        Set<Dependency> dependencies = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            Pattern pattern = Pattern.compile("\\[INFO \\] \\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} \\(([^)]+)\\) : 1\\.0");
            String line;
            boolean recordLines = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("## Solution:")) {
                    recordLines = true; // record at solution line
                    continue;
                }
                if (recordLines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String[] splitedGav = matcher.group(1).split(":");
                        if (splitedGav.length==3){
                            dependencies.add(new Dependency(splitedGav[0], splitedGav[1], splitedGav[2]));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dependencies;
    }

    public static Set<Dependency> readUpdaterDirectChangeFor(Project project, Configuration config) {
        String logFilePath = ConstantProperties.resultDirectory+"execution/"+config.getName()+"/"+project.getRepoName()+".log";
        Set<Dependency> dependencies = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            Pattern pattern = Pattern.compile("\\b([a-zA-Z0-9\\._-]+):([a-zA-Z0-9_-]+):(\\d+\\.\\d+\\.\\d+[-\\w]*)\\b");
            String line;
            boolean recordLines = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("## Solution:")) {
                    recordLines = true; // record at solution line
                    continue;
                }
                if (recordLines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        if (matcher.group(1) != null && matcher.group(2) != null && matcher.group(3) != null){
                            dependencies.add(new Dependency(matcher.group(1), matcher.group(2), matcher.group(3)));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dependencies;
    }

    private static UpdaterResult parseUpdaterOutput(List<String> output) {
        UpdaterResult updaterResult = new UpdaterResult();
        if(output == null){
            return updaterResult;
        }
        Pattern pattern;
        Matcher matcher;
        for (String line : output) {
            if (line.contains("Direct dependencies number")) {
                pattern = Pattern.compile("Direct dependencies number: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setDirectDepNumber(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Release nodes size")) {
                pattern = Pattern.compile("Release nodes size: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setReleaseSize(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Artifact nodes size")) {
                pattern = Pattern.compile("Artifact nodes size: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setArtifactSize(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Dependency edges size")) {
                pattern = Pattern.compile("Dependency edges size: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setDependencySize(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Version edges size")) {
                pattern = Pattern.compile("Version edges size: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setVersionSize(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Change edges size")) {
                pattern = Pattern.compile("Change edges size: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setChangeEdgeSize(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Time to generate graph (ms)")) {
                pattern = Pattern.compile("Time to generate graph \\(ms\\): (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setGenerateGraphTime(Long.parseLong(matcher.group(1)));
                }
            } else if (line.contains("Time to generate generate change edges (ms)")) {
                pattern = Pattern.compile("Time to generate generate change edges \\(ms\\) : (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setGenerateChangeEdgeTime(Long.parseLong(matcher.group(1)));
                }
            } else if (line.contains("Time to normalize metrics (ms)")) {
                pattern = Pattern.compile("Time to normalize metrics \\(ms\\): (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setNormalizeTime(Long.parseLong(matcher.group(1)));
                }
            } else if (line.contains("Time to solve (ms)")) {
                pattern = Pattern.compile("Time to solve \\(ms\\): (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setSolveTime(Long.parseLong(matcher.group(1)));
                }
            } else if (line.contains("Max memory usage (Mo)")) {
                pattern = Pattern.compile("Max memory usage \\(Mo\\): (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setMaxMemoryUsage(Integer.parseInt(matcher.group(1)));
                }
            } else if (line.contains("Initial graph quality")) {
                pattern = Pattern.compile("Initial graph quality: (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setInitialQuality(Double.parseDouble(matcher.group(1)));
                }
            } else if (line.contains(" QUALITY :")) {
                pattern = Pattern.compile("QUALITY : (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setFinalQuality(Double.parseDouble(matcher.group(1)));
                }
            } else if (line.contains(" COST :")) {
                pattern = Pattern.compile("COST : (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setUpdateCost(Double.parseDouble(matcher.group(1)));
                }
            } else if (line.contains("Total execution time (ms)")) {
                pattern = Pattern.compile("Total execution time \\(ms\\): (\\d+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    updaterResult.setTotalTime(Long.parseLong(matcher.group(1)));
                }
            }
        }
        return updaterResult;
    }
}
