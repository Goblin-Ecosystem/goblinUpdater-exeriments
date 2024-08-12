import entities.Configuration;
import experiments.*;
import helpers.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Logger.info("Start");
        FileHelper.createDirectory(ConstantProperties.experienceTmpDir);
        FileHelper.createDirectory(ConstantProperties.outDirectory);
        if (YmlConfReader.getInstance().getExperiencesToRun().contains("datasetGenerator")) {
            Logger.info("Dataset info generation");
            DatasetGenerator.run();
        }
        // Run updater
        if (YmlConfReader.getInstance().getExperiencesToRun().contains("runUpdater")) {
            Logger.info("Run updater");
            List<Configuration> configs = new ArrayList<>();
            configs.add(new Configuration("conf_1_global", false));
            configs.add(new Configuration("conf_2_global", false));
            configs.add(new Configuration("conf_3_global", false));
            configs.add(new Configuration("conf_4_local", false));
            configs.add(new Configuration("conf_5_local", true));
            configs.add(new Configuration("conf_5_global", true));
            //configs.add(new Configuration("conf_6_global", true));
            for (Configuration config : configs) {
                GoblinUpdaterWorker.executeUpdater(config);
            }
            FileHelper.moveFile(Paths.get(ConstantProperties.updaterDataFolder), Paths.get(ConstantProperties.outDirectory));
        }
        if (YmlConfReader.getInstance().getExperiencesToRun().contains("compileAndTest")) {
            Logger.info("Run compile and test");
            CompileAndTestWorker.run(new Configuration("conf_1_global", false));
            CompileAndTestWorker.run(new Configuration("conf_2_global", false));
            CompileAndTestWorker.run(new Configuration("conf_3_global", false));
            CompileAndTestWorker.run(new Configuration("conf_4_local", false));
        }
        if (YmlConfReader.getInstance().getExperiencesToRun().contains("naiveUpdate")) {
            Logger.info("Naive updater comparison");
            NaiveUpdateWorker.run(new Configuration("conf_2_global", false));
        }
        if (YmlConfReader.getInstance().getExperiencesToRun().contains("compareConfQualityAndCost")) {
            Logger.info("Compare Conf Quality And Cost");
            CompareConfQualityAndCost.run(new Configuration("conf_5_local", false), new Configuration("conf_5_global", false));
            CompareConfQualityAndCost.run(new Configuration("conf_4_local", false), new Configuration("conf_2_global", false));
            CompareConfQualityAndCost.run(new Configuration("conf_1_global", false), new Configuration("conf_2_global", false));
        }
        Logger.info("End");
    }
}
