package helpers;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YmlConfReader {
    private final Map<String, Object> confMap;
    private static YmlConfReader INSTANCE;

    private YmlConfReader() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(ConstantProperties.configurationFileName); //This assumes that youryamlfile.yaml is on the classpath
        confMap = yaml.load(inputStream);
    }

    public static synchronized YmlConfReader getInstance()
    {
        if (INSTANCE == null)
        {   INSTANCE = new YmlConfReader();
        }
        return INSTANCE;
    }

    public List<String> getExperiencesToRun(){
        return (List<String>) confMap.get("experienceToRun");
    }
    public String weaverUrl(){
        return (String) confMap.get("weaverUrl");
    }
}
