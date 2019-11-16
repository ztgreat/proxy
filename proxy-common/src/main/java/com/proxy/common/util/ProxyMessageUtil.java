package com.proxy.common.util;

import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;

/**
 * 构建服务器和客户端通信的消息实体
 */
public class ProxyMessageUtil {

    /**
     * 对ProxyMessage 进行 编码
     *
     * @param message 消息
     * @return
     */
    public static byte[] encode(ProxyMessage message) {
        if (message == null) {
            return null;
        }
        return ProtostuffUtil.serialize(message);
    }

    /**
     * 对ProxyMessage 进行 解码
     *
     * @param message 需要解码的字节数组
     * @return ProxyMessage
     */
    public static ProxyMessage decode(byte[] message) {
        return ProtostuffUtil.deserialize(message, ProxyMessage.class);
    }

    /**
     * 构造登录请求消息
     *
     * @param command 命令
     * @param data    数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildLoginReq(byte[] command, byte[] data) {
        return buildMsg(null, CommonConstant.Login.TYPE_LOGIN_REQ, null, null, command, data);
    }

    /**
     * 构造登录响应消息
     *
     * @return ProxyMessage
     */
    public static ProxyMessage buildLoginResp(byte[] command, byte[] data) {
        return buildMsg(null, CommonConstant.Login.TYPE_LOGIN_RESP, null, null, command, data);
    }

    /**
     * 心跳请求消息
     *
     * @return ProxyMessage
     */
    public static ProxyMessage buildHeartBeatReq() {
        return buildMsg(null, CommonConstant.HearBeat.TYPE_HEARTBEAT_REQ, null, null, null, null);
    }

    /**
     * 心跳响应消息
     *
     * @return ProxyMessage
     */
    public static ProxyMessage buildHeartBeatResp() {
        return buildMsg(null, CommonConstant.HearBeat.TYPE_HEARTBEAT_RESP, null, null, null, null);
    }

    /**
     * 连接请求消息
     *
     * @param sessionID 回话id
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildConnect(Long sessionID, byte[] data) {
        return buildMsg(sessionID, CommonConstant.MessageType.TYPE_CONNECT_REALSERVER, null, null, null, data);
    }

    /**
     * 需要用户重新请求建立连接
     *
     * @param sessionID 回话id
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildReConnect(Long sessionID, byte[] data) {
        return buildMsg(sessionID, CommonConstant.MessageType.TYPE_RECONNECT, null, null, null, data);
    }

    /**
     * 连接失败消息
     *
     * @param sessionID 回话id
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildConnectFail(Long sessionID, byte[] data) {
        return buildMsg(sessionID, CommonConstant.MessageType.TYPE_CONNECT_FAIL, null, null, null, data);
    }

    /**
     * 连接成功消息
     *
     * @param sessionID 回话id
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildConnectSuccess(Long sessionID, byte[] data) {
        return buildMsg(sessionID, CommonConstant.MessageType.TYPE_CONNECT_SUCCESS, null, null, null, data);
    }


    /**
     * 连接断开消息
     *
     * @param sessionID 回话id
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildDisConnect(Long sessionID, byte[] data) {
        return buildMsg(sessionID, CommonConstant.MessageType.TYPE_DISCONNECT, null, null, null, data);
    }

    /**
     * 构建通信消息
     *
     * @param sessionID 回话id
     * @param type      消息类型
     * @param priority  优先级
     * @param command   命令
     * @param data      数据
     * @return ProxyMessage
     */
    public static ProxyMessage buildMsg(Long sessionID, Byte type, Byte proxyType,
                                        Byte priority, byte[] command, byte[] data) {
        ProxyMessage.Builder builder = ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.sessionID(sessionID);
        builder.type(type);
        builder.proxyType(proxyType);
        builder.priority(priority);
        builder.command(command);
        builder.data(data);
        return builder.build();
    }
}
