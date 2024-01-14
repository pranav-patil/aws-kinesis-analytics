package com.emprovise.machine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CpuStat implements Serializable {

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

    public CpuStat() {
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
}
