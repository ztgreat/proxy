package com.proxy.server.service;

import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.server.dao.ProxyChannelDao;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 本地 端口(tcp)/域名 绑定服务层
 *
 * @author ztgreat
 */
public class ProxyChannelService implements LifeCycle {

    private static Logger logger = LoggerFactory.getLogger(ProxyChannelService.class);

    private static ProxyChannelDao proxyChannelDao = new ProxyChannelDao();

    /**
     * 绑定服务端口
     *
     * @param serverPort 服务器 服务端口
     * @param bootstrap  启动器
     * @throws InterruptedException
     */
    public ChannelFuture bind(Integer serverPort, ServerBootstrap bootstrap, int proxyType, Object saveKey) throws Exception {
        return proxyChannelDao.bind(serverPort, bootstrap, proxyType, saveKey);
    }

    /**
     * 绑定tcp 服务
     *
     * @param serverPort      服务端口
     * @param bootstrap       启动器
     * @param proxyRealServer 真正提供服务的类
     * @throws Exception
     */
    public void bindForTCP(Integer serverPort, ServerBootstrap bootstrap, ProxyRealServer proxyRealServer) throws Exception {
        proxyChannelDao.bindForTCP(serverPort, bootstrap, proxyRealServer);
    }

    /**
     * 解绑 服务器端口
     *
     * @param serverPort 需要解绑的端口
     * @return boolean
     */
    public boolean unBind(Integer serverPort) {

        return proxyChannelDao.unBind(serverPort);
    }

    /**
     * 根据服务端口,返回绑定信息
     *
     * @param serverPort 服务端口
     * @return ProxyChannel
     */
    @Deprecated
    public ProxyChannel getByServerPort(int serverPort) {
        return proxyChannelDao.getByServerPort(serverPort);
    }

    /**
     * 根据服务域名,返回绑定信息
     *
     * @param domain
     * @return ProxyChannel
     */
    @Deprecated
    public ProxyChannel getByServerdomain(String domain) {
        return proxyChannelDao.getByServerdomain(domain);
    }


    /**
     * 获取代理信息
     *
     * @param key 可能是服务器端口,也可以是指定的域名
     * @return ProxyChannel
     */
    public ProxyChannel getServerProxy(Object key) {
        return proxyChannelDao.getServerProxy(key);
    }

    public void addByServerdomain(String domain, ProxyRealServer proxyRealServer) {
        proxyChannelDao.addByServerdomain(domain, proxyRealServer);
    }

    public Map<Object, ProxyChannel> getAll() {
        return proxyChannelDao.getAll();
    }

    @Override
    public void shutDown() {

        try {
            Map<Object, ProxyChannel> map = this.getAll();
            for (Map.Entry<Object, ProxyChannel> entry : map.entrySet()) {
                entry.getValue().getChannel().close();
                logger.debug("代理服务(端口/域名):{} 退出", entry.getKey());
            }
        } catch (Exception ignored) {

        }
    }
}
