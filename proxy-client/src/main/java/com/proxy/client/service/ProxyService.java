package com.proxy.client.service;


import com.proxy.client.dao.ProxyDao;
import io.netty.channel.Channel;

public class ProxyService {

    private static ProxyDao proxyDao=new ProxyDao();

    public  void setChannel(Channel channel) {
        proxyDao.setChannel(channel);
    }

    public  Channel getChannel() {
        return proxyDao.getChannel();
    }

    public  Long getRealServerChannelSessionID(Channel realServerChannel) {
        return proxyDao.getRealServerChannelSessionID(realServerChannel);
    }

    public  Channel getRealServerChannel(Long sessionID) {
        return proxyDao.getRealServerChannel(sessionID);
    }

    public  void addRealServerChannel(Long sessionID, Channel realServerChannel,String proxyType,String proxyServer) {
        proxyDao.addRealServerChannel(sessionID,realServerChannel,proxyType,proxyServer);
    }
    public  int getProxyType(Channel realServerChannel) {
        return proxyDao.getProxyType(realServerChannel);
    }
    public  String getProxyServer(Channel realServerChannel) {
        return proxyDao.getProxyServer(realServerChannel);
    }
    public void removeRealServerChannel(Long sessionID){
        proxyDao.removeRealServerChannel(sessionID);

    }
    public void clear(){
        proxyDao.clear();
    }
}
