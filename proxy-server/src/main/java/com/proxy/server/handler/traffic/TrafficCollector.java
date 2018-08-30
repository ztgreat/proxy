package com.proxy.server.handler.traffic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流量统计工具
 */
public class TrafficCollector {

    private static Map<Integer, TrafficCollector> trafficCollectors = new ConcurrentHashMap<Integer, TrafficCollector>();

    private Integer port;

    private AtomicLong readBytes = new AtomicLong();

    private AtomicLong wroteBytes = new AtomicLong();

    private AtomicLong readMsgs = new AtomicLong();

    private AtomicLong wroteMsgs = new AtomicLong();

    private AtomicInteger channels = new AtomicInteger();

    private TrafficCollector() {
    }

    public static TrafficCollector getCollector(Integer port) {
        TrafficCollector collector = trafficCollectors.get(port);
        if (collector == null) {
            synchronized (trafficCollectors) {
                collector = trafficCollectors.get(port);
                if (collector == null) {
                    collector = new TrafficCollector();
                    collector.setPort(port);
                    trafficCollectors.put(port, collector);
                }
            }
        }

        return collector;
    }

    public static List<Traffic> getAndResetAllTraffic() {
        List<Traffic> allMetrics = new ArrayList<Traffic>();
        Iterator<Entry<Integer, TrafficCollector>> ite = trafficCollectors.entrySet().iterator();
        while (ite.hasNext()) {
            allMetrics.add(ite.next().getValue().getAndResetTraffic());
        }

        return allMetrics;
    }

    public static List<Traffic> getAllTraffic() {
        List<Traffic> allMetrics = new ArrayList<Traffic>();
        Iterator<Entry<Integer, TrafficCollector>> ite = trafficCollectors.entrySet().iterator();
        while (ite.hasNext()) {
            allMetrics.add(ite.next().getValue().getTraffic());
        }

        return allMetrics;
    }

    public Traffic getAndResetTraffic() {
        Traffic traffic = new Traffic();
        traffic.setChannels(channels.get());
        traffic.setPort(port);
        traffic.setReadBytes(readBytes.getAndSet(0));
        traffic.setWriteBytes(wroteBytes.getAndSet(0));
        traffic.setTimestamp(System.currentTimeMillis());
        traffic.setReadMsgs(readMsgs.getAndSet(0));
        traffic.setWriteMsgs(wroteMsgs.getAndSet(0));

        return traffic;
    }

    public Traffic getTraffic() {
        Traffic traffic = new Traffic();
        traffic.setChannels(channels.get());
        traffic.setPort(port);
        traffic.setReadBytes(readBytes.get());
        traffic.setWriteBytes(wroteBytes.get());
        traffic.setTimestamp(System.currentTimeMillis());
        traffic.setReadMsgs(readMsgs.get());
        traffic.setWriteMsgs(wroteMsgs.get());

        return traffic;
    }

    public void incrementReadBytes(long bytes) {
        readBytes.addAndGet(bytes);
    }

    public void incrementWriteBytes(long bytes) {
        wroteBytes.addAndGet(bytes);
    }

    public void incrementReadMsgs(long msgs) {
        readMsgs.addAndGet(msgs);
    }

    public void incrementWriteMsgs(long msgs) {
        wroteMsgs.addAndGet(msgs);
    }

    public AtomicInteger getChannels() {
        return channels;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

}