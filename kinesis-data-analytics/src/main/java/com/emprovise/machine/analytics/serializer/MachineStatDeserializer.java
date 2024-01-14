package com.emprovise.machine.analytics.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.emprovise.machine.dto.MachineStat;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MachineStatDeserializer extends AbstractDeserializationSchema<MachineStat> {

    private static final Logger LOG = LoggerFactory.getLogger(MachineStatDeserializer.class);
    private static final long serialVersionUID = -1699854177598621044L;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public MachineStat deserialize(byte[] message) throws IOException {
        try {
            return mapper.readValue(message, MachineStat.class);
        } catch (Exception ex) {
            LOG.error("Deserialize error {} for metric: {}", ex.getMessage(), new String(message));
            return null;
        }
    }
}
