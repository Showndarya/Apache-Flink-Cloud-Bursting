package operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ControllerProcessFunction extends ProcessAllWindowFunction<String, Tuple2<String, Boolean>, TimeWindow> {

    private static final int MEASURE_INTERVAL_MS = 450;
    private static final int INPUT_THRESHOLD = 2000;
    private static final double CPU_THRESHOLD = 0.06;

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
    public void process(Context context, Iterable<String> values, Collector<Tuple2<String, Boolean>> out) throws Exception {
        long currentTime = System.currentTimeMillis();
        Random rand = new Random();
        messageCount++;

        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {
            String job_id = getjobid();
            Double inputRate = getInputRate(job_id);
            Long busyTimeMs = getBusyTime(job_id);

            Double busyTimeRatio = Math.max((double) (busyTimeMs/1000), 0.8);
            System.out.println("busy time ratio" + busyTimeRatio);

            double offloading_ratio = rand.nextDouble()*inputRate;
            if(busyTimeRatio > CPU_THRESHOLD) {
                shouldOffload = true;
            } else {
                if(offloading_ratio < INPUT_THRESHOLD) {
                    shouldOffload = true;
                } else {
                    shouldOffload = false;
                }
            }
            messageCount = 0;
            lastMeasureTime = currentTime;
            for(String value:values) {
                out.collect(new Tuple2<>(value, shouldOffload));
            }
        } else {
            for(String value:values) {
                out.collect(new Tuple2<>(value, shouldOffload));
            }
        }
    }


    private String getjobid() throws Exception {
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

    private Long getBusyTime(String jobID) throws Exception {
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

        url = new URL(URL_BASE+JOBS_QUERY_PARAM+"/" + jobID + "/vertices/" + vertexId + "/metrics/" + "?get=0.busyTimeMsPerSecond");
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


    private Double getInputRate(String jobID) throws Exception {
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
        System.out.println("metrics url: http://localhost:8081/jobs/" + jobID + "/vertices/" + vertexId + "/metrics/");
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


