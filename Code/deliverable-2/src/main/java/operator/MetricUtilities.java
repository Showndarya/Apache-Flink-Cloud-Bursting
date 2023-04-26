package operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MetricUtilities {

    public static String getjobid() throws Exception {
        URL url = new URL("http://localhost:8081/jobs");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;

        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            System.out.println("The input line is" + inputLine);
            sb.append(inputLine);
        }
        in.close();
        System.out.println("The string being considered is" + sb);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(sb.toString());
        JsonNode jobs = jsonNode.get("jobs");
        String jobID = null;
        for (JsonNode job : jobs) {
            if (job.get("status").asText().equals("RUNNING")) {
                jobID = job.get("id").asText();
                System.out.println("Job ID: " + jobID);
            }
        }
        return jobID;
    }

    public static Double getInputRate(String jobID, String taskName) throws Exception {
        URL url = new URL("http://localhost:8081/jobs/" + jobID);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(sb.toString());

        String vertexId = null;
        for (JsonNode vertexNode : jsonNode.get("vertices")) {
            String vertexName = vertexNode.get("name").asText();
            if (vertexName.equals(taskName)) {
                vertexId = vertexNode.get("id").asText();
                break;
            }
        }

        url = new URL("http://localhost:8081/jobs/" + jobID + "/vertices/" + vertexId + "/metrics/" + "?get=0.numRecordsIn");
//        System.out.println("metrics url: http://localhost:8081/jobs/" + jobID + "/vertices/" + vertexId + "/metrics/");
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();


        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readTree(sb.toString());
        JsonNode metricNode = jsonNode.get(0);
        if(metricNode!=null&& metricNode.get("value") != null) {
            Double metricValue = metricNode.get("value").asDouble();
            if (metricValue != null) {
                return metricValue;
            }
        }

        return 0.0;
    }

    public static Long getBusyTime(String jobID, String taskName) throws Exception {
        URL url = new URL("http://localhost:8081/jobs/" + jobID);
        JsonNode jsonNode = OpenUrlConnection(url);
        String inputLine;

        String vertexId = null;
        for (JsonNode vertexNode : jsonNode.get("vertices")) {
            String vertexName = vertexNode.get("name").asText();
            if (vertexName.equals(taskName)) {
                vertexId = vertexNode.get("id").asText();
                break;
            }
        }

        url = new URL("http://localhost:8081/jobs/" + jobID + "/vertices/" + vertexId + "/metrics/" + "?get=0.busyTimeMsPerSecond");
        jsonNode = OpenUrlConnection(url);
        JsonNode metricNode = jsonNode.get(0);
        if(metricNode!=null&& metricNode.get("value") != null) {
            Long metricValue = metricNode.get("value").asLong();
            if (metricValue != null) {
                return metricValue;
            }
        }
        return 0L;
    }

    public static JsonNode OpenUrlConnection(URL url) throws IOException {
        HttpURLConnection con = openConnection(url);
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(sb.toString());
        return jsonNode;
    }

    private static HttpURLConnection openConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}
