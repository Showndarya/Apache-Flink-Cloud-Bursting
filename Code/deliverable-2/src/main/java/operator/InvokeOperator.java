package operator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entity.TokenObject;
import lambda.LambdaInvokerUsingURLPayload;
import nexmark.NexmarkConfiguration;
import nexmark.generator.GeneratorConfig;
import nexmark.model.Event;
import nexmark.source.EventDeserializer;
import nexmark.source.NexmarkSourceFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.shaded.asm9.org.objectweb.asm.TypeReference;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InvokeOperator extends ProcessFunction<String,String> implements CheckpointedFunction{
    @Override
    public void close() throws Exception {
        super.close();
        fileWriter.write('\n');
        fileWriter.flush();
        fileWriter.close();
        fileWriter1.write('\n');
        fileWriter1.flush();
        fileWriter1.close();
    }

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

    private  final String latencyName;
    private  final String throughputName;
    /**
     * Todo find suitable implementation
     * How to create a list state
     * alternative: use value state to store a java list
     * might use other liststate implimentation
     */
    private transient ListState<String> state;
    private List<String> bufferedElements;

    private transient FileWriter fileWriter;
    private transient FileWriter fileWriter1;

    private double startTime;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        System.out.println(this.latencyName);
        System.out.println(this.throughputName);
        fileWriter=new FileWriter(latencyName,true);
        fileWriter1 = new FileWriter(throughputName, true);

    }

    public InvokeOperator(int threshold, String latencyName, String throughputName) throws IOException {
        this.threshold = threshold;
        this.latencyName = latencyName;
        this.throughputName = throughputName;
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
        if(bufferedElements.size()==0){
            startTime=System.currentTimeMillis();
        }
        bufferedElements.add(value);
        if (bufferedElements.size() >= threshold) {
            String payload = getPayload(bufferedElements);
            String jsonResult = LambdaInvokerUsingURLPayload.invoke_lambda(payload);




            List<String> strings = getResultFromJsonJava(jsonResult);
            for (String i : strings) {
                out.collect(i);
            }
            double latency = (System.currentTimeMillis()-startTime);
            System.out.println(latency+"---------------------------------------------------------------------------------------");
            fileWriter.write(latency+",");
            fileWriter.flush();
            fileWriter1.write((threshold/(latency/1000))+",");
            fileWriter1.flush();
            bufferedElements.clear();
        }

    }

    private static String getPayload(List<String> list){
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        jsonString.substring(1, jsonString.length() - 1);
        return jsonString;
    }

    private  static List<String> getResultFromJsonPython(String jsonString){
        Gson gson = new Gson();
        List list = gson.fromJson(jsonString, List.class);
        return list;
    }

    private static List<String> getResultFromJsonJava(String jsonString){
//        System.out.println(jsonString);
        Gson gson = new Gson();

        jsonString=jsonString.replace("\"[","[");
        jsonString=jsonString.replace("]\"","]");

        List<List<String>> tokenObject = gson.fromJson(jsonString, List.class);

//        System.out.println(tokenObject);
        List<String> result = new ArrayList<>();
        for(List<String> i:tokenObject){
            result.addAll(i);
        }
        result.remove("");
//        System.out.println(result);
        return result;
    }

    public static void main(String[] args) throws Exception{
        int[] arr = new int[]{10};
        for(int j:arr) {
            for (int i = 0; i < 1; i++) {
                // create a Flink execution environment
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                env.setParallelism(1);
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

                DataStream<String> invoker = randomStrings.process(new InvokeOperator(j, "new warm start latency"+j+".csv","new warm start throughput"+j+".csv"));

                invoker.print();

                env.execute("Flink Pipeline Tokenization");
                System.out.println("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
                TimeUnit.SECONDS.sleep(10);
            }
        }
    }


}
