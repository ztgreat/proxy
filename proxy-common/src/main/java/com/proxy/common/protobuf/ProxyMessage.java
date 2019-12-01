package com.proxy.common.protobuf;


import com.proxy.common.protocol.CommonConstant;

/**
 * 代理消息
 */
public class ProxyMessage {

    /**
     * crc 校验
     */
    private Integer crcCode;

    /**
     * 回话id
     */
    private Long sessionID;

    /**
     * 类型
     */
    private Byte type;

    /**
     * 代理类型
     *
     * @see CommonConstant.ProxyType
     */
    private Byte proxyType;

    /**
     * 优先级
     */
    private Byte priority;

    /**
     * 命令
     */
    private byte[] command;

    /**
     * 数据
     */
    private byte[] data;

    public Integer getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(Integer crcCode) {
        this.crcCode = crcCode;
    }

    public Long getSessionID() {
        return sessionID;
    }

    public void setSessionID(Long sessionID) {
        this.sessionID = sessionID;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Byte getProxyType() {
        return proxyType;
    }

    public void setProxyType(Byte proxyType) {
        this.proxyType = proxyType;
    }

    public Byte getPriority() {
        return priority;
    }

    public void setPriority(Byte priority) {
        this.priority = priority;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public static final class Builder {
        Integer crcCode;
        Long sessionID;
        Byte type;
        Byte proxyType;
        Byte priority;
        byte[] command;
        byte[] data;

        private Builder() {
        }

        public static Builder aProxyMessage() {
            return new Builder();
        }

        public Builder crcCode(Integer crcCode) {
            this.crcCode = crcCode;
            return this;
        }

        public Builder sessionID(Long sessionID) {
            this.sessionID = sessionID;
            return this;
        }

        public Builder type(Byte type) {
            this.type = type;
            return this;
        }

        public Builder proxyType(Byte proxyType) {
            this.proxyType = proxyType;
            return this;
        }

        public Builder priority(Byte priority) {
            this.priority = priority;
            return this;
        }

        public Builder command(byte[] command) {
            this.command = command;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public ProxyMessage build() {
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setCrcCode(crcCode);
            proxyMessage.setSessionID(sessionID);
            proxyMessage.setType(type);
            proxyMessage.setProxyType(proxyType);
            proxyMessage.setPriority(priority);
            proxyMessage.setCommand(command);
            proxyMessage.setData(data);
            return proxyMessage;
        }
    }
}
