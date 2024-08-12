package helpers;

import entities.Dependency;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaverHelper {

    public static void removeAddedValues(){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(YmlConfReader.getInstance().weaverUrl()+"/addedValues"))
                .DELETE()
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Logger.error(e.getMessage());
        }
    }

    public static Set<Dependency> getDependencyGraph(Set<Dependency> directDependencies){
        String apiRoute = "/graph/traversing";

        JSONObject bodyJsonObject = new JSONObject();

        JSONArray startReleasesArray = new JSONArray();
        startReleasesArray.addAll(directDependencies.stream().map(Dependency::gav).collect(Collectors.toSet()));
        bodyJsonObject.put("startReleasesGav", startReleasesArray);
        bodyJsonObject.put("libToExpendsGa", new JSONArray());
        bodyJsonObject.put("filters", new JSONArray());
        bodyJsonObject.put("addedValues", new JSONArray());
        JSONObject resultObject = executeQuery(bodyJsonObject, apiRoute);
        Set<Dependency> releases = new HashSet<>();
        JSONArray nodes = (JSONArray) resultObject.get("nodes");
        for (Object node : nodes) {
            JSONObject jsonObject = (JSONObject) node;
            String id = (String) jsonObject.get("id");
            String nodeType = (String) jsonObject.get("nodeType");
            if(nodeType.equals("RELEASE") && !id.equals("ROOT")){
                Dependency dep = jsonNodeToDependency(jsonObject);
                if (dep != null){
                    releases.add(dep);
                }
            }
        }
        return releases;
    }

    public static Set<Dependency> getArtifactRelease(String groupId, String artifactId) {
        String apiRoute = "/artifact/releases";
        JSONObject bodyJsonObject = new JSONObject();
        bodyJsonObject.put("groupId", groupId);
        bodyJsonObject.put("artifactId", artifactId);
        bodyJsonObject.put("addedValues", new JSONArray());

        Set<Dependency> releases = new HashSet<>();
        JSONObject resultObject =  executeQuery(bodyJsonObject, apiRoute);
        JSONArray nodes = (JSONArray) resultObject.get("nodes");
        if(nodes != null) {
            for (Object node : nodes) {
                JSONObject jsonObject = (JSONObject) node;
                Dependency dep = jsonNodeToDependency(jsonObject);
                if (dep != null) {
                    releases.add(dep);
                }
            }
        }
        return releases;
    }

    private static Dependency jsonNodeToDependency(JSONObject nodeJsonObject){
        String id = (String) nodeJsonObject.get("id");
        String[] gav = id.split(":");
        String version = (String) nodeJsonObject.get("version");
        long timestamp = (long) nodeJsonObject.get("timestamp");
        if(version.split("\\.").length > 1) {
            Dependency dependency = new Dependency(gav[0], gav[1], version);
            dependency.setTimestamp(timestamp);
            return dependency;
        }
        return null;
    }

    private static JSONObject executeQuery(JSONObject bodyJsonObject, String apiRoute) {
        try {
            URL url = new URL(YmlConfReader.getInstance().weaverUrl() + apiRoute);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json; utf-8");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);

            byte[] out = bodyJsonObject.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            if (http.getResponseCode() == 200) {
                JSONParser jsonParser = new JSONParser();
                return (JSONObject) jsonParser.parse(
                        new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8));
            } else {
                Logger.error("API error code: " + http.getResponseCode() + "\n");
            }
            http.disconnect();
        } catch (IOException | org.json.simple.parser.ParseException e) {
            Logger.fatal("Unable to connect to API:\n" + e.getMessage());
        }
        return null;
    }
}
