package com.proxy.common.entity.client;

import io.netty.channel.Channel;

/**
 * 代理客户端和真实服务器的映射
 *
 * @author ztgreat
 */
public class RealServer {

    /**
     * 映射名称
     */
    private String name;

    /**
     * 真实服务器端口
     */
    private Integer realHostPort;

    /**
     * 服务器 服务端口,通过端口转发的时候启用
     */
    private Integer serverPort;

    /**
     * 访问域名,通过域名转发的时候启用
     */
    private String domain;

    /**
     * 代理类型
     */
    private Integer proxyType;

    /**
     * 真实服务器地址
     */
    private String realHost;

    /**
     * 代理客户端key
     */
    private String clientKey;

    /**
     * 描述
     */
    private String description;


    /**
     * 代理客户端和真实服务器通道
     */
    private Channel channel;

    /**
     * 该代理状态
     */
    private int status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRealHostPort() {
        return realHostPort;
    }

    public void setRealHostPort(Integer realHostPort) {
        this.realHostPort = realHostPort;
    }

    public Integer getProxyType() {
        return proxyType;
    }

    public void setProxyType(Integer proxyType) {
        this.proxyType = proxyType;
    }

    public String getRealHost() {
        return realHost;
    }

    public void setRealHost(String realHost) {
        this.realHost = realHost;
    }

    public String getAddress() {
        if (realHost == null || realHostPort == null) {
            return null;
        }
        return new StringBuilder(realHost).append(":").append(realHostPort).toString();
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
