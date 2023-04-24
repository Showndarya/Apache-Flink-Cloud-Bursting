package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class TokenizerProcessFunction extends ProcessAllWindowFunction<Tuple2<String, Boolean>, String, TimeWindow> {

    private static int numOfElemProcessedByProcessFn = 0;

    @Override
    public void process(Context context, Iterable<Tuple2<String, Boolean>> input, Collector<String> out) throws Exception {
        for (Tuple2<String, Boolean> element : input) {
            String value = element.f0;
            boolean shouldOffload = element.f1;

//            if (shouldOffload) {
//                // Offload to AWS Lambda
//                // ...
//                String lambda_tokens = LambdaInvokerUsingURLPayload.invoke_lambda(value);
//                System.out.println("Lambda Function Operator tokens" + lambda_tokens);
//                out.collect(lambda_tokens);
//                numOfElemProcessedByLambda+=1;
//            } else {
                // Tokenize in the process function
                // ...
//                isBusy = true;
                value = value.replaceAll("\\W"," ");
                String[] tokens = value.split("\\s+"); // split the string into tokens
                for (String token : tokens) {
                    out.collect(token); // emit each token to downstream operators
                }
                Thread.sleep(1000);
                numOfElemProcessedByProcessFn+=1;
//                isBusy = false;
                System.out.println("Process Function Operator tokens" + Arrays.toString(tokens));
//            }
//            totalElemProcessed+=1;
            System.out.println("############################# Process function elements processed" + numOfElemProcessedByProcessFn);
//            System.out.println("############################# lambda function elements processed" + numOfElemProcessedByLambda);
//            System.out.println("############################# Total elements processed" + totalElemProcessed);
        }
    }
}
