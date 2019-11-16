package com.proxy.server.event;


/**
 * 通知:代理服务器开始向客户端发送数据
 */
public class TransferEvent {

    private boolean transfer;

    public TransferEvent() {
        this.transfer = true;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }
}
