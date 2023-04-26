package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class TokenizerProcessFunction extends ProcessAllWindowFunction<Tuple2<String, Long>, Tuple2<String, Long>, TimeWindow> {

    @Override
    public void process(Context context, Iterable<Tuple2<String, Long>> input, Collector<Tuple2<String, Long>> out) throws Exception {
        for (Tuple2<String, Long> element : input) {
            String value = element.f0;
            value = value.replaceAll("\\W"," ");
            String[] tokens = value.split("\\s+"); // split the string into tokens
            for (String token : tokens) {
                out.collect(new Tuple2<String, Long>(token, element.f1)); // emit each token to downstream operators
            }
            Thread.sleep(1000);
        }
    }
}
