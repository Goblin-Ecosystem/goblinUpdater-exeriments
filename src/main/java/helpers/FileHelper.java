package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileHelper {

    public static void createDirectory(String directoryPath){
        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
        } catch (IOException e) {
            Logger.error("Failed to create directory: " + e.getMessage());
        }
    }

    public static void listStringToFile(List<String> strings, String filePath){
        try {
            Files.write(Paths.get(filePath), strings);
        } catch (IOException e) {
            Logger.error("Unable to write in file "+filePath+": " + e.getMessage());
        }
    }

    public static boolean fileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void moveFile(Path pathSource, Path pathTarget){
        try {
            // Déplacer le dossier
            Files.move(pathSource, pathTarget.resolve(pathSource.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Logger.error("Unable to move folder "+pathSource+"\n"+e);
        }
    }
}
