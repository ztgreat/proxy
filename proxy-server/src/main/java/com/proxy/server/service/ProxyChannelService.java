package com.proxy.server.service;

import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.server.dao.ProxyChannelDao;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

/**
 * 本地 端口(tcp)/域名 绑定服务层
 * @author  ztgreat
 */
public class ProxyChannelService {


    private static ProxyChannelDao proxyChannelDao =new ProxyChannelDao();

    /**
     * 绑定服务端口
     * @param serverPort 服务器 服务端口
     * @param bootstrap 启动器
     * @throws InterruptedException
     */
    public ChannelFuture bind(Integer serverPort, ServerBootstrap bootstrap, int proxyType, Object saveKey) throws  Exception{
        return proxyChannelDao.bind(serverPort,bootstrap,proxyType,saveKey);
    }

    /**
     * 绑定tcp 服务
     * @param serverPort
     * @param bootstrap
     * @param proxyRealServer
     * @throws Exception
     */
    public  void bindForTCP(Integer serverPort, ServerBootstrap bootstrap, ProxyRealServer proxyRealServer) throws  Exception{
        proxyChannelDao.bindForTCP(serverPort,bootstrap,proxyRealServer);
    }

    /**
     * 解绑 服务器端口
     * @param serverPort 需要解绑的端口
     * @return
     */
    public  boolean unBind(Integer serverPort){

       return proxyChannelDao.unBind(serverPort);
    }

    /**
     * 根据服务端口,返回绑定信息
     * @param serverPort
     * @return
     */
    @Deprecated
    public ProxyChannel getByServerPort(int serverPort){
        return  proxyChannelDao.getByServerPort(serverPort);
    }

    /**
     * 根据服务域名,返回绑定信息
     * @param domain
     * @return
     */
    @Deprecated
    public ProxyChannel getByServerdomain(String domain){
        return  proxyChannelDao.getByServerdomain(domain);
    }


    /**
     * 获取代理信息
     * @param key 可能是服务器端口,也可以是指定的域名
     * @return
     */
    public ProxyChannel getServerProxy(Object key){
        return  proxyChannelDao.getServerProxy(key);
    }

    public  void addByServerdomain(String domain,ProxyRealServer proxyRealServer){
        proxyChannelDao.addByServerdomain(domain,proxyRealServer);
    }

}
