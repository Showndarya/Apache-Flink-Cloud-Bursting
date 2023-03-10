package operator;

import lambda.LambdaInvokerUsingURLPayload;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class TokenizerProcessFunction extends ProcessFunction<String, String> {
    private static boolean isBusy = false;
    private static int totalElemProcessed = 0;
    private static int numOfElemProcessedByLambda = 0;
    private static int numOfElemProcessedByProcessFn = 0;
    @Override
    public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
        if(!isBusy) {
            isBusy = true;
            value = value.replaceAll("\\W"," ");
            String[] tokens = value.split("\\s+"); // split the string into tokens
            for (String token : tokens) {
                out.collect(token); // emit each token to downstream operators
            }
            Thread.sleep(1000);
            numOfElemProcessedByProcessFn+=1;
            isBusy = false;
            System.out.println("Process Function Operator tokens" + Arrays.toString(tokens));
        }
        else{
            String lambda_tokens = LambdaInvokerUsingURLPayload.invoke_lambda(value);
            System.out.println("Lambda Function Operator tokens" + lambda_tokens);
            out.collect(lambda_tokens);
            numOfElemProcessedByLambda+=1;
        }

        totalElemProcessed+=1;
    }
}
