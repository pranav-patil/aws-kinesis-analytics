package com.emprovise.machine.analytics.transform;

import com.emprovise.machine.dto.MachineMetric;
import com.emprovise.machine.dto.MachineStat;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MachineMetricSplitter implements FlatMapFunction<MachineStat, MachineMetric> {

    private static final Logger log = LoggerFactory.getLogger(MachineMetricSplitter.class);

    @Override
    public void flatMap(MachineStat machineStat, Collector<MachineMetric> out) throws Exception {

        List<MachineMetric> machineMetrics = new ArrayList<>();
        machineMetrics.add(mapMachineStat("memoryStats", machineStat, machineStat.getMemoryStat()));
        machineMetrics.add(mapMachineStat("networkStats", machineStat, machineStat.getNetworkStat()));
        machineMetrics.addAll(machineStat.getCpuStats().stream().map(x -> mapMachineStat("cpuStats", machineStat, x)).collect(Collectors.toList()));
        machineMetrics.addAll(machineStat.getInterfaceStats().stream().map(x -> mapMachineStat("interfaceStats", machineStat, x)).collect(Collectors.toList()));

        if(!machineStat.getInterfaceStats().isEmpty()) {
            machineMetrics.add(getInterfaceTotals(machineStat));
        }

        machineMetrics.forEach(out::collect);
    }

    private MachineMetric getInterfaceTotals(MachineStat machineStat) {
        MachineMetric totalInterfaceStat = new MachineMetric("interfaceStats");
        copyProperties(totalInterfaceStat, machineStat);
        totalInterfaceStat.setInterfaceStats("interfaceStatsTotal", 0L, 0L,
                0L, 0L, 0L,0L);
        machineStat.getInterfaceStats().forEach(totalInterfaceStat::addInterfaceTotal);
        return totalInterfaceStat;
    }

    private <T> MachineMetric mapMachineStat(String metricType, final MachineStat machineStat, T stat) {
        MachineMetric metric = new MachineMetric(metricType);
        copyProperties(metric, machineStat);
        copyProperties(metric, stat);
        return metric;
    }

    private void copyProperties(Object dest, Object orig) {
        try {
            BeanUtils.copyProperties(dest, orig);
        } catch (Exception ex) {
            log.error("Error in Copy Beans", ex);
            throw new RuntimeException(ex);
        }
    }
}
