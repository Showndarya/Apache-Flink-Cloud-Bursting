package operator;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AggregatorProcessFunction extends ProcessAllWindowFunction<Tuple2<String, Long>, Tuple2<String,Long>, TimeWindow> {

    @Override
    public void process(Context context, Iterable<Tuple2<String, Long>> tokens, Collector<Tuple2<String, Long>> out) {
        for (Tuple2<String, Long> token : tokens) {
            out.collect(Tuple2.of(token.f0,token.f1));
        }
    }
}

