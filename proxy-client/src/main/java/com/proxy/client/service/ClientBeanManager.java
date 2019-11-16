package com.proxy.client.service;


/**
 * 客户端 实例化服务
 */
public class ClientBeanManager {

    private static ConfigService configService = new ConfigService();

    private static ProxyService proxyService = new ProxyService();

    public static ConfigService getConfigService() {
        return configService;
    }

    public static ProxyService getProxyService() {
        return proxyService;
    }
}
