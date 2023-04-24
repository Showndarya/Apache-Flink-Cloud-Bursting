package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class TokenizerProcessFunction extends ProcessAllWindowFunction<Tuple2<String, Boolean>, String, TimeWindow> {


    @Override
    public void process(Context context, Iterable<Tuple2<String, Boolean>> input, Collector<String> out) throws Exception {
        for (Tuple2<String, Boolean> element : input) {
            String value = element.f0;

                value = value.replaceAll("\\W"," ");
                String[] tokens = value.split("\\s+"); // split the string into tokens
                for (String token : tokens) {
                    out.collect(token); // emit each token to downstream operators
                }
                Thread.sleep(1000);
        }
    }
}
