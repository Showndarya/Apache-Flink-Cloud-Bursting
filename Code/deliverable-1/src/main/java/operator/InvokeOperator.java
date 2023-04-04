package operator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lambda.LambdaInvokerUsingURLPayload;
import nexmark.NexmarkConfiguration;
import nexmark.generator.GeneratorConfig;
import nexmark.model.Event;
import nexmark.source.EventDeserializer;
import nexmark.source.NexmarkSourceFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.operators.util.SimpleVersionedListState;
import org.apache.flink.util.Collector;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvokeOperator extends ProcessFunction<String,String> implements CheckpointedFunction{
    /**
     * Process one element from the input stream.
     *
     * <p>This function can output zero or more elements using the {@link Collector} parameter and
     * also update internal state or set timers using the {@link Context} parameter.
     *
     * @param value The input value.
     * @param ctx   A {@link Context} that allows querying the timestamp of the element and getting a
     *
     *              valid during the invocation of this method, do not store it.
     * @param out   The collector for returning result values.
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the
     *                   operation to fail and may trigger recovery.
     */

    private final int threshold;
    /**
     * Todo find suitable implementation
     * How to create a list state
     * alternative: use value state to store a java list
     * might use other liststate implimentation
     */
    private transient ListState<String> state;
    private List<String> bufferedElements;
    public InvokeOperator(int threshold) {
        this.threshold = threshold;
        this.bufferedElements = new ArrayList<>();
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<String> descriptor =
                new ListStateDescriptor<>("lambdaBuffer", String.class);

        state = context.getOperatorStateStore().getListState(descriptor);

        if (context.isRestored()) {
            for (String element : state.get()) {
                bufferedElements.add(element);
            }
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        state.clear();
        for (String element : bufferedElements) {
            state.add(element);
        }
    }

    @Override
    public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
        /**
         * add string to state
         */

        bufferedElements.add(value);
        if (bufferedElements.size() >= threshold) {
            String payload = getPayload(bufferedElements);
            String jsonResult = LambdaInvokerUsingURLPayload.invoke_lambda(payload);
            List<String> strings = getResultFromJson(jsonResult);
            for (String i : strings) {
                out.collect(i);
            }
            bufferedElements.clear();
        }

    }

    private static String getPayload(List<String> list){
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        return jsonString;
    }

    private static List<String> getResultFromJson(String jsonString){
        Gson gson = new Gson();
        List<String> result = gson.fromJson(jsonString, new TypeToken<List<String>>() {
        }.getType());
        return result;
    }

    public static void main(String[] args) throws Exception{
        // create a Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.disableOperatorChaining();

        //env.enableCheckpointing(100);
        NexmarkConfiguration nexmarkConfiguration = new NexmarkConfiguration();
        nexmarkConfiguration.bidProportion = 46;
        GeneratorConfig generatorConfig = new GeneratorConfig(
                nexmarkConfiguration, System.currentTimeMillis(), 1, 1000, 1);

        // generate a stream of random strings
        DataStream<String> randomStrings = env.addSource(new NexmarkSourceFunction<>(
                generatorConfig,
                (EventDeserializer<String>) Event::toString,
                BasicTypeInfo.STRING_TYPE_INFO));

        DataStream<String> invoker = randomStrings.process(new InvokeOperator(10));

        FileSink sink=CustomedFileSink.getSink();
        invoker.sinkTo(sink);

        env.execute("Flink Pipeline Tokenization");
    }


}
