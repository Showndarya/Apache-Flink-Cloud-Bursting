package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class TokenizerProcessFunction extends ProcessAllWindowFunction<Tuple3<String, Long, Boolean>, Tuple2<String,Long>, TimeWindow> {
    
    @Override
    public void process(Context context, Iterable<Tuple3<String, Long, Boolean>> input, Collector<Tuple2<String, Long>> out) throws Exception {
        if (input != null) {
            for (Tuple3<String, Long, Boolean> element : input) {
                String value = element.f0;
                value = value.replaceAll("\\W", " ");
                String[] tokens = value.split("\\s+"); // split the string into tokens
                for (String token : tokens) {
                    out.collect(Tuple2.of(token, element.f1)); // emit each token to downstream operators
                }

                Thread.sleep(1000);
            }
        }
    }
}
