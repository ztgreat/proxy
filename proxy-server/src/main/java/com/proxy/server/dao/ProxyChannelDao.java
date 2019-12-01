package com.proxy.server.dao;

import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;
import com.proxy.common.cache.memory.MemoryCacheManager;
import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 本地 端口(tcp)/域名 绑定 具体操作层
 *
 * @author ztgreat
 */
public class ProxyChannelDao {

    private static Logger logger = LoggerFactory.getLogger(ProxyChannelDao.class);

    private static CacheManager<Object, ProxyChannel> cacheManager = new MemoryCacheManager<Object, ProxyChannel>();

    /**
     * 服务器端口/访问域名 -- 代理通道 映射
     */
    private static Cache<Object, ProxyChannel> proxyChannelCache = cacheManager.getCache("proxy_cache");

    /**
     * 绑定服务端口
     *
     * @param serverPort 服务器 服务端口
     * @param bootstrap  启动器
     */
    public ChannelFuture bind(final Integer serverPort, final ServerBootstrap bootstrap, int proxyType, Object saveKey) {

        return bootstrap.bind(serverPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {

                if (channelFuture.isSuccess()) {
                    logger.info("绑定本地服务端口({})成功", serverPort);
                    //绑定成功
                    ProxyChannel proxyChannel = new ProxyChannel();
                    proxyChannel.setPort(serverPort);
                    proxyChannel.setChannel(channelFuture.channel());
                    proxyChannel.setBootstrap(bootstrap);
                    proxyChannel.setStatus(CommonConstant.ProxyStatus.ONLINE);
                    proxyChannel.setProxyType(proxyType);

                    proxyChannelCache.put(saveKey, proxyChannel);

                } else {
                    logger.error("绑定本地服务端口{}失败", serverPort);
                }

            }
        });

    }

    /**
     * 解绑 服务器端口
     *
     * @param serverPort 需要解绑的端口
     */
    public boolean unBind(Integer serverPort) {
        if (serverPort == null) {
            return false;
        }
        ProxyChannel proxyChannel = proxyChannelCache.get(serverPort);
        if (proxyChannel == null) {
            return false;
        }
        if (proxyChannel.getChannel() != null && proxyChannel.getChannel().isActive()) {
            proxyChannel.getChannel().close();
        }
        //不移除
        //proxyChannelCache.remove(serverPort);
        return true;
    }

    /**
     * 根据服务端口,返回绑定信息
     */
    public ProxyChannel getByServerPort(int serverPort) {
        return proxyChannelCache.get(serverPort);
    }

    /**
     * 根据服务域名,返回绑定信息
     */
    public ProxyChannel getByServerdomain(String domain) {
        return proxyChannelCache.get(domain);
    }


    public void bindForTCP(Integer serverPort, ServerBootstrap bootstrap, ProxyRealServer proxyRealServer) {

        bootstrap.bind(serverPort).addListener((ChannelFutureListener) channelFuture -> {

            if (channelFuture.isSuccess()) {
                logger.info("绑定本地服务端口({})成功 客户端({})--{}", serverPort, proxyRealServer.getClientKey(), proxyRealServer.getDescription());
                //绑定成功
                ProxyChannel proxyChannel = new ProxyChannel();
                proxyChannel.setPort(serverPort);
                proxyChannel.setChannel(channelFuture.channel());
                proxyChannel.setBootstrap(bootstrap);
                proxyChannel.setClientKey(proxyRealServer.getClientKey());
                proxyChannel.setProxyType(CommonConstant.ProxyType.TCP);
                proxyChannelCache.put(serverPort, proxyChannel);

                //设置状态
                proxyRealServer.setStatus(CommonConstant.ProxyStatus.ONLINE);
            } else {
                logger.error("绑定本地服务端口{}失败", serverPort);
            }

        });
    }

    public void addByServerdomain(String domain, ProxyRealServer proxyRealServer) {

        ProxyChannel proxyChannel = new ProxyChannel();
        proxyChannel.setClientKey(proxyRealServer.getClientKey());
        proxyChannel.setProxyType(CommonConstant.ProxyType.HTTP);
        proxyChannelCache.put(domain, proxyChannel);
    }

    public ProxyChannel getServerProxy(Object key) {
        return proxyChannelCache.get(key);
    }

    public Map<Object, ProxyChannel> getAll() {
        return proxyChannelCache.getAll();
    }
}
