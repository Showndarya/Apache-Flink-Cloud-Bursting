import operator.TokenizerProcessFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class TokenizerProcessFunctionTest {

    @Test
    public void testProcess() throws Exception {
        // create mock objects for the Context and Collector interfaces
        ProcessAllWindowFunction.Context mockContext = mock(ProcessAllWindowFunction.Context.class);
        Collector<Tuple2<String, Long>> mockCollector = mock(Collector.class);

        // create test data
        Tuple3<String, Long, Boolean> input1 = new Tuple3<>("Hello, world!", 1L, true);
        Tuple3<String, Long, Boolean> input2 = new Tuple3<>("Goodbye, world!", 2L, false);
        List<Tuple3<String, Long, Boolean>> inputList = Arrays.asList(input1, input2);

        // create expected output
        Tuple2<String, Long> expectedOutput1 = new Tuple2<>("Hello", 1L);
        Tuple2<String, Long> expectedOutput2 = new Tuple2<>("world", 1L);
        Tuple2<String, Long> expectedOutput3 = new Tuple2<>("Goodbye", 2L);
        Tuple2<String, Long> expectedOutput4 = new Tuple2<>("world", 2L);
        List<Tuple2<String, Long>> expectedOutputList = Arrays.asList(expectedOutput1, expectedOutput2, expectedOutput3, expectedOutput4);

        // create instance of TokenizerProcessFunction
        TokenizerProcessFunction tokenizer = new TokenizerProcessFunction();

        // call the process method with the test data
        tokenizer.process(mockContext, inputList, mockCollector);

        // verify that the expected output was collected by the Collector
        verify(mockCollector, times(4)).collect(any(Tuple2.class));
        verify(mockCollector).collect(expectedOutput1);
        verify(mockCollector).collect(expectedOutput2);
        verify(mockCollector).collect(expectedOutput3);
        verify(mockCollector).collect(expectedOutput4);
    }

    @Test
    public void testProcessWithEmptyInput() throws Exception {
        // create mock objects for the Context and Collector interfaces
        ProcessAllWindowFunction.Context mockContext = mock(ProcessAllWindowFunction.Context.class);
        Collector<Tuple2<String, Long>> mockCollector = mock(Collector.class);

        // create empty input list
        List<Tuple3<String, Long, Boolean>> inputList = new ArrayList<>();

        // create instance of TokenizerProcessFunction
        TokenizerProcessFunction tokenizer = new TokenizerProcessFunction();

        // call the process method with the empty input list
        tokenizer.process(mockContext, inputList, mockCollector);

        // verify that nothing was collected by the Collector
        verify(mockCollector, never()).collect(any(Tuple2.class));
    }

    @Test
    public void testProcessWithNullInput() throws Exception {
        // create mock objects for the Context and Collector interfaces
        ProcessAllWindowFunction.Context mockContext = mock(ProcessAllWindowFunction.Context.class);
        Collector<Tuple2<String, Long>> mockCollector = mock(Collector.class);

        // create null input
        Iterable<Tuple3<String, Long, Boolean>> input = null;

        // create instance of TokenizerProcessFunction
        TokenizerProcessFunction tokenizer = new TokenizerProcessFunction();

        // call the process method with the null input
        tokenizer.process(mockContext, input, mockCollector);

        // verify that nothing was collected by the Collector
        verify(mockCollector, never()).collect(any(Tuple2.class));
    }

    @Test
    public void testProcessWithSingleToken() throws Exception {
        // create mock objects for the Context and Collector interfaces
        ProcessAllWindowFunction.Context mockContext = mock(ProcessAllWindowFunction.Context.class);
        Collector<Tuple2<String, Long>> mockCollector = mock(Collector.class);

        // create input with a single token
        Tuple3<String, Long, Boolean> input = new Tuple3<>("hello", 1L, true);
        List<Tuple3<String, Long, Boolean>> inputList = Collections.singletonList(input);

        // create expected output
        Tuple2<String, Long> expectedOutput = new Tuple2<>("hello", 1L);
        List<Tuple2<String, Long>> expectedOutputList = Collections.singletonList(expectedOutput);

        // create instance of TokenizerProcessFunction
        TokenizerProcessFunction tokenizer = new TokenizerProcessFunction();

        // call the process method with the single-token input
        tokenizer.process(mockContext, inputList, mockCollector);

        // verify that the expected output was collected by the Collector
        verify(mockCollector, times(1)).collect(any(Tuple2.class));
        verify(mockCollector).collect(expectedOutput);
    }

    @Test
    public void testProcessWithPunctuationOnly() throws Exception {
        // create mock objects for the Context and Collector interfaces
        ProcessAllWindowFunction.Context mockContext = mock(ProcessAllWindowFunction.Context.class);
        Collector<Tuple2<String, Long>> mockCollector = mock(Collector.class);

        // create input with only punctuation
        Tuple3<String, Long, Boolean> input = new Tuple3<>(".!?;,", 1L, true);
        List<Tuple3<String, Long, Boolean>> inputList = Collections.singletonList(input);

        // create instance of TokenizerProcessFunction
        TokenizerProcessFunction tokenizer = new TokenizerProcessFunction();

        // call the process method with the input
        tokenizer.process(mockContext, inputList, mockCollector);

        // verify that nothing was collected by the Collector
        verifyZeroInteractions(mockCollector);
    }


}
