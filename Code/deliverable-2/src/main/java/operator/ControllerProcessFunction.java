package operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ControllerProcessFunction extends ProcessFunction<String, Tuple2<String, Boolean>> {

    private static final int MEASURE_INTERVAL_MS = 450;
    private static final int INPUT_THRESHOLD = 1;
    private static final double CPU_THRESHOLD = 0.6;

    private long lastMeasureTime;
    private int messageCount;
    boolean shouldOffload = false;
    private String URL_BASE="http://localhost:8081/";
    private String JOBS_QUERY_PARAM="jobs";

    public ControllerProcessFunction() {
        lastMeasureTime = 0;
        messageCount = 0;
    }

    @Override
    public void processElement(String value, Context ctx, Collector<Tuple2<String, Boolean>> out) throws Exception {
        long currentTime = ctx.timerService().currentProcessingTime();
        Random rand = new Random();
        messageCount++;

        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {
            String job_id = getjobid();
            Double inputRate = getInputRate(job_id);
            double offloading_ratio = rand.nextDouble()*inputRate;
            Double cputil = getCPUtil();
            if(cputil > CPU_THRESHOLD){
                shouldOffload = true;
            }else{
                if(offloading_ratio < INPUT_THRESHOLD){
                    shouldOffload = true;
                }else{
                    shouldOffload = false;
                }
            }
            messageCount = 0;
            lastMeasureTime = currentTime;
            out.collect(new Tuple2<>(value, shouldOffload));
        } else {
            Double cputil = getCPUtil();
            String job_id = getjobid();
            Double inputRate = getInputRate(job_id);
            double offloading_ratio = rand.nextDouble()*inputRate;
            if(cputil > CPU_THRESHOLD){
                shouldOffload = true;
            }else{
                if(offloading_ratio < INPUT_THRESHOLD){
                    shouldOffload = true;
                }else{
                    shouldOffload = false;
                }
            }
            out.collect(new Tuple2<>(value, shouldOffload));

        }
    }

    private String getjobid() throws Exception {
        URL url = new URL(URL_BASE+JOBS_QUERY_PARAM);
        JsonNode jsonNode = OpenUrlConnection(url);
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

    private Double getInputRate(String jobID) throws Exception {
        String taskName = getRuntimeContext().getTaskName();

        URL url = new URL(URL_BASE+JOBS_QUERY_PARAM+"/" + jobID);
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

        url = new URL(URL_BASE+JOBS_QUERY_PARAM+"/" + jobID + "/vertices/" + vertexId + "/metrics/" + "?get=0.numRecordsIn");
        jsonNode = OpenUrlConnection(url);
        JsonNode metricNode = jsonNode.get(0);
        if(metricNode!=null&& metricNode.get("value") != null) {
            Double metricValue = metricNode.get("value").asDouble();
            if (metricValue != null) {
                return metricValue;
            }
        }

        return 0.0;
    }

    private double getCPUtil() throws Exception {
        URL url = new URL(URL_BASE+"jobmanager/" + "metrics" + "?get=Status.JVM.CPU.Load");
        JsonNode jsonNode = OpenUrlConnection(url);
        JsonNode metricNode = jsonNode.get(0);

        if(metricNode!=null&& metricNode.get("value") != null) {
            Double cputilValue = metricNode.get("value").asDouble();
            if (cputilValue != null) {
                return cputilValue;
            }
        }

        return 0.0;
    }

    private JsonNode OpenUrlConnection(URL url) throws IOException {
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

