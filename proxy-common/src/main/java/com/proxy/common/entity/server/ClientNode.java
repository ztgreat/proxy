package com.proxy.common.entity.server;


import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 客户端节点
 *
 * @author ztgreat
 */
public class ClientNode {

    /**
     * 节点名称
     */
    private String name;

    /**
     * 客户端key
     */
    private String clientKey;

    /**
     * 客户端地址
     */
    private String host;


    /**
     * 客户端 端口
     */
    private Integer port;

    /**
     * 描述
     */
    private String description;


    /**
     * 客户端与代理服务器的channel
     */
    private Channel channel;

    /**
     * 节点状态
     */
    private int status;

    /**
     * 节点映射关系 ,服务器域名/服务器端口--ProxyRealServer
     */
    private Map<Object, ProxyRealServer> serverPort2RealServer;

    public ClientNode() {
        serverPort2RealServer = new ConcurrentHashMap<Object, ProxyRealServer>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<Object, ProxyRealServer> getServerPort2RealServer() {
        return serverPort2RealServer;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

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

    public void addRealServer(Object saveKey, ProxyRealServer nodeMapping) {
        if (saveKey != null) {
            this.serverPort2RealServer.put(saveKey, nodeMapping);
        }
    }

    public void removeNodeMappings(Integer proxyPort) {
        if (this.serverPort2RealServer.containsKey(proxyPort)) {
            this.serverPort2RealServer.remove(proxyPort);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
