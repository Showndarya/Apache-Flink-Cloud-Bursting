package operator;

import lambda.LambdaInvokerUsingURLPayload;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class ControllerProcessFunction extends ProcessFunction<String, Tuple2<String, Boolean>> {

    private static final int MEASURE_INTERVAL_MS = 450;
    private static final int THRESHOLD = 1;

    private long lastMeasureTime;
    private int messageCount;

    private boolean shouldOffload;

    private Counter recordsProcessed;

    @Override
    public void open(Configuration parameters) throws Exception {
        recordsProcessed = getRuntimeContext()
                .getMetricGroup()
                .getIOMetricGroup()
                .getNumRecordsInCounter();
    }

    public ControllerProcessFunction() {
        lastMeasureTime = System.currentTimeMillis();
        messageCount = 0;
        shouldOffload = false;
        //Counter recordsProcessed = getRuntimeContext().getMetricGroup().getIOMetricGroup().getNumRecordsInCounter();
    }

    @Override
    public void processElement(String value, Context ctx, Collector<Tuple2<String, Boolean>> out) throws Exception {
        long currentTime = System.currentTimeMillis();
        messageCount++;
        //recordsProcessed.inc();
        if(messageCount == 1){
            currentTime = lastMeasureTime;
        }
        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {

            int inputRate = (int) recordsProcessed.getCount()  * 1000 / MEASURE_INTERVAL_MS;
            boolean shouldOffload;
            if(inputRate > THRESHOLD){
                shouldOffload = true;
            }else{
                shouldOffload = false;
            }
            messageCount = 0;
            lastMeasureTime = currentTime;
            recordsProcessed.dec(recordsProcessed.getCount());
            out.collect(new Tuple2<>(value, shouldOffload));
        } else {
            out.collect(new Tuple2<>(value, shouldOffload));
        }
        System.out.println("THE MESSAGE COUNT IS" + messageCount);
        System.out.println("################ THE offfloadfield IS" + shouldOffload);
        System.out.println("################ THE string IS" + value);


    }
}
