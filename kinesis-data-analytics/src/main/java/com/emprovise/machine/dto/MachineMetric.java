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
import java.util.Date;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MachineMetric implements Serializable {

    @Valid
    @NotBlank
    @JsonProperty
    private String metric;

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
    private Date timestamp;

    @NotNull
    @Min(value = 0)
    @JsonProperty
    private Long availableMemory;

    @NotNull
    @Min(value = 0)
    @JsonProperty
    private Long appResidentMemory;

    @Valid
    @NotBlank
    @JsonProperty
    private String core;

    @Valid
    @NotNull
    @Min(value=0)
    @JsonProperty
    private Integer utilization;

    @NotNull
    @Min(value = 0)
    @JsonProperty
    private Long cyclesProcessed;

    @NotNull
    @Min(value = 0)
    @JsonProperty
    private Long frequency;

    @Valid
    @JsonProperty
    @NotNull
    @Min(value=0)
    private Long connectionRate;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long packetsDroppedNic;

    @Valid
    @JsonProperty
    @Min(value=0)
    private Long packetsTotalNic;

    @Valid
    @JsonProperty
    @Min(value=0)
    private Long packetsDroppedFpga;

    @Valid
    @JsonProperty
    @Min(value=0)
    private Long packetsTotalFpga;

    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long decompressedPackets;

    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long avgLatency;

    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long congestionAll;

    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long congestionFpga;

    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long congestionNic;

    @Valid
    @NotBlank
    @JsonProperty
    private String networkInterface;

    @Valid
    @NotBlank
    @JsonProperty
    private String type;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long rxPackets;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long rxBytes;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long rxPacketsDropped;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long txPackets;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long txBytes;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long txPacketsDropped;

    public MachineMetric(String metric) {
        this.metric = metric;
    }

    public MachineMetric(String metric, String region, Long userId, Long machineId, Date timestamp) {
        this.metric = metric;
        this.region = region;
        this.userId = userId;
        this.machineId = machineId;
        this.timestamp = timestamp;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
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

    public Long getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(Long availableMemory) {
        this.availableMemory = availableMemory;
    }

    public Long getAppResidentMemory() {
        return appResidentMemory;
    }

    public void setAppResidentMemory(Long appResidentMemory) {
        this.appResidentMemory = appResidentMemory;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public Integer getUtilization() {
        return utilization;
    }

    public void setUtilization(Integer utilization) {
        this.utilization = utilization;
    }

    public Long getCyclesProcessed() {
        return cyclesProcessed;
    }

    public void setCyclesProcessed(Long cyclesProcessed) {
        this.cyclesProcessed = cyclesProcessed;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Long getConnectionRate() {
        return connectionRate;
    }

    public void setConnectionRate(Long connectionRate) {
        this.connectionRate = connectionRate;
    }

    public Long getPacketsDroppedNic() {
        return packetsDroppedNic;
    }

    public void setPacketsDroppedNic(Long packetsDroppedNic) {
        this.packetsDroppedNic = packetsDroppedNic;
    }

    public Long getPacketsTotalNic() {
        return packetsTotalNic;
    }

    public void setPacketsTotalNic(Long packetsTotalNic) {
        this.packetsTotalNic = packetsTotalNic;
    }

    public Long getPacketsDroppedFpga() {
        return packetsDroppedFpga;
    }

    public void setPacketsDroppedFpga(Long packetsDroppedFpga) {
        this.packetsDroppedFpga = packetsDroppedFpga;
    }

    public Long getPacketsTotalFpga() {
        return packetsTotalFpga;
    }

    public void setPacketsTotalFpga(Long packetsTotalFpga) {
        this.packetsTotalFpga = packetsTotalFpga;
    }

    public Long getDecompressedPackets() {
        return decompressedPackets;
    }

    public void setDecompressedPackets(Long decompressedPackets) {
        this.decompressedPackets = decompressedPackets;
    }

    public Long getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(Long avgLatency) {
        this.avgLatency = avgLatency;
    }

    public Long getCongestionAll() {
        return congestionAll;
    }

    public void setCongestionAll(Long congestionAll) {
        this.congestionAll = congestionAll;
    }

    public Long getCongestionFpga() {
        return congestionFpga;
    }

    public void setCongestionFpga(Long congestionFpga) {
        this.congestionFpga = congestionFpga;
    }

    public Long getCongestionNic() {
        return congestionNic;
    }

    public void setCongestionNic(Long congestionNic) {
        this.congestionNic = congestionNic;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRxPackets() {
        return rxPackets;
    }

    public void setRxPackets(Long rxPackets) {
        this.rxPackets = rxPackets;
    }

    public Long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(Long rxBytes) {
        this.rxBytes = rxBytes;
    }

    public Long getRxPacketsDropped() {
        return rxPacketsDropped;
    }

    public void setRxPacketsDropped(Long rxPacketsDropped) {
        this.rxPacketsDropped = rxPacketsDropped;
    }

    public Long getTxPackets() {
        return txPackets;
    }

    public void setTxPackets(Long txPackets) {
        this.txPackets = txPackets;
    }

    public Long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(Long txBytes) {
        this.txBytes = txBytes;
    }

    public Long getTxPacketsDropped() {
        return txPacketsDropped;
    }

    public void setTxPacketsDropped(Long txPacketsDropped) {
        this.txPacketsDropped = txPacketsDropped;
    }

    public void setInterfaceStats(String interfaceName, Long rxPackets, Long rxBytes, Long rxPacketsDropped,
                                  Long txPackets, Long txBytes, Long txPacketsDropped) {
        this.networkInterface = interfaceName;
        this.rxPackets = rxPackets;
        this.rxBytes = rxBytes;
        this.rxPacketsDropped = rxPacketsDropped;
        this.txPackets = txPackets;
        this.txBytes = txBytes;
        this.txPacketsDropped = txPacketsDropped;
    }

    public void addInterfaceTotal(InterfaceStat interfaceStat) {

        if(interfaceStat.getRxPackets() != null) {
            this.rxPackets = this.rxPackets + interfaceStat.getRxPackets();
        }
        if(interfaceStat.getRxBytes() != null) {
            this.rxBytes = this.rxBytes + interfaceStat.getRxBytes();
        }
        if(interfaceStat.getRxPacketsDropped() != null) {
            this.rxPacketsDropped = this.rxPacketsDropped + interfaceStat.getRxPacketsDropped();
        }
        if(interfaceStat.getTxPackets() != null) {
            this.txPackets = this.txPackets + interfaceStat.getTxPackets();
        }
        if(interfaceStat.getTxBytes() != null) {
            this.txBytes = this.txBytes + interfaceStat.getTxBytes();
        }
        if(interfaceStat.getTxPacketsDropped() != null) {
            this.txPacketsDropped = this.txPacketsDropped + interfaceStat.getTxPacketsDropped();
        }
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
}
