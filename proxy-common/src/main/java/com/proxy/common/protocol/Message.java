package com.proxy.common.protocol;

import io.netty.channel.Channel;

/**
 * 用于 用户和代理服务之间的通信
 * 不用于 代理服务器和客户端之间的通信
 */
public class Message {

    /**
     * 用户ip
     */
    private String ip;

    /**
     * 用户端口
     */
    private Integer uPort;

    /**
     * 服务器端口
     */
    private Integer sPort;

    /**
     * 会话id
     */
    private Long sessionID;

    /**
     * 真实服务器地址
     */
    private String remoteAddress;

    /**
     * 消息类型
     */
    private byte type;

    /**
     * 代理类型
     */
    private byte proxyType;

    /**
     * 优先级
     */
    private byte priority;

    /**
     * 命令
     */
    private byte[] command;

    /**
     * 数据
     */
    private byte[] data;

    /**
     * 客户端 channel
     */
    private Channel clientChannel;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getuPort() {
        return uPort;
    }

    public void setuPort(Integer uPort) {
        this.uPort = uPort;
    }

    public Integer getsPort() {
        return sPort;
    }

    public void setsPort(Integer sPort) {
        this.sPort = sPort;
    }

    public Long getSessionID() {
        return sessionID;
    }

    public void setSessionID(Long sessionID) {
        this.sessionID = sessionID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public Channel getClientChannel() {
        return clientChannel;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    public byte getProxyType() {
        return proxyType;
    }

    public void setProxyType(byte proxyType) {
        this.proxyType = proxyType;
    }
}
