import operator.AggregatorProcessFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class AggregatorProcessFunctionTest {

    @Test
    public void testProcess() throws Exception {
        // Create a mock Collector object
        Collector<Tuple2<String, Long>> collector = Mockito.mock(Collector.class);

        // Create some sample input data
        List<Tuple2<String, Long>> input = new ArrayList<>();
        input.add(new Tuple2<>("foo", 1L));
        input.add(new Tuple2<>("bar", 2L));
        input.add(new Tuple2<>("baz", 3L));

        // Create a ProcessAllWindowFunction object
        ProcessAllWindowFunction<Tuple2<String, Long>, Tuple2<String,Long>, TimeWindow> function = new AggregatorProcessFunction();

        // Invoke the process() method with the mock collector and sample input
        function.process(Mockito.mock(ProcessAllWindowFunction.Context.class), input, collector);

        // Verify that the output is correct
        Mockito.verify(collector).collect(new Tuple2<>("foo", 1L));
        Mockito.verify(collector).collect(new Tuple2<>("bar", 2L));
        Mockito.verify(collector).collect(new Tuple2<>("baz", 3L));
    }

    @Test
    public void testEmptyInput() throws Exception {
        // Create a mock Collector object
        Collector<Tuple2<String, Long>> collector = Mockito.mock(Collector.class);

        // Create an empty input list
        List<Tuple2<String, Long>> input = new ArrayList<>();

        // Create a ProcessAllWindowFunction object
        ProcessAllWindowFunction<Tuple2<String, Long>, Tuple2<String,Long>, TimeWindow> function = new AggregatorProcessFunction();

        // Invoke the process() method with the mock collector and empty input
        function.process(Mockito.mock(ProcessAllWindowFunction.Context.class), input, collector);

        // Verify that no output is generated
        Mockito.verifyZeroInteractions(collector);
    }
}
