package operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ControllerProcessFunction extends ProcessFunction<String, Tuple2<String,Boolean>> {

    private static final int MEASURE_INTERVAL_MS = 450;
    private static int INPUT_THRESHOLD = 1;
    private static double CPU_THRESHOLD = 0.6;

    private long lastMeasureTime;
    private int messageCount;
    private boolean shouldOffload;

    public ControllerProcessFunction() {
        lastMeasureTime = System.currentTimeMillis();
        messageCount = 0;
        shouldOffload = false;
    }

    @Override
    public void processElement(String value, ProcessFunction<String, Tuple2<String,Boolean>>.Context ctx, Collector<Tuple2<String,Boolean>> out) throws Exception {
        long currentTime = System.currentTimeMillis();
        messageCount++;

        if (messageCount == 1) {
            currentTime = lastMeasureTime;
        }

        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {
            String job_id = getjobid();
            double inputRate = getInputRate(job_id);
            double cputil = getCPUtil();
            shouldOffload = inputRate > INPUT_THRESHOLD || cputil > CPU_THRESHOLD;
            messageCount = 0;
            lastMeasureTime = currentTime;
        }

        out.collect(new Tuple2<>(value, shouldOffload));
    }

    private String getjobid() throws Exception {
        System.out.println("#################################### We are here");
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

    private double getInputRate(String jobID) throws Exception {
        String taskName = getRuntimeContext().getTaskName();

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
        double metricValue = metricNode.get("value").asDouble();
        return metricValue;
    }

    private double getCPUtil() throws Exception {
        URL url = new URL("http://localhost:8081/jobmanager/" + "metrics" + "?get=Status.JVM.CPU.Load");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(sb.toString());
        JsonNode metricNode = jsonNode.get(0);
        double cputilValue = metricNode.get("value").asDouble();
        return cputilValue;
    }

}

