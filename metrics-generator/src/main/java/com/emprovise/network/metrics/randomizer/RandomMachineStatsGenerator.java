package com.emprovise.network.metrics.randomizer;


import com.emprovise.network.metrics.dto.CpuStat;
import com.emprovise.network.metrics.dto.MachineStat;
import com.emprovise.network.metrics.dto.InterfaceStat;
import com.emprovise.network.metrics.dto.MemoryStat;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;
import org.jeasy.random.randomizers.range.DateRangeRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.jeasy.random.randomizers.range.LongRangeRandomizer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.jeasy.random.FieldPredicates.inClass;
import static org.jeasy.random.FieldPredicates.named;
import static org.jeasy.random.FieldPredicates.ofType;

public class RandomMachineStatsGenerator {

    public List<MachineStat> getRandomMachineMetrics(Map<String, InterfaceStat> previousInterfaceStatMap) {

        int minuteRange = 2;
        int machineStatTotal = 4;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beforeNow = now.minusMinutes(minuteRange);

        EasyRandomParameters parameters = new EasyRandomParameters()
                .seed(123L)
                .objectPoolSize(10000)
                .randomizationDepth(3)
                .charset(StandardCharsets.UTF_8)
                .stringLengthRange(4, 10)
                .collectionSizeRange(1, 10)
                .scanClasspathForConcreteTypes(true)
                .overrideDefaultInitialization(false)
                .timeRange(beforeNow.toLocalTime(), now.toLocalTime())
                .dateRange(beforeNow.toLocalDate(), now.toLocalDate())
//                .bypassSetters(true)
//                .ignoreRandomizationErrors(true)
//                .excludeField(named("age").and(ofType(Integer.class)).and(inClass(MachineStat.class)))
                .randomize(Integer.class, new IntegerRangeRandomizer(1, 100000))
                .randomize(Long.class, new LongRangeRandomizer(1L, 100000L))
                .randomize(named("timestamp").and(ofType(Date.class)).and(inClass(MachineStat.class)), new DateRangeRandomizer(Date.from(beforeNow.atZone(ZoneId.systemDefault()).toInstant()),
                                                                                                                              Date.from(now.atZone(ZoneId.systemDefault()).toInstant())))
                .randomize(named("networkInterface").and(ofType(String.class)).and(inClass(InterfaceStat.class)), new RegularExpressionRandomizer("^(nw_ifc_)([0-9]{4})"))
                .randomize(named("type").and(ofType(String.class)).and(inClass(InterfaceStat.class)), new ValueSetRandomizer<>("ethernet", "VLAN", "LAN"))
                .randomize(named("availableMemory").and(ofType(Long.class)).and(inClass(MemoryStat.class)), new LongRangeRandomizer(1L, 100L))
                .randomize(named("appResidentMemory").and(ofType(Long.class)).and(inClass(MemoryStat.class)), new LongRangeRandomizer(1L, 100L))
                .randomize(named("core").and(ofType(String.class)).and(inClass(CpuStat.class)), new UniqueValueSetRandomizer<>("a0", "b0", "c0", "a1", "b1", "a2", "a3"))
                .randomize(named("frequency").and(ofType(Long.class)).and(inClass(CpuStat.class)), new LongRangeRandomizer(1L, 100L));

        EasyRandom easyRandom = new EasyRandom(parameters);

        List<MachineStat> machineStats = easyRandom.objects(MachineStat.class, machineStatTotal).collect(Collectors.toList());
        Collections.sort(machineStats);

        for (MachineStat machineStat : machineStats) {

            List<InterfaceStat> interfaceStats = randomFlowList(easyRandom, previousInterfaceStatMap.keySet());

            if(!previousInterfaceStatMap.isEmpty()) {
                for (InterfaceStat interfaceStat : interfaceStats) {
                    if(previousInterfaceStatMap.containsKey(interfaceStat.getNetworkInterface())) {
                        InterfaceStat prevInterfaceStat = previousInterfaceStatMap.get(interfaceStat.getNetworkInterface());
                        interfaceStat.setRxPackets(interfaceStat.getRxPackets() + prevInterfaceStat.getRxPackets());
                        interfaceStat.setRxBytes(interfaceStat.getRxBytes() + prevInterfaceStat.getRxBytes());
                        interfaceStat.setRxPacketsDropped(interfaceStat.getRxPacketsDropped() + prevInterfaceStat.getRxPacketsDropped());
                        interfaceStat.setTxPackets(interfaceStat.getTxPackets() + prevInterfaceStat.getTxPackets());
                        interfaceStat.setTxBytes(interfaceStat.getTxBytes() + prevInterfaceStat.getTxBytes());
                        interfaceStat.setTxPacketsDropped(interfaceStat.getTxPacketsDropped() + prevInterfaceStat.getTxPacketsDropped());
                    }
                }
            }

            machineStat.setInterfaceStats(interfaceStats);
            previousInterfaceStatMap = getInterfaceStatMap(machineStat);
        }

        return machineStats;
    }

    public HashMap<String, InterfaceStat> getInterfaceStatMap(MachineStat machineStat) {
        return machineStat.getInterfaceStats()
                .parallelStream()
                .collect(Collectors.toMap(
                        InterfaceStat::getNetworkInterface,
                        m -> m,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        HashMap::new));
    }

    private List<InterfaceStat> randomFlowList(EasyRandom easyRandom, Set<String> networkInterfaces) {

        List<InterfaceStat> interfaceStats = easyRandom.objects(InterfaceStat.class, networkInterfaces.size()).collect(Collectors.toList());

        if(!networkInterfaces.isEmpty()) {
            final UniqueValueSetRandomizer<String> randomizer = new UniqueValueSetRandomizer<>(networkInterfaces);

            // Dynamically changing list of network interfaces, not quite realistic but demo for other cases
            for (int i = 0; i < networkInterfaces.size(); i++) {
                InterfaceStat flowStat = interfaceStats.get(i);
                flowStat.setNetworkInterface(randomizer.getRandomValue());
            }
        }

        Set<InterfaceStat> uniqueSet = interfaceStats.stream().collect(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(InterfaceStat::getNetworkInterface))));
        return new ArrayList<>(uniqueSet);
    }

    public static void main(String[] args) {
        RandomMachineStatsGenerator generator = new RandomMachineStatsGenerator();
        generator.getRandomMachineMetrics(new HashMap<>()).forEach(System.out::println);
    }
}
