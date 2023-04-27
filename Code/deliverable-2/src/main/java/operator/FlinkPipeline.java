package operator;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
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

//        env.enableCheckpointing(100);
        NexmarkConfiguration nexmarkConfiguration = new NexmarkConfiguration();
        nexmarkConfiguration.bidProportion = 46;
        GeneratorConfig generatorConfig = new GeneratorConfig(
                nexmarkConfiguration, System.currentTimeMillis(), 1, 1000, 1);

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
        DataStream<Tuple2<String, Long>> tokens = randomStrings
                .map(tuple -> Tuple2.of(tuple, System.currentTimeMillis()))
                .returns(Types.TUPLE(Types.STRING, Types.LONG));

        //filter strings to offload
        DataStream<Tuple3<String,Long, Boolean>> controlledStrings = tokens
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new ControllerProcessFunction())
                .returns(Types.TUPLE(Types.STRING, Types.LONG, Types.BOOLEAN));

        DataStream<Tuple2<String,Long>> trueControlledStrings = controlledStrings.filter(tuple -> tuple.f2.equals(true))
                .map(tuple -> Tuple2.of(tuple.f0,tuple.f1)).returns(Types.TUPLE(Types.STRING, Types.LONG));

        DataStream<Tuple3<String, Long, Boolean>> falseControlledStrings = controlledStrings
                .filter(tuple -> tuple.f2.equals(false)).returns(Types.TUPLE(Types.STRING, Types.LONG, Types.BOOLEAN));


        DataStream<Tuple2<String,Long>> timedTokens = falseControlledStrings
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new TokenizerProcessFunction())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<String,Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple2<String,Long> element) {
                        return System.currentTimeMillis(); // use the second field of the tuple as the timestamp
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.LONG));;

        //offload to lambda
        DataStream<Tuple2<String,Long>> lambdaTokens=trueControlledStrings
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new InvokeOperator())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<String,Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple2<String,Long> element) {
                        return System.currentTimeMillis(); // use the second field of the tuple as the timestamp
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.LONG));;

        //aggregate results
        DataStream<Tuple2<String,Long>> unionTokens = timedTokens.union(lambdaTokens);
        DataStream<Tuple2<String, Long>> aggregatedTokens = unionTokens.windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(10)))
                .process(new AggregatorProcessFunction())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<String,Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple2<String,Long> element) {
                        return System.currentTimeMillis(); // use the second field of the tuple as the timestamp
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.LONG));

        DataStream<Tuple2<String, Long>> latencyTokens = aggregatedTokens
                .map(tuple -> Tuple2.of(tuple.f0, System.currentTimeMillis()-tuple.f1))
                .returns(Types.TUPLE(Types.STRING, Types.LONG));


        //add to sink
        FileSink sink= CustomedFileSink.getSink();
        latencyTokens.sinkTo(sink);
        env.execute("Flink Pipeline Tokenization");
    }

}