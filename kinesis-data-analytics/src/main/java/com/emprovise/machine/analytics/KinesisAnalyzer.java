package com.emprovise.machine.analytics;

import com.amazonaws.services.kinesisanalytics.flink.connectors.producer.FlinkKinesisFirehoseProducer;
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emprovise.machine.analytics.serializer.MachineStatDeserializer;
import com.emprovise.machine.analytics.transform.DerivativeProcessFunction;
import com.emprovise.machine.analytics.transform.MachineMetricSplitter;
import com.emprovise.machine.dto.MachineStat;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class KinesisAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisAnalyzer.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        Map<String, Properties> runConfigurations = KinesisAnalyticsRuntime.getApplicationProperties();
        Properties inputConfig = runConfigurations.get("consumer.config.0");
        int parallelism = Integer.parseInt(inputConfig.getProperty("flink.parallelism", "1"));
        String region = inputConfig.getProperty("aws.region");
        String inputStreamName = inputConfig.getProperty("input.stream.name");
        String outputStreamName = inputConfig.getProperty("output.stream.name");
        String inputStartingPosition = inputConfig.getProperty("flink.stream.initpos");
        LOG.info("firehoseSinkPath is {}", outputStreamName);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelism);
        env.setMaxParallelism(parallelism * 2);

        // how often we want watermarks to be generated (default is one watermark every 200 msec), setting 2 seconds
        env.getConfig().setAutoWatermarkInterval(2000L);

//        enableCheckpoints(env);

        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, org.apache.flink.api.common.time.Time.minutes(5),
                                                                                 org.apache.flink.api.common.time.Time.seconds(10)));

        Properties inputProperties = new Properties();
        inputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION, inputStartingPosition);

        // how those watermarks are to be computed, i.e. use BoundedOutOfOrderness strategy, with a bounded delay of 3 seconds.
        WatermarkStrategy<MachineStat> watermarkStrategy = WatermarkStrategy
                .<MachineStat>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                .withTimestampAssigner((element, recordTimestamp) -> element.getTimestamp().getTime());

        DataStreamSource<MachineStat> dataStreamSource = env.addSource(new FlinkKinesisConsumer<>(inputStreamName, new MachineStatDeserializer(), inputProperties));
        dataStreamSource.print();

        DataStream<String> metricsStream = dataStreamSource
                .filter(Objects::nonNull)
                .uid("machine-stat-source-id")
                .name("machine-stat-source")
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .keyBy(new KeySelector<MachineStat, Tuple2<Long, Long>>() {
                    @Override
                    public Tuple2<Long, Long> getKey(MachineStat machineStat) throws Exception {
                        return Tuple2.of(machineStat.getUserId(), machineStat.getMachineId());
                    }
                })
                // evaluates window over a key grouped stream. Elements are put into windows by a WindowAssigner. The grouping of elements is done both by key and by window.
                // specifies the size of the window.
//                .window(TumblingEventTimeWindows.of(Time.minutes(3)))
//                .allowedLateness(Time.minutes(2))
//                .process(new DerivativeWindowProcessFunction())
                .process(new DerivativeProcessFunction())
                .uid("delta-processed-id")
                .name("delta-processed-stats")
                .flatMap(new MachineMetricSplitter())
                .uid("delta-metrics-id")
                .name("machine-metrics")
                .map(OBJECT_MAPPER::writeValueAsString);

        metricsStream.print();

        metricsStream.addSink(createFirehoseSinkFromStaticConfig(region, outputStreamName));

        env.execute();
    }

    private static FlinkKinesisFirehoseProducer<String> createFirehoseSinkFromStaticConfig(String region, String outputStreamName) {
        Properties outputProperties = new Properties();
        outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        return new FlinkKinesisFirehoseProducer<>(outputStreamName, new SimpleStringSchema(), outputProperties);
    }

    private static void enableCheckpoints(StreamExecutionEnvironment env) {
        // checkpoint every 5 minutes
        env.enableCheckpointing(300_000L);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // checkpoints have to complete within 10 minutes, or are discarded
        checkpointConfig.setCheckpointTimeout(600_000);
        checkpointConfig.setMaxConcurrentCheckpoints(1);
        checkpointConfig.setMinPauseBetweenCheckpoints(1000);
        checkpointConfig.setTolerableCheckpointFailureNumber(0);
//        env.getCheckpointConfig().enableUnalignedCheckpoints();
    }
}
