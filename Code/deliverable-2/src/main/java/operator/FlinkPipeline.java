package operator;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.tuple.Tuple2;
import nexmark.NexmarkConfiguration;
import nexmark.generator.GeneratorConfig;
import nexmark.model.Event;
import nexmark.source.NexmarkSourceFunction;
import nexmark.source.EventDeserializer;
import org.apache.flink.streaming.api.functions.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;


public class FlinkPipeline {

    public static void main(String[] args) throws Exception {
        // create a Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.disableOperatorChaining();
        env.getConfig().setAutoWatermarkInterval(1000L);
        env.getConfig().setLatencyTrackingInterval(500);

//        env.enableCheckpointing(100);
        NexmarkConfiguration nexmarkConfiguration = new NexmarkConfiguration();
        nexmarkConfiguration.bidProportion = 46;
        GeneratorConfig generatorConfig = new GeneratorConfig(
                nexmarkConfiguration, System.currentTimeMillis(), 1, 0, 1);

        // generate a stream of random strings
        DataStream<String> randomStrings = env.addSource(new NexmarkSourceFunction<>(
                generatorConfig,
                (EventDeserializer<String>) Event::toString,
                BasicTypeInfo.STRING_TYPE_INFO))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<String>() {
                    @Override
                    public long extractAscendingTimestamp(String element) {
                        return System.currentTimeMillis();
                    }
                });

        DataStream<Tuple2<String,Boolean>> controlledStrings = randomStrings
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new ControllerProcessFunction());
        DataStream<String> trueControlledStrings = controlledStrings.filter(tuple -> tuple.f1.equals(true)).map(tuple -> tuple.f0);
        DataStream<Tuple2<String, Boolean>> falseControlledStrings = controlledStrings.filter(tuple -> tuple.f1.equals(false));

        // Create a KeyedStream with a dummy key extractor function
//        KeyedStream<Tuple2<String, Boolean>, String> keyedControlledStrings = falseControlledStrings.keyBy(t -> "");
        DataStream<String> tokens = falseControlledStrings
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new TokenizerProcessFunction())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<String>() {
                    @Override
                    public long extractAscendingTimestamp(String element) {
                        return System.currentTimeMillis();
                    }
                });
        DataStream<String> lambdaTokens=trueControlledStrings
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new InvokeOperator())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<String>() {
                    @Override
                    public long extractAscendingTimestamp(String element) {
                        return System.currentTimeMillis();
                    }
                });

        DataStream<String> unionTokens = tokens.union(lambdaTokens);
        DataStream<String> aggregatedTokens = unionTokens.windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new AggregatorProcessFunction())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<String>() {
                    @Override
                    public long extractAscendingTimestamp(String element) {
                        return System.currentTimeMillis();
                    }
                })
                .map(token -> String.format("%s @ %d", token, System.currentTimeMillis())); // add timestamp to string


        FileSink sink= CustomedFileSink.getSink();
        aggregatedTokens.sinkTo(sink);
        env.execute("Flink Pipeline Tokenization");
    }


}