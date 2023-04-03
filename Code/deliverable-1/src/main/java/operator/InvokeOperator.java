package operator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lambda.LambdaInvokerUsingURLPayload;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.operators.util.SimpleVersionedListState;
import org.apache.flink.util.Collector;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvokeOperator extends ProcessFunction<String,String> {

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ValueStateDescriptor<List> descriptor =
                new ValueStateDescriptor<List>("myListState", List.class);
        state = getRuntimeContext().getState(descriptor);
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
    /**
     * Todo find suitable implementation
     * How to create a list state
     * alternative: use value state to store a java list
     * might use other liststate implimentation
     */
    private transient ValueState<List> state;
    public InvokeOperator(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
        /**
         * add string to state
         */

        // check length of state replace 10 to acutal number
        int len=10;
        if(len<threshold){
            //store into the state
        }
        else{
            List<String> list = state.value();
            String payload = getPayload(list);
            String jsonResult = LambdaInvokerUsingURLPayload.invoke_lambda(payload);
            List<String> strings = getResultFromJson(jsonResult);
            for(String i:strings){
                out.collect(i);
            }
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

    public static void main(String[] args) {
        List<String> list = getResultFromJson("[\"aaa\",\"bbb\",\"ccc\"]");
        System.out.println(list.get(1));
    }


}
