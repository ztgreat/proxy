package com.proxy.server.service;


import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.server.dao.UserSessionDao;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * 用户session channel的管理
 */
public class UserSessionService {

    private static UserSessionDao userSessionDao = new UserSessionDao();

    public void add(Long sessionID, Channel channel, ProxyRealServer realServer) {
        userSessionDao.add(sessionID, channel, realServer);
    }

    public void remove(Long sessionID) {
        if (sessionID != null)
            userSessionDao.remove(sessionID);
    }

    public Channel get(Long sessionID) {
        if (sessionID != null)
            return userSessionDao.get(sessionID);
        return null;
    }

    public Long getSessionID(Channel channel) {
        if (channel != null)
            return userSessionDao.getSessionID(channel);
        return null;
    }

    public Integer getType(Channel channel) {
        if (channel != null)
            return userSessionDao.getType(channel);
        return null;
    }

    public String getClientKey(Channel channel) {
        if (channel != null)
            return userSessionDao.getClientKey(channel);
        return null;
    }

    public Map<Long, Channel> getAll() {
        return userSessionDao.getAll();
    }
}
