package operator;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;

public class FlinkPipeline {

    public static void main(String[] args) throws Exception {
        // create a Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // generate a stream of random strings
        DataStream<String> randomStrings = env.addSource(new RandomStringGenerator(1,0,1000));

        DataStream<String> tokens = randomStrings.process(new TokenizerProcessFunction());

        final StreamingFileSink<String> sink = StreamingFileSink
                .forRowFormat(new Path("FileSink"), new SimpleStringEncoder<String>("UTF-8"))


                .build();
        tokens.addSink(sink);


        env.execute("Flink Pipeline Tokenization");

    }


}