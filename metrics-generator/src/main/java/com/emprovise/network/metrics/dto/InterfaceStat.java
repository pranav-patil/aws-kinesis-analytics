package com.emprovise.network.metrics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterfaceStat implements Serializable {

    @Valid
    @NotBlank
    @JsonProperty
    private String networkInterface;

    // ethernet, VLAN
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

    public InterfaceStat() {
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
}
