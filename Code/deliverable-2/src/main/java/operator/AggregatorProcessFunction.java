package operator;

import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AggregatorProcessFunction extends ProcessAllWindowFunction<String, String, TimeWindow> {

    @Override
    public void process(Context context, Iterable<String> tokens, Collector<String> out) {
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            builder.append(token).append(" ");
        }
        out.collect(builder.toString().trim());
    }
}

