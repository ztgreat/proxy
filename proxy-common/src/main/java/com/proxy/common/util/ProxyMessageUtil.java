package com.proxy.common.util;

import com.google.protobuf.ByteString;
import com.proxy.common.protobuf.ProxyMessage;
import com.proxy.common.protocol.CommonConstant;

/**
 * 构建服务器和客户端通信的消息实体
 */
public class ProxyMessageUtil {

    /**
     * 对ProxyMessage 进行 编码
     * @param message 消息
     * @return
     */
    public static  byte[] encode(ProxyMessage message){
        if (message==null)
            return null;
        return ProtostuffUtil.serialize(message);
    }

    /**
     * 对ProxyMessage 进行 解码
     * @param message
     * @return
     */
    public  static ProxyMessage decode(byte[] message) {
        return ProtostuffUtil.deserialize(message,ProxyMessage.class);
    }

    /**
     * 构造登录请求消息
     * @param command 命令
     * @param data 数据
     * @return
     */
    public  static ProxyMessage buildLoginReq(byte[] command,byte[] data){
        ProxyMessage.Builder builder= ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.Login.TYPE_LOGIN_REQ});
        if (command!=null)
            builder.command(command);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }

    /**
     * 构造登录响应消息
     * @return
     */
    public  static ProxyMessage buildLoginResp(byte[] command,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.Login.TYPE_LOGIN_RESP});

        if (command!=null)
            builder.command(command);

        if (data!=null)
            builder.data(data);
        return  builder.build();
    }

    /**
     * 心跳请求消息
     * @return
     */
    public  static ProxyMessage buildHeartBeatReq(){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.HearBeat.TYPE_HEARTBEAT_REQ});
        return  builder.build();
    }

    /**
     * 心跳响应消息
     * @return
     */
    public  static ProxyMessage buildHeartBeatResp(){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.HearBeat.TYPE_HEARTBEAT_RESP});
        return  builder.build();
    }

    /**
     * 连接请求消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessage buildConnect(Long sessionID,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_REALSERVER});
        builder.sessionID(sessionID);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }

    /**
     * 需要用户重新请求建立连接
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessage buildReConnect(Long sessionID,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.MessageType.TYPE_RECONNECT});
        builder.sessionID(sessionID);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }

    /**
     * 连接失败消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessage buildConnectFail(Long sessionID,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_FAIL});
        builder.type(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_FAIL});
        builder.sessionID(sessionID);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }
    /**
     * 连接成功消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessage buildConnectSuccess(Long sessionID,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_SUCCESS});
        builder.sessionID(sessionID);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }


    /**
     * 连接断开消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessage buildDisConnect(Long sessionID,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.type(new byte[]{CommonConstant.MessageType.TYPE_DISCONNECT});
        builder.sessionID(sessionID);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }

    /**
     * 构建通信消息
     * @param sessionID
     * @param type
     * @param priority
     * @param command
     * @param data
     * @return
     */
    public  static ProxyMessage buildMsg(Long sessionID,Byte type,Byte proxyType,
                                                            Byte priority,byte[] command,byte[] data){
        ProxyMessage.Builder builder=ProxyMessage.Builder.aProxyMessage();
        builder.crcCode(CommonConstant.CRCCODE);
        builder.sessionID(sessionID);
        builder.type(new byte[]{type});
        if (proxyType!=null){
            builder.proxyType(new byte[]{proxyType});
        }
        if (priority!=null)
            builder.priority(new byte[]{priority});
        if (command!=null)
            builder.command(command);
        if (data!=null)
            builder.data(data);
        return  builder.build();
    }
}
