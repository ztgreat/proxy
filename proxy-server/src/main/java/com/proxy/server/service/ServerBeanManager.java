package com.proxy.server.service;

import com.proxy.common.util.NumberGenerate;
import com.proxy.common.util.SessionIDGenerate;
import com.proxy.server.handler.traffic.handler.TrafficLimitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * 将实例服务 放这里
 * 暂时这样处理
 * @// TODO: 2018/2/11  
 * @author ztgreat
 */
public class ServerBeanManager {

    /**
     * 代理服务 bootstrap
     */
    private  static ServerBootstrap bootstrap;

    /**
     * 用于生成sessionID
     */
    private static NumberGenerate sessionIDGenerate = SessionIDGenerate.getInstance();

    /**
     * 客户端服务,用于获取客户端信息
     */
    private static ClientService clientService =new ClientService();

    /**
     * 转发用户消息到代理客户端
     */
    private static TransferService transferService=new TransferService();

    /**
     * 用户回话管理
     */
    private static UserSessionService userSessionService =new UserSessionService();

    /**
     * 配置管理
     */
    private static ConfigService configService=new ConfigService();

    /**
     * 代理通道管理
     */
    private static ProxyChannelService proxyChannelService =new ProxyChannelService();

    /**
     * 限流
     */
    private static TrafficLimitHandler trafficLimitHandler=new TrafficLimitHandler();


    public static ServerBootstrap getBootstrap() {
        if (bootstrap!=null)
            return bootstrap.clone();
        return null;
    }
    public static void setBootstrap(ServerBootstrap boot) {
        bootstrap = boot;
    }

    public static ClientService getClientService() {
        return clientService;
    }

    public static TransferService getTransferService() {
        return transferService;
    }

    public static UserSessionService getUserSessionService() {
        return userSessionService;
    }

    public static ConfigService getConfigService() {
        return configService;
    }


    public static ProxyChannelService getProxyChannelService() {
        return proxyChannelService;
    }

    public static NumberGenerate getSessionIDGenerate() {
        return sessionIDGenerate;
    }

    public static TrafficLimitHandler getTrafficLimitHandler() {
        return trafficLimitHandler;
    }
}
