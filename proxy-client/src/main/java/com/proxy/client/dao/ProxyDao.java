package com.proxy.client.dao;


import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;
import com.proxy.common.cache.memory.MemoryCacheManager;
import com.proxy.common.entity.client.RealServer;
import com.proxy.common.protocol.CommonConstant;
import io.netty.channel.Channel;

public class ProxyDao {

    private static CacheManager<Long, RealServer> cacheManager = new MemoryCacheManager<Long, Channel>();

    private static Cache<Long, RealServer> realServerChannels = cacheManager.getCache("proxy_cache");

    /**
     * 代理客户端和代理服务器的通道
     */
    private volatile Channel channel;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public Long getRealServerChannelSessionID(Channel realServerChannel) {
        return Long.valueOf(realServerChannel.attr(CommonConstant.SESSION_ID).get());
    }

    public RealServer getRealServerChannel(Long sessionID) {
        return realServerChannels.get(sessionID);
    }

    public void addRealServerChannel(Long sessionID, RealServer realServer, Channel realServerChannel, String proxyType, String proxyServer) {
        realServerChannels.put(sessionID, realServer);
        realServerChannel.attr(CommonConstant.SESSION_ID).set(String.valueOf(sessionID));
        realServerChannel.attr(CommonConstant.UserChannelAttributeKey.TYPE).set(proxyType);
        realServerChannel.attr(CommonConstant.UserChannelAttributeKey.PROXYSERVER).set(proxyServer);
    }

    public void removeRealServerChannel(Long sessionID) {
        realServerChannels.remove(sessionID);
    }

    public void clear() {
        realServerChannels.clear();
        this.channel = null;
    }

    public int getProxyType(Channel realServerChannel) {
        return Integer.parseInt(realServerChannel.attr(CommonConstant.UserChannelAttributeKey.TYPE).get());
    }

    public String getProxyServer(Channel realServerChannel) {
        return realServerChannel.attr(CommonConstant.UserChannelAttributeKey.PROXYSERVER).get();
    }
}
