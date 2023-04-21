package operator;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class AggregatorProcessFunction extends ProcessFunction<String, String> {

    @Override
    public void processElement(String tokens, Context ctx, Collector<String> out) {
        out.collect(tokens);
    }
}