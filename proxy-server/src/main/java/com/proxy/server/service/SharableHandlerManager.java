package com.proxy.server.service;

import com.proxy.server.handler.traffic.handler.TrafficCollectionHandler;
import com.proxy.server.handler.traffic.handler.TrafficLimitHandler;

/**
 * 共享模式的 handler 放这里
 * 暂时这样处理
 * @author ztgreat
 */
public class SharableHandlerManager {


    private static TrafficLimitHandler trafficLimitHandler=new TrafficLimitHandler();

    private static TrafficCollectionHandler trafficCollectionHandler =new TrafficCollectionHandler();


    public static TrafficLimitHandler getTrafficLimitHandler() {
        return trafficLimitHandler;
    }

    public static TrafficCollectionHandler getTrafficCollectionHandler() {
        return trafficCollectionHandler;
    }
}
