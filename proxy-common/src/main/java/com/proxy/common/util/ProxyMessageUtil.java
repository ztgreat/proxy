package com.proxy.common.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.proxy.common.protobuf.ProxyMessageProtos;
import com.proxy.common.protocol.CommonConstant;

/**
 * 构建服务器和客户端通信的消息实体
 */
public class ProxyMessageUtil {

    /**
     * 对ProxyMessageProtos message 进行 编码
     * @param message
     * @return
     */
    public static  byte[] encode(ProxyMessageProtos.ProxyMessage message){
        if (message==null)
            return null;
        return message.toByteArray();
    }

    /**
     * 对ProxyMessageProtos message 进行 解码
     * @param message
     * @return
     * @throws InvalidProtocolBufferException
     */
    public  static ProxyMessageProtos.ProxyMessage decode(byte[] message) throws InvalidProtocolBufferException {
        return ProxyMessageProtos.ProxyMessage.parseFrom(message);
    }

    /**
     * 构造登录请求消息
     * @param command
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildLoginReq(byte[] command,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.Login.TYPE_LOGIN_REQ});
        builder.setType(type);
        if (command!=null)
            builder.setCommand(ByteString.copyFrom(command));
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }

    /**
     * 构造登录响应消息
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildLoginResp(byte[] command,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.Login.TYPE_LOGIN_RESP});
        builder.setType(type);

        if (command!=null)
            builder.setCommand(ByteString.copyFrom(command));

        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }

    /**
     * 心跳请求消息
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildHeartBeatReq(){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.HearBeat.TYPE_HEARTBEAT_REQ});
        builder.setType(type);
        return  builder.build();
    }

    /**
     * 心跳响应消息
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildHeartBeatResp(){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.HearBeat.TYPE_HEARTBEAT_RESP});
        builder.setType(type);
        return  builder.build();
    }

    /**
     * 连接请求消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildConnect(Long sessionID,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_REALSERVER});
        builder.setType(type);
        builder.setSessionID(sessionID);
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }

    /**
     * 需要用户重新请求建立连接
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildReConnect(Long sessionID,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_RECONNECT});
        builder.setType(type);
        builder.setSessionID(sessionID);
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }

    /**
     * 连接失败消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildConnectFail(Long sessionID,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_FAIL});
        builder.setType(type);
        builder.setSessionID(sessionID);
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }
    /**
     * 连接成功消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildConnectSuccess(Long sessionID,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_CONNECT_SUCCESS});
        builder.setType(type);
        builder.setSessionID(sessionID);
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }


    /**
     * 连接断开消息
     * @param sessionID
     * @param data
     * @return
     */
    public  static ProxyMessageProtos.ProxyMessage buildDisConnect(Long sessionID,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        ByteString type=ByteString.copyFrom(new byte[]{CommonConstant.MessageType.TYPE_DISCONNECT});
        builder.setType(type);
        builder.setSessionID(sessionID);
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
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
    public  static ProxyMessageProtos.ProxyMessage buildMsg(Long sessionID,Byte type,Byte proxyType,
                                                            Byte priority,byte[] command,byte[] data){
        ProxyMessageProtos.ProxyMessage.Builder builder=ProxyMessageProtos.ProxyMessage.newBuilder();
        builder.setCrcCode(CommonConstant.CRCCODE);
        builder.setSessionID(sessionID);
        ByteString typeString=ByteString.copyFrom(new byte[]{type});
        builder.setType(typeString);
        if (proxyType!=null){
            ByteString proxyTypeString=ByteString.copyFrom(new byte[]{proxyType});
            builder.setProxyType(proxyTypeString);
        }
        if (priority!=null)
            builder.setPriority(ByteString.copyFrom(new byte[]{priority}));
        if (command!=null)
            builder.setCommand(ByteString.copyFrom(command));
        if (data!=null)
            builder.setData(ByteString.copyFrom(data));
        return  builder.build();
    }
}
