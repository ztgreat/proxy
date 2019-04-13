package com.proxy.common.protobuf;


/**
 * 代理消息
 */
public class ProxyMessage {

    /**
     * crc 校验
     */
    Integer crcCode ;

    /**
     * 回话id
     */
    Long sessionID;

    /**
     * 类型
     */
    byte[] type ;

    /**
     * 代理类型
     */
    byte[] proxyType;

    /**
     * 优先级
     */
    byte[] priority;

    /**
     * 命令
     */
    byte[] command;

    /**
     * 数据
     */
    byte[] data;


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

    public byte[] getType() {
        return type;
    }

    public void setType(byte[] type) {
        this.type = type;
    }

    public byte[] getProxyType() {
        return proxyType;
    }

    public void setProxyType(byte[] proxyType) {
        this.proxyType = proxyType;
    }

    public byte[] getPriority() {
        return priority;
    }

    public void setPriority(byte[] priority) {
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
        Integer crcCode ;
        Long sessionID;
        byte[] type ;
        byte[] proxyType;
        byte[] priority;
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

        public Builder type(byte[] type) {
            this.type = type;
            return this;
        }

        public Builder proxyType(byte[] proxyType) {
            this.proxyType = proxyType;
            return this;
        }

        public Builder priority(byte[] priority) {
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
