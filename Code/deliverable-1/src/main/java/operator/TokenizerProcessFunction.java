package operator;

import lambda.LambdaInvokerUsingURLPayload;
import org.apache.flink.metrics.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Arrays;


public class TokenizerProcessFunction extends ProcessFunction<String, String> {
    private static boolean isBusy = false;

    private static final int MEASURE_INTERVAL_MS = 500;
    private static final int THRESHOLD = 3;

    private long lastMeasureTime;
    private int messageCount;

    private boolean shouldOffload;

    private Counter recordsProcessed;
    private static int totalElemProcessed = 0;
    private static int numOfElemProcessedByLambda = 0;
    private static int numOfElemProcessedByProcessFn = 0;


    @Override
    public void open(Configuration parameters) throws Exception {
        recordsProcessed = getRuntimeContext()
                .getMetricGroup()
                .getIOMetricGroup()
                .getNumRecordsInCounter();
    }

    public TokenizerProcessFunction() {
        lastMeasureTime = System.currentTimeMillis();
        messageCount = 0;
        shouldOffload = false;
        //Counter recordsProcessed = getRuntimeContext().getMetricGroup().getIOMetricGroup().getNumRecordsInCounter();
    }
    @Override
    public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
        long currentTime = System.currentTimeMillis();
        messageCount++;

        if (currentTime - lastMeasureTime >= MEASURE_INTERVAL_MS) {

            int inputRate = (int) recordsProcessed.getCount()  * 1000 / MEASURE_INTERVAL_MS;
            //int inputRate = messageCount  * 1000 / MEASURE_INTERVAL_MS;
            shouldOffload = inputRate > THRESHOLD;
            lastMeasureTime = currentTime;
            recordsProcessed.dec(recordsProcessed.getCount());
            //out.collect(new Tuple2<>(value, shouldOffload));
        }
        if(!shouldOffload) {
            isBusy = true;
            value = value.replaceAll("\\W"," ");
            String[] tokens = value.split("\\s+"); // split the string into tokens
            for (String token : tokens) {
                out.collect(token); // emit each token to downstream operators
            }
            Thread.sleep(1000);
            numOfElemProcessedByProcessFn+=1;
            isBusy = false;
            //System.out.println("Process Function Operator tokens" + Arrays.toString(tokens));
        }else{
            String lambda_tokens = LambdaInvokerUsingURLPayload.invoke_lambda(value);
            //System.out.println("Lambda Function Operator tokens" + lambda_tokens);
            out.collect(lambda_tokens);
            numOfElemProcessedByLambda+=1;
        }

        totalElemProcessed+=1;
        System.out.println("############################# Process function elements processed" + numOfElemProcessedByProcessFn);
        System.out.println("############################# lambda function elements processed" + numOfElemProcessedByLambda);
        System.out.println("############################# Total elements processed" + totalElemProcessed);
    }
}
