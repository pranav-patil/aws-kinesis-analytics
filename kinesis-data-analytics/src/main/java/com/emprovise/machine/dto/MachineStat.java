package com.emprovise.machine.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MachineStat implements Serializable, Comparable<MachineStat> {

    @Valid
    @NotBlank
    @JsonProperty
    private String region;

    @Valid
    @NotNull
    @JsonProperty
    @JsonAlias("userId")
    @Min(value=1)
    private Long userId;

    @Valid
    @NotNull
    @JsonProperty
    @JsonAlias("machineId")
    @Min(value=1)
    private Long machineId;

    @NotNull
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date timestamp;

    @Valid
    @JsonProperty
    private MemoryStat memoryStat = new MemoryStat();

    @Valid
    @JsonProperty
    private List<CpuStat> cpuStats = new ArrayList<>();

    @Valid
    @JsonProperty
    private NetworkStat networkStat = new NetworkStat();

    @Valid
    @JsonProperty
    private List<InterfaceStat> interfaceStats = new ArrayList<>();

    public MachineStat() {
    }

    public MachineStat(String region, Long userId, Long machineId, Date timestamp) {
        this.region = region;
        this.userId = userId;
        this.machineId = machineId;
        this.timestamp = timestamp;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MemoryStat getMemoryStat() {
        return memoryStat;
    }

    public void setMemoryStat(MemoryStat memoryStat) {
        this.memoryStat = memoryStat;
    }

    public List<CpuStat> getCpuStats() {
        return cpuStats;
    }

    public void setCpuStats(List<CpuStat> cpuStats) {
        this.cpuStats = cpuStats;
    }

    public NetworkStat getNetworkStat() {
        return networkStat;
    }

    public void setNetworkStat(NetworkStat networkStat) {
        this.networkStat = networkStat;
    }

    public List<InterfaceStat> getInterfaceStats() {
        return interfaceStats;
    }

    public void setInterfaceStats(List<InterfaceStat> interfaceStats) {
        this.interfaceStats = interfaceStats;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public int compareTo(MachineStat machineStat) {
        return Long.compare(timestamp.getTime(), machineStat.timestamp.getTime());
    }
}
