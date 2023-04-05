package operator;


import operator.ControllerProcessFunction;
import operator.CustomedFileSink;
import operator.TokenizerProcessFunction;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.tuple.Tuple2;

import nexmark.NexmarkConfiguration;
import nexmark.generator.GeneratorConfig;
import nexmark.model.Event;
import nexmark.source.NexmarkSourceFunction;
import nexmark.source.EventDeserializer;

import java.io.File;

public class FlinkPipeline {

    public static void main(String[] args) throws Exception {
        // create a Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.disableOperatorChaining();

//        env.enableCheckpointing(100);
        NexmarkConfiguration nexmarkConfiguration = new NexmarkConfiguration();
        nexmarkConfiguration.bidProportion = 46;
        GeneratorConfig generatorConfig = new GeneratorConfig(
                nexmarkConfiguration, System.currentTimeMillis(), 1, 100, 1);

        // generate a stream of random strings
        DataStream<String> randomStrings = env.addSource(new NexmarkSourceFunction<>(
                generatorConfig,
                (EventDeserializer<String>) Event::toString,
                BasicTypeInfo.STRING_TYPE_INFO));

        DataStream<Tuple2<String, Boolean>> controlledStrings = randomStrings.process(new ControllerProcessFunction());

// Create a KeyedStream with a dummy key extractor function
        KeyedStream<Tuple2<String, Boolean>, String> keyedControlledStrings = controlledStrings.keyBy(t -> "");

        DataStream<String> tokens = keyedControlledStrings.process(new TokenizerProcessFunction());



        FileSink sink= CustomedFileSink.getSink();
        tokens.sinkTo(sink);




        env.execute("Flink Pipeline Tokenization");

    }


}