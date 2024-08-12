package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SystemHelper {

    public static List<String> execCommand(ProcessBuilder processBuilder) {
        List<String> output = new ArrayList<>();
        try {
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = stdInput.lines().collect(Collectors.toList());

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Logger.error("Error on command: " + String.join(" ", processBuilder.command()) + "\n Error output: \n" + String.join("\n", output));
                return null;
            }
            stdInput.close();
        } catch (IOException | InterruptedException e) {
            Logger.error(
                    "Unable to run command: " + String.join(" ", processBuilder.command()) + "\n" + e.getMessage());
        }
        return output;
    }
}
