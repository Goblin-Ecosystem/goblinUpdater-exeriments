package entities;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static helpers.ConstantProperties.updaterFolder;

public class Configuration {
    private final String name;
    private final String path;
    private final boolean cleanWeaver;
    private Set<Metric> metrics;

    public Configuration(String name, boolean cleanWeaver) {
        this.name = name;
        this.path = updaterFolder+name+".yml";
        this.cleanWeaver = cleanWeaver;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean cleanWeaver() {
        return cleanWeaver;
    }

    public Set<Metric> getMetrics(){
        if (metrics != null){
            return metrics;
        }
        Yaml yaml = new Yaml();
        Set<Metric> metricSet = new HashSet<>();

        try (InputStream inputStream = new FileInputStream(this.path)) {
            Map<String, Object> obj = yaml.load(inputStream);
            List<Map<String, Object>> metricsList = (List<Map<String, Object>>) obj.get("metrics");

            for (Map<String, Object> metricMap : metricsList) {
                String name = (String) metricMap.get("metric");
                double coef = ((Number) metricMap.get("coef")).doubleValue();
                Metric metric = new Metric(name, coef);
                metricSet.add(metric);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.metrics = metricSet;
        return metrics;
    }
}
