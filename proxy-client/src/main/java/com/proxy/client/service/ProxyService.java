package com.proxy.client.service;


import com.proxy.client.dao.ProxyDao;
import com.proxy.common.entity.client.RealServer;
import io.netty.channel.Channel;

public class ProxyService {

    private static ProxyDao proxyDao = new ProxyDao();

    public void setChannel(Channel channel) {
        proxyDao.setChannel(channel);
    }

    public Channel getChannel() {
        return proxyDao.getChannel();
    }

    public Long getRealServerChannelSessionID(Channel realServerChannel) {
        return proxyDao.getRealServerChannelSessionID(realServerChannel);
    }

    public RealServer getRealServerChannel(Long sessionID) {
        return proxyDao.getRealServerChannel(sessionID);
    }

    public void addRealServerChannel(Long sessionID, RealServer realServer, Channel realServerChannel, String proxyType, String proxyServer) {
        proxyDao.addRealServerChannel(sessionID, realServer, realServerChannel, proxyType, proxyServer);
    }

    public int getProxyType(Channel realServerChannel) {
        return proxyDao.getProxyType(realServerChannel);
    }

    public String getProxyServer(Channel realServerChannel) {
        return proxyDao.getProxyServer(realServerChannel);
    }

    public void removeRealServerChannel(Long sessionID) {
        proxyDao.removeRealServerChannel(sessionID);

    }

    public void clear() {
        proxyDao.clear();
    }
}
