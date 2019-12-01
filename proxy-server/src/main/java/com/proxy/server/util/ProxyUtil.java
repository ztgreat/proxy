package com.proxy.server.util;


import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyUtil {
    private static String PATTERN_IP = "(\\d*\\.){3}\\d*";
    private static Pattern ipPattern = Pattern.compile(PATTERN_IP);

    public static Object getKey(String domain, Channel userChannel) {

        Matcher matcher = ipPattern.matcher(domain);
        if (matcher.find() || domain.startsWith("localhost")) {
            // 通过ip 访问,则返回端口
            InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();
            return sa.getPort();
        }
        String[] all = domain.split(":");
        domain = all[0];
        return domain;

    }

    /**
     * 判断是否是ip地址
     */
    public static boolean isIpAddr(String ip) {
        Matcher matcher = ipPattern.matcher(ip);
        return matcher.find();
    }

}
