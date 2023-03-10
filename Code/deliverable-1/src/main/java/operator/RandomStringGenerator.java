package operator;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class RandomStringGenerator implements SourceFunction<String> {

    private volatile boolean isRunning = true;

    private final int maxDelayMillis;
    private final int minDelayMillis;
    private final int maxStringLength;
    private final Random random;

    public RandomStringGenerator(int maxDelayMillis, int minDelayMillis, int maxStringLength) {
        this.maxDelayMillis = maxDelayMillis;
        this.minDelayMillis = minDelayMillis;
        this.maxStringLength = maxStringLength;
        this.random = new Random();
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        while (isRunning) {
            long delayMillis = random.nextInt(maxDelayMillis - minDelayMillis) + minDelayMillis;
            Thread.sleep(delayMillis);

            int stringLength = random.nextInt(maxStringLength) + 1;
            char[] chars = new char[stringLength];
            for (int i = 0; i < stringLength; i++) {
                chars[i] = (char) (random.nextInt(26) + 'a');
            }
            String randomString = new String(chars);
            ctx.collect(randomString);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
