package com.proxy.server.handler.traffic;

import java.io.Serializable;

/**
 * 流量统计
 */
public class Traffic implements Serializable {

    private static final long serialVersionUID = 1L;

    private int port;

    private long readBytes;

    private long writeBytes;

    private long readMsgs;

    private long writeMsgs;

    private int channels;

    private long timestamp;

    public long getReadBytes() {
        return readBytes;
    }

    public void setReadBytes(long readBytes) {
        this.readBytes = readBytes;
    }

    public long getWriteBytes() {
        return writeBytes;
    }

    public void setWriteBytes(long wroteBytes) {
        this.writeBytes = wroteBytes;
    }

    public long getReadMsgs() {
        return readMsgs;
    }

    public void setReadMsgs(long readMsgs) {
        this.readMsgs = readMsgs;
    }

    public long getWriteMsgs() {
        return writeMsgs;
    }

    public void setWriteMsgs(long wroteMsgs) {
        this.writeMsgs = wroteMsgs;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}