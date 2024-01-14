package com.emprovise.machine.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.emprovise.machine.analytics.transform.DerivativeWindowProcessFunction;
import com.emprovise.machine.analytics.transform.MachineMetricSplitter;
import com.emprovise.machine.dto.MachineStat;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

public class SocketAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SocketAnalyzer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final int PARALLELISM = 4;

    public static void main(String[] args) throws Exception {

//        EnvironmentSettings es = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, es);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Default is PIPELINED
//        env.getConfig().setExecutionMode(ExecutionMode.PIPELINED);
        env.setParallelism(PARALLELISM);
        env.setMaxParallelism(PARALLELISM * 2);
//        env.setStateBackend(new MemoryStateBackend());
//        env.setStateBackend(new FsStateBackend(""));
//        env.setStateBackend(new RocksDBStateBackend(""));

        // how often we want watermarks to be generated (default is one watermark every 200 msec)
        env.getConfig().setAutoWatermarkInterval(1000L);

        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointTimeout(60_000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(100);
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(0);
//        env.getCheckpointConfig().enableUnalignedCheckpoints();

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,10000L));
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, org.apache.flink.api.common.time.Time.minutes(10),
                                                                                 org.apache.flink.api.common.time.Time.minutes(1)));

        // how those watermarks are to be computed, i.e. use BoundedOutOfOrderness strategy, with a bounded delay of 3 seconds.
        WatermarkStrategy<MachineStat> watermarkStrategy = WatermarkStrategy
                .<MachineStat>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner((element, recordTimestamp) -> element.getTimestamp().getTime());

        DataStreamSource<String> socketTextStream = env.socketTextStream("localhost", 8989);
        socketTextStream.print();

        DataStream<String> metricsStream = socketTextStream
                .map((String string) -> {
                    try {
//                    System.out.println(object.toString());
                    return OBJECT_MAPPER.readValue(string, MachineStat.class);
                    } catch (Exception e) {
                        log.error("Error in parsing Machine Stat for Object: " + string);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .uid("machine-stat-mapper-id")
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .keyBy(new KeySelector<MachineStat, Tuple2<Long, Long>>() {
                    @Override
                    public Tuple2<Long, Long> getKey(MachineStat machineStat) throws Exception {
                        return Tuple2.of(machineStat.getUserId(), machineStat.getMachineId());
                    }
                })
                // evaluates window over a key grouped stream. Elements are put into windows by a WindowAssigner. The grouping of elements is done both by key and by window.
                // specifies the size of the window.
//                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .window(TumblingEventTimeWindows.of(Time.seconds(30)))
                .process(new DerivativeWindowProcessFunction())
                .uid("delta-processed-id")
                .name("delta-processed-stats")
                .flatMap(new MachineMetricSplitter())
//                .filter((FilterFunction<MachineMetric>) metric -> "flowStats".equals(metric.getMetric()))
                .uid("delta-metrics-id")
                .name("machine-metrics")
                .map(OBJECT_MAPPER::writeValueAsString);
//                .addSink(fileSink);

        metricsStream.print();

        // Sort the stream by timestamp, Find previous metric by timestamp (over period of 2 minutes),

        // Keyed Vs Windows Process Function ?

        env.execute();
        // doesn't wait, trigger asynchronous job execution
//        env.executeAsync();
    }
}
