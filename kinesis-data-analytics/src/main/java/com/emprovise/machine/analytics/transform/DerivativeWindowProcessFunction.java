package com.emprovise.machine.analytics.transform;

import com.emprovise.machine.dto.MachineStat;
import com.emprovise.machine.dto.NetworkStat;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.emprovise.machine.analytics.transform.DerivativeWindowProcessFunction.NetworkComponent.Fpga;
import static com.emprovise.machine.analytics.transform.DerivativeWindowProcessFunction.NetworkComponent.Nic;


public class DerivativeWindowProcessFunction extends ProcessWindowFunction<MachineStat, MachineStat, Tuple2<Long, Long>, TimeWindow> {

    private transient ValueState<HashMap<String, Tuple2<Long, Long>>> filterState;
    private transient ValueState<HashMap<String, Tuple2<Long, Long>>> networkState;
    public static final String TIMESTAMP = "TIMESTAMP";

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        StateTtlConfig stateTtlConfig = StateTtlConfig
                .newBuilder(Time.days(7))
                .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        ValueStateDescriptor<HashMap<String, Tuple2<Long, Long>>> filterStateDescriptor = new ValueStateDescriptor<>("filterState",
                                                                                                                    TypeInformation.of(new TypeHint<>() {}));
        filterStateDescriptor.enableTimeToLive(stateTtlConfig);
        filterState = getRuntimeContext().getState(filterStateDescriptor);

        ValueStateDescriptor<HashMap<String, Tuple2<Long, Long>>> networkStateDescriptor = new ValueStateDescriptor<>("networkState",
                                                                                                                    TypeInformation.of(new TypeHint<>() {}));
        networkStateDescriptor.enableTimeToLive(stateTtlConfig);
        networkState = getRuntimeContext().getState(networkStateDescriptor);

    }

    @Override
    public void process(Tuple2<Long, Long> key, Context context, Iterable<MachineStat> elements, Collector<MachineStat> collector) throws Exception {

        List<MachineStat> machineStatList = new ArrayList<>();
        elements.forEach(machineStatList::add);
        Collections.sort(machineStatList);

        for (MachineStat machineStat : machineStatList) {

            NetworkStat networkStat = machineStat.getNetworkStat();
            Long packetsTotalNic = networkStat.getPacketsTotalNic();

            if(packetsTotalNic != null && packetsTotalNic > 0) {

                HashMap<String, Tuple2<Long, Long>> networkMap = networkState.value();

                if (networkMap != null && !networkMap.isEmpty()) {
                    machineStat.setNetworkStat(getCongestionStats(networkMap, networkStat));
                }

                networkMap = getUpdatedNetworkMap(machineStat);
                networkMap.put(TIMESTAMP, Tuple2.of(machineStat.getTimestamp().getTime(),0L));
                networkState.update(networkMap);
            }

            collector.collect(machineStat);
        }
    }

    private HashMap<String, Tuple2<Long, Long>> getUpdatedNetworkMap(MachineStat machineStat) {
        HashMap<String, Tuple2<Long, Long>> networkMap = new HashMap<>();
        NetworkStat networkStat = machineStat.getNetworkStat();
        networkMap.put(Nic.name(),     Tuple2.of(networkStat.getPacketsDroppedNic(), networkStat.getPacketsTotalNic()));
        networkMap.put(Fpga.name(),    Tuple2.of(networkStat.getPacketsDroppedFpga(), networkStat.getPacketsTotalFpga()));
        return networkMap;
    }

    private NetworkStat getCongestionStats(HashMap<String, Tuple2<Long, Long>> networkMap, NetworkStat networkStat) {

        Tuple2<Long, Long> nicTuple = networkMap.get(Nic.name());
        long droppedNic = networkStat.getPacketsDroppedNic() - nicTuple.f0;
        long totalNic = networkStat.getPacketsTotalNic() - nicTuple.f1;
        networkStat.setCongestionNic(percent(droppedNic, totalNic));

        Tuple2<Long, Long> fpgaTuple = networkMap.get(Fpga.name());
        long droppedFpga = networkStat.getPacketsDroppedFpga() - fpgaTuple.f0;
        long totalFpga = networkStat.getPacketsTotalFpga() - fpgaTuple.f1;
        networkStat.setCongestionFpga(percent(droppedFpga, totalFpga));

        long allDrops = droppedNic + droppedFpga;
        networkStat.setCongestionAll(percent(allDrops, totalNic));
        return networkStat;
    }

    private long percent(long obtained, long total) {
        return obtained * 100 / total;
    }

    enum NetworkComponent {
        Nic, Fpga;
    }
}