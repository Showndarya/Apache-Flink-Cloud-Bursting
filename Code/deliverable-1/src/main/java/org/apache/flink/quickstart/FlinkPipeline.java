package org.apache.flink.quickstart;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;



public class FlinkPipeline {

    public static void main(String[] args) throws Exception {
        // create a Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // generate a stream of random strings
        DataStream<String> randomStrings = env.addSource(new RandomStringGenerator(1000,500,100));

        DataStream<String> tokens = randomStrings.process(new TokenizerProcessFunction());
        //System.out.println("lambda tokens:" + lambda_tokens);
        //tokens.print();
        env.execute("Flink Pipeline Tokenization");

    }


}