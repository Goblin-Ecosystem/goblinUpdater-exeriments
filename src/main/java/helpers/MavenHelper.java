package helpers;

import entities.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;

public class MavenHelper {
    public static boolean checkPomExist(String folderPath){
        File folder = new File(folderPath);
        // Check if the folder exists and is a directory
        if (folder.exists() && folder.isDirectory()) {
            // Create a File object for the pom.xml file
            File pomFile = new File(folder, "pom.xml");
            return pomFile.exists() && pomFile.isFile();
        } else {
            return false;
        }
    }

    public static boolean hasModules(String projectPath){
        int count = countPomFiles(new File(projectPath));
        return count > 1;
    }

    public static int buildProject(String projectPath) throws MavenInvocationException {
        return runMavenGoal(projectPath, "compile");
    }

    public static int testProject(String projectPath) throws MavenInvocationException {
        return runMavenGoal(projectPath, "test");
    }

    public static Set<Dependency> getProjectDirectDependencies(Path projectPath) {
        Set<Dependency> resultList = new HashSet<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(projectPath.toFile());
        processBuilder.command("mvn", "dependency:list", "-DexcludeTransitive=true");
        List<String> lines = SystemHelper.execCommand(processBuilder);
        if(lines != null) {
            Iterator<String> lineIterator = lines.iterator();
            String line = lineIterator.next();
            while (lineIterator.hasNext() && !line.contains("The following files have been resolved:")) {
                line = lineIterator.next();
            }
            while (lineIterator.hasNext() && !line.contains("BUILD SUCCESS")) {
                line = lineIterator.next();
                if (line.matches(".*:.+:.+:.+:.+")) {
                    String[] parts = line.split(":");
                    String groupId = parts[0].split("]")[1].trim();
                    String artifactId = parts[1].trim();
                    String version = parts[3].trim();
                    resultList.add(new Dependency(groupId, artifactId, version));
                }
            }
        }
        return resultList;
    }

    private static int countPomFiles(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countPomFiles(file);
                } else if (file.getName().equals("pom.xml")) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int runMavenGoal(String projectPath, String goal) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectPath, "pom.xml"));
        request.setGoals(Collections.singletonList(goal));
        request.setOutputHandler(new NullOutputHandler());

        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("/usr/share/maven"));
        invoker.setLocalRepositoryDirectory(new File("/root/.m2/repository"));

        InvocationResult result = invoker.execute(request);
        return result.getExitCode();
    }

    public static void changeDependenciesVersion(Set<Dependency> versionsToChange, String projectPath){
        String pomFilePath = projectPath+"/pom.xml";
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pomFilePath));

            for (org.apache.maven.model.Dependency existingDep : model.getDependencies()) {
                for (Dependency newDep : versionsToChange) {
                    if (existingDep.getGroupId().equals(newDep.getGroupId()) &&
                            existingDep.getArtifactId().equals(newDep.getArtifactId())) {
                        existingDep.setVersion(newDep.getVersion());
                    }
                }
            }

            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(pomFilePath), model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void replaceAllDependencies(Set<Dependency> dependencies, String projectPath) {
        String pomFilePath = projectPath+"/pom.xml";
        if(dependencies.size() == 0){
            // No solution case
            return;
        }
        Set<Dependency> transitivesDependencies = new HashSet<>(dependencies);
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(projectPath+"/pom.xml"));
            // Change existing versions
            for (org.apache.maven.model.Dependency existingDep : model.getDependencies()) {
                for (Dependency newDep : dependencies) {
                    if (existingDep.getGroupId().equals(newDep.getGroupId()) &&
                            existingDep.getArtifactId().equals(newDep.getArtifactId())) {
                        existingDep.setVersion(newDep.getVersion());
                        transitivesDependencies.remove(newDep);
                    }
                }
            }
            // Add transitive to root
            for (Dependency transitiveDep : transitivesDependencies) {
                org.apache.maven.model.Dependency newDep = new org.apache.maven.model.Dependency();
                newDep.setGroupId(transitiveDep.getGroupId());
                newDep.setArtifactId(transitiveDep.getArtifactId());
                newDep.setVersion(transitiveDep.getVersion());
                model.addDependency(newDep);
            }
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(pomFilePath), model);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    public static int countDependencies(String projectPath){
        String pomFilePath = projectPath+"/pom.xml";
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pomFilePath));
            return model.getDependencies().size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Map<String, Boolean> runBuildAndTest(String projectPath){
        boolean isBuild = false;
        boolean isTest = false;
        try {
            int buildExitCode = MavenHelper.buildProject(projectPath);
            if ( buildExitCode == 0) {
                isBuild = true;
                int testExitCode = MavenHelper.testProject(projectPath);
                if ( testExitCode == 0) {
                    isTest = true;
                }
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("build", isBuild);
        result.put("test", isTest);
        return result;
    }
}

class NullOutputHandler implements InvocationOutputHandler {
    @Override
    public void consumeLine(String line) {
        // Do nothing, discard the output
    }
}
