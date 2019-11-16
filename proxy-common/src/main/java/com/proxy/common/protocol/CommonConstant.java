package com.proxy.common.protocol;


import io.netty.util.AttributeKey;

public final class CommonConstant {


    public static final int CRCCODE = 0x10101010;

    public static final AttributeKey<String> SESSION_ID = AttributeKey.newInstance("session_id");

    public static final String SESSION_NAME = "SESSION_ID";

    public static final int DEFAULT_HTTP_PORT = 80;

    /**
     * 用户请求channle AttributeKey
     */
    public static class UserChannelAttributeKey {

        public static final AttributeKey<String> USER_ID = AttributeKey.newInstance("user_id");
        public static final AttributeKey<String> TYPE = AttributeKey.newInstance("type");
        public static final AttributeKey<String> CLIENT_KEY = ServerChannelAttributeKey.CLIENT_KEY;
        public static final AttributeKey<String> PROXYSERVER = ServerChannelAttributeKey.PROXY_SERVER;

        public static final AttributeKey<String> PROXY_KEY = AttributeKey.newInstance("proxy_key");
    }

    /**
     * 代理服务器与客户端channle AttributeKey
     */
    public static class ServerChannelAttributeKey {
        public static final AttributeKey<String> CLIENT_KEY = AttributeKey.newInstance("client_key");
        public static final AttributeKey<String> PROXY_SERVER = AttributeKey.newInstance("proxy_server");
    }

    /**
     * 登录
     */
    public class Login {

        /**
         * 登录成功
         */
        public static final byte LOGIN_SUCCESS = 1;

        /**
         * 登录失败
         */
        public static final byte LOGIN_FAIL = 0;


        /**
         * 登录请求
         */
        public static final byte TYPE_LOGIN_REQ = 1;

        /**
         * 登录响应
         */
        public static final byte TYPE_LOGIN_RESP = 2;

    }

    public class ClientStatus {


        /**
         * 禁止
         */
        public static final int FORBIDDEN = 0;
        /**
         * 客户端有效
         */
        public static final int ACTIVE = 1;

        /**
         * 客户端在线
         */
        public static final int ONLINE = 2;

        /**
         * 客户端离线
         */
        public static final int OFFLINE = 3;

    }

    public class ProxyStatus {

        /**
         * 禁止
         */
        public static final int FORBIDDEN = 0;
        /**
         * 代理有效
         */
        public static final int ACTIVE = 1;

        /**
         * 代理在线
         */
        public static final int ONLINE = 2;

        /**
         * 代理离线
         */
        public static final int OFFLINE = 3;

        /**
         * 连接中
         */
        public static final int CONNECTING = 4;

    }

    public class MessageType {


        /**
         * 通知客户端与真实服务器建立连接
         */
        public static final byte TYPE_CONNECT_REALSERVER = 0x03;

        /**
         * 代理后端服务器建立连接消息
         */
        public static final byte TYPE_CONNECT_SUCCESS = 0x03;

        /**
         * 需要用户重新发起连接请求
         */
        public static final byte TYPE_RECONNECT = 0x04;

        /**
         * 代理客户端和真实服务器断开(连接失败)
         */
        public static final byte TYPE_DISCONNECT = 0x05;

        /**
         * 代理客户端和真实服务器 连接失败
         */
        public static final byte TYPE_CONNECT_FAIL = 0x05;

        /**
         * 代理数据传输
         */
        public static final byte TYPE_TRANSFER = 0x06;
    }

    public class HearBeat {
        /**
         * 心跳响应
         */
        public static final byte TYPE_HEARTBEAT_RESP = 0x0A;

        /**
         * 心跳请求
         */
        public static final byte TYPE_HEARTBEAT_REQ = 0x0B;
    }

    /**
     * 客户端与代理服务器channle AttributeKey
     */
    public class ClientChannelAttributeKey {

    }

    /**
     * 客户端与真实服务器channle AttributeKey
     */
    public class RealServerChannelAttributeKey {

    }

    public class ProxyType {

        /**
         * tcp代理
         */
        public static final int TCP = 1;

        /**
         * http代理
         */
        public static final int HTTP = 2;
    }

    public class HeaderAttr {


        /**
         * 指定 X-Forwarded-For 为本机代理ip地址
         */
        public static final String Forwarded_Default = "default";

        /**
         * 指定 X-Forwarded-For 为随机值
         */
        public static final String Forwarded_Random = "random";

        /**
         * 不修改 X-Forwarded-For 属性
         */
        public static final String Forwarded_None = "none";

    }


}
