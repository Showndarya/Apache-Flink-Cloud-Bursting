package operator;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CustomMetricReporter implements MetricReporter, Scheduled {

    public static  AtomicLong inputRate = new AtomicLong(0L);
    public static AtomicBoolean shouldOffload = new AtomicBoolean(false);
    private final int throughputThreshold = 5; // Set your desired threshold here

    @Override
    public void open(MetricConfig metricConfig) {
    }

    @Override
    public void close() {
    }


    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        if (metricName.equals("numRecordsInPerSecond")) {
            if (metric instanceof Gauge) {
                Gauge<Long> gauge = (Gauge<Long>) metric;
                inputRate.set(gauge.getValue());
                System.out.println("############# Found input rate metric: " + metricName + ", value: " + gauge.getValue());
            } else if (metric instanceof Meter) {
                Meter meter = (Meter) metric;
                inputRate.set((long) meter.getRate());
                System.out.println("############# Found input rate metric: " + metricName + ", value: " + meter.getRate());
            } else if (metric instanceof Counter) {
                Counter recordsInCounter = (Counter) metric;
                System.out.println("############# Found records in counter metric: " + metricName);
            }
        }
    }



    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        if (metricName.contains("numRecordsInPerSecond")) {
            inputRate.set(0L);
        }
    }

    @Override
    public void report() {
        // Update the input rate and shouldOffload flag
        long currentInputRate = inputRate.get();
        shouldOffload.set(currentInputRate > throughputThreshold);
    }
}
