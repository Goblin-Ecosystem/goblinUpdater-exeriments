package helpers;

import entities.Project;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class GitHelper {
    public static int gitClone(Project project) throws IOException, InterruptedException {
        String destinationPath = ConstantProperties.experienceTmpDir + project.getRepoName();
        File destinationDir = new File(destinationPath);
        if (destinationDir.exists() && destinationDir.isDirectory()) {
            return 0;
        } else {
            ProcessBuilder cloneBuilder = new ProcessBuilder("git", "clone", project.getGitUrl());
            cloneBuilder.directory(new File(ConstantProperties.experienceTmpDir));
            Process cloneProcess = cloneBuilder.start();
            return cloneProcess.waitFor();
        }
    }

    public static int gitCheckout(String branch, String projectPath) throws IOException, InterruptedException {
        ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout", branch);
        checkoutBuilder.directory(new File(projectPath));
        Process checkoutProcess = checkoutBuilder.start();
        return checkoutProcess.waitFor();
    }

    public static String getCommitId(String projectName) {
        ProcessBuilder processBuilderBuilder = new ProcessBuilder("git", "rev-parse", "HEAD");
        processBuilderBuilder.directory(new File(ConstantProperties.experienceTmpDir+projectName));
        return SystemHelper.execCommand(processBuilderBuilder).get(0);
    }

    public static String getLastCommitDate(String projectName) {
        ProcessBuilder processBuilderBuilder = new ProcessBuilder("git", "log", "-1", "--format=%cd", "--date=iso");
        processBuilderBuilder.directory(new File(ConstantProperties.experienceTmpDir+projectName));
        return SystemHelper.execCommand(processBuilderBuilder).get(0);
    }
}
