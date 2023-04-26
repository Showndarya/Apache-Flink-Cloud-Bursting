package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import java.util.Random;

public class ControllerProcessFunction extends ProcessAllWindowFunction<Tuple2<String,Long>, Tuple3<String, Long, Boolean>, TimeWindow> {

    private static final int MEASURE_INTERVAL_MS = 1000;
    private static final int INPUT_THRESHOLD = 500;
    private static final double BUSY_TIME_THRESHOLD = 0.06;

    private long lastMeasureTime;
    private int messageCount;
    boolean shouldOffload = false;

    public ControllerProcessFunction() {
        lastMeasureTime = 0;
        messageCount = 0;
    }

    @Override
    public void process(Context context,  Iterable<Tuple2<String, Long>> values, Collector<Tuple3<String, Long, Boolean>> out) throws Exception {
        long currentTime = System.currentTimeMillis();
        Random rand = new Random();
        messageCount++;

        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {
            String job_id = MetricUtilities.getjobid();
            Double inputRate = MetricUtilities.getInputRate(job_id, getRuntimeContext().getTaskName());
            Long busyTimeMs = MetricUtilities.getBusyTime(job_id, getRuntimeContext().getTaskName());

            Double busyTimeRatio = (double) (busyTimeMs / 1000);

            double offloading_ratio = rand.nextDouble() * inputRate;
            if (busyTimeRatio > BUSY_TIME_THRESHOLD) {
                shouldOffload = true;
            } else {
                if (offloading_ratio > INPUT_THRESHOLD) {
                    shouldOffload = true;
                } else {
                    shouldOffload = false;
                }
            }

            messageCount = 0;
            lastMeasureTime = currentTime;
        }
        for(Tuple2<String, Long> value:values) {
            out.collect(new Tuple3<>(value.f0,value.f1, shouldOffload));
        }
    }
}
