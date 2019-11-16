package com.proxy.server.dao;


import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;
import com.proxy.common.cache.memory.MemoryCacheManager;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * 用户回话 channel的管理
 */
public class UserSessionDao {


    private static CacheManager<Long, Channel> cacheManager = new MemoryCacheManager<Long, Channel>();

    private static Cache<Long, Channel> sessionIDToChannel = cacheManager.getCache("user_session_cache");

    public void add(Long sessionID, Channel channel, ProxyRealServer realServer) {
        sessionIDToChannel.put(sessionID, channel);
        channel.attr(CommonConstant.UserChannelAttributeKey.USER_ID).set(String.valueOf(sessionID));
        channel.attr(CommonConstant.UserChannelAttributeKey.TYPE).set(String.valueOf(realServer.getProxyType()));
        channel.attr(CommonConstant.UserChannelAttributeKey.CLIENT_KEY).set(realServer.getClientKey());
    }

    public void remove(Long sessionID) {
        sessionIDToChannel.remove(sessionID);
    }

    public Channel get(Long sessionID) {
        return sessionIDToChannel.get(sessionID);
    }

    public Long getSessionID(Channel channel) {
        if (channel.attr(CommonConstant.UserChannelAttributeKey.USER_ID).get() == null) {
            return null;
        }
        return Long.valueOf(channel.attr(CommonConstant.UserChannelAttributeKey.USER_ID).get());
    }

    public Integer getType(Channel channel) {
        if (channel.attr(CommonConstant.UserChannelAttributeKey.TYPE).get() == null) {
            return null;
        }
        return Integer.valueOf(channel.attr(CommonConstant.UserChannelAttributeKey.TYPE).get());
    }

    public String getClientKey(Channel channel) {
        if (channel.attr(CommonConstant.UserChannelAttributeKey.CLIENT_KEY).get() == null) {
            return null;
        }
        return channel.attr(CommonConstant.UserChannelAttributeKey.CLIENT_KEY).get();
    }

    public Map<Long, Channel> getAll() {
        return sessionIDToChannel.getAll();
    }
}
