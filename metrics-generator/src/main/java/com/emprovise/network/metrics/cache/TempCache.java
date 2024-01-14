package com.emprovise.network.metrics.cache;

import com.emprovise.network.metrics.dto.InterfaceStat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class TempCache {

    private Map<String, InterfaceStat> interfaceStatMap;
    private final File filterCacheFile;
    private static final String FILE_PATH = "objects/";

    public TempCache() {

        try {
            File dirPath = new File("/tmp/" + FILE_PATH);

            if(!dirPath.exists() && !dirPath.mkdirs()) {
                System.err.println("File tmp does not exists !");
            }

            filterCacheFile = new File(dirPath, "filterCache.ser");

            if(filterCacheFile.exists()) {
                interfaceStatMap = (HashMap<String, InterfaceStat>) readObjectFromFile(filterCacheFile, HashMap.class);
            } else {
                interfaceStatMap = new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to read temp cache", e);
        }
    }

    public InterfaceStat getInterfaceStat(String filterId) {
        return interfaceStatMap.get(filterId);
    }

    public InterfaceStat putInterfaceStat(String filterId, InterfaceStat flowStat) {
        return interfaceStatMap.put(filterId, flowStat);
    }

    public boolean isInterfaceStatEmpty() {
        return interfaceStatMap.isEmpty();
    }

    public void setInterfaceStatMap(Map<String, InterfaceStat> flowStatMap) {
        this.interfaceStatMap = flowStatMap;
    }

    public Map<String, InterfaceStat> getInterfaceStatMap() {
        return this.interfaceStatMap;
    }

    public void persist() {
        try {
            if(!filterCacheFile.exists() || filterCacheFile.delete()) {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filterCacheFile));
                out.writeObject(interfaceStatMap);
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to persis filter cache file", e);
        }
    }

    private static <T> T readObjectFromFile(File file, Class<T> objectType) throws IOException, ClassNotFoundException {
        T result;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            result = objectType.cast(ois.readObject());
        }
        return result;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TempCache cache = new TempCache();

        if(cache.filterCacheFile.exists()) {
            System.out.println("flowStatMap: " +  cache.getInterfaceStatMap());
        }
//            FlowStat flowStat = new FlowStat();
//            flowStat.setFilterId("filter_566565");
//            flowStat.setMatchedFlows(2343242L);
//            flowStat.setTriggeredFlows(4564557L);
//            cache.putFlowStat("filter_566565", flowStat);
//
//            flowStat = new FlowStat();
//            flowStat.setFilterId("filter_111111");
//            flowStat.setMatchedFlows(23443243L);
//            flowStat.setTriggeredFlows(8767867L);
//            cache.putFlowStat("filter_111111", flowStat);
//
//            cache.persist();

        System.out.println("File is " + cache.filterCacheFile.getAbsolutePath());
    }
}
