package com.emprovise.network.metrics.functions;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emprovise.network.metrics.cache.TempCache;
import com.emprovise.network.metrics.dto.MachineStat;
import com.emprovise.network.metrics.randomizer.RandomMachineStatsGenerator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements RequestHandler<ScheduledEvent, String> {

    private static String region = System.getenv("AWS_REGION");
    private static String kinesisStream = System.getenv("KINESIS_STREAM");

    @Override
    public String handleRequest(ScheduledEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        TempCache tempCache = new TempCache();

        final RandomMachineStatsGenerator generator = new RandomMachineStatsGenerator();
        List<MachineStat> machineMetrics = generator.getRandomMachineMetrics(tempCache.getInterfaceStatMap());
        MachineStat machineStat = machineMetrics.get(machineMetrics.size() - 1);
        tempCache.setInterfaceStatMap(generator.getInterfaceStatMap(machineStat));
        tempCache.persist();

        Collections.shuffle(machineMetrics);
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> metrics = machineMetrics.stream().map(stat -> writeValueAsString(objectMapper, stat)).collect(Collectors.toList());

        logger.log("Generated Metrics : " + metrics.size());

        AmazonKinesisClientBuilder builder = AmazonKinesisClientBuilder.standard().withRegion(region);
        AmazonKinesis kinesisClient = builder.build();

        PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
        putRecordsRequest.setStreamName(kinesisStream);
        List<PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();
        int counter = 0;

        for (String metric : metrics) {
            PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();
            putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(metric).getBytes()));
            putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", counter++));
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }

        putRecordsRequest.setRecords(putRecordsRequestEntryList);
        PutRecordsResult putRecordsResult  = kinesisClient.putRecords(putRecordsRequest);

        logger.log("Put Result : " + putRecordsResult);
        return "success";
    }

    private String writeValueAsString(ObjectMapper objectMapper, Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}