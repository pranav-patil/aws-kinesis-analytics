package com.emprovise.network.metrics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkStat implements Serializable {

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

    public NetworkStat() {
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
}