package com.proxy.common.entity.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

/**
 * 服务器代理通道信息
 *
 * @author ztgreat
 */
public class ProxyChannel {

    /**
     * 本地socket 绑定端口
     */
    private Integer port;

    /**
     * 代理服务器 channel
     */
    private Channel channel;

    /**
     * 启动器
     */
    private ServerBootstrap bootstrap;

    /**
     * 代理类型
     */
    private int proxyType;

    /**
     * 客户端key
     */
    private String clientKey;

    /**
     * 代理状态
     */
    private int status;


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ServerBootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(ServerBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProxyType() {
        return proxyType;
    }

    public void setProxyType(int proxyType) {
        this.proxyType = proxyType;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
}
