package com.emprovise.network.metrics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemoryStat implements Serializable {

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long availableMemory;

    @Valid
    @NotNull
    @JsonProperty
    @Min(value=0)
    private Long appResidentMemory;

    public MemoryStat() {
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
}
