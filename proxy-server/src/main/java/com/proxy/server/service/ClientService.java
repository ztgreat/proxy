package com.proxy.server.service;

import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.server.dao.ClientDao;
import com.proxy.server.handler.HttpChannelHandler;
import com.proxy.server.handler.HttpNoticeChannelHandler;
import com.proxy.server.handler.TCPChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 代理客户端 管理
 */
public class ClientService {

    private static Logger logger = LoggerFactory.getLogger(ClientService.class);

    private static ClientDao clientDao = new ClientDao();

    public void add(String clientKey, ClientNode node) {

        //1.添加客户端
        clientDao.add(clientKey, node);
        if (node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
            return;
        }
        //2.开始为客户端绑定服务端口

        //绑定客户端服务端口
        Map<Object, ProxyRealServer> keyToNode = node.getServerPort2RealServer();
        for (Map.Entry<Object, ProxyRealServer> keyToProxyRealServer : keyToNode.entrySet()) {

            ProxyRealServer proxyRealServer = keyToProxyRealServer.getValue();
            /**
             * 如果是HTTP代理,并且设置了通过域名访问,则不需要单独绑定端口
             */
            if (proxyRealServer.getProxyType() == CommonConstant.ProxyType.HTTP && StringUtils.isNotBlank(proxyRealServer.getDomain())) {
                ServerBeanManager.getProxyChannelService().addByServerdomain(proxyRealServer.getDomain(), proxyRealServer);
                proxyRealServer.setStatus(CommonConstant.ProxyStatus.ONLINE);
                continue;
            }

            if (proxyRealServer.getProxyType() == CommonConstant.ProxyType.HTTP) {

                //http 端口代理绑定
                HttpProxy(keyToProxyRealServer.getKey(), proxyRealServer);

            } else if (proxyRealServer.getProxyType() == CommonConstant.ProxyType.TCP) {
                //tcp 端口代理绑定
                TCPProxy(keyToProxyRealServer.getKey(), proxyRealServer);
            }

        }

    }

    /**
     * 绑定tcp 代理
     *
     * @param key             端口
     * @param proxyRealServer 真正的服务
     */
    private void TCPProxy(Object key, ProxyRealServer proxyRealServer) {

        NioEventLoopGroup serverWorkerGroup;
        NioEventLoopGroup serverBossGroup;
        int serverPort = 0;
        if (key instanceof Integer) {
            serverPort = (int) key;
            if (ServerBeanManager.getProxyChannelService().getServerProxy(serverPort) != null && ServerBeanManager.getProxyChannelService().getServerProxy(serverPort).getStatus() == CommonConstant.ProxyStatus.ONLINE) {
                logger.error("服务端口 {} 已经被绑定了", key);
            }
            serverBossGroup = new NioEventLoopGroup();
            serverWorkerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {

                            ch.pipeline().addLast(SharableHandlerManager.getTrafficLimitHandler());
                            ch.pipeline().addLast(SharableHandlerManager.getTrafficCollectionHandler());
                            ch.pipeline().addLast(new TCPChannelHandler());
                        }
                    });
            try {
                //绑定服务端口,会更新代理状态
                ServerBeanManager.getProxyChannelService().bindForTCP(serverPort, bootstrap, proxyRealServer);
            } catch (Exception e) {
                logger.error("服务端口 {} 绑定失败:" + e.getMessage(), key);
            }
        }
    }

    /**
     * 绑定http代理
     *
     * @param key             端口
     * @param proxyRealServer 真正的服务
     */
    private void HttpProxy(Object key, ProxyRealServer proxyRealServer) {

        NioEventLoopGroup serverWorkerGroup;
        NioEventLoopGroup serverBossGroup;

        int serverPort = 0;
        if (key instanceof Integer) {
            serverPort = (int) key;
            if (ServerBeanManager.getProxyChannelService().getServerProxy(serverPort) != null && ServerBeanManager.getProxyChannelService().getServerProxy(serverPort).getStatus() == CommonConstant.ProxyStatus.ONLINE) {
                logger.error("服务端口 {} 已经被绑定了", key);
            }
            serverBossGroup = new NioEventLoopGroup();
            serverWorkerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(SharableHandlerManager.getTrafficLimitHandler());
                            ch.pipeline().addLast(SharableHandlerManager.getTrafficCollectionHandler());
                            //HttpRequestDecoder http请求消息解码器
                            ch.pipeline().addLast("httpDecoder", new HttpRequestDecoder());
                            ch.pipeline().addLast("connectHandler", new HttpNoticeChannelHandler());
                            //解析 HTTP POST 请求
                            ch.pipeline().addLast("httpObject", new HttpObjectAggregator(2 * 1024 * 1024));
                            ch.pipeline().addLast("transferHandler", new HttpChannelHandler());

                        }
                    });
            try {
                //绑定服务端口,会更新代理状态
                ServerBeanManager.getProxyChannelService().bindForTCP(serverPort, bootstrap, proxyRealServer);
            } catch (Exception e) {
                logger.error("服务端口 {} 绑定失败:" + e.getMessage(), key);
            }
        }

    }


    public String getClientKey(Channel channel) {
        return clientDao.getClientKey(channel);
    }

    public void delete(String clientKey) {
        if (clientKey != null)
            clientDao.remove(clientKey);
    }

    public void setNodeStatus(String clientKey, Integer status) {
        clientDao.setNodeStatus(clientKey, status);
    }

    public ClientNode get(String clientKey) {
        if (clientKey != null)
            return clientDao.get(clientKey);
        return null;
    }

    public void setNodeChannle(String clientKey, Channel channel) {
        clientDao.setNodeChannle(clientKey, channel);
    }

    public Map<String, ClientNode> getAllNode() {
        return clientDao.getAll();
    }

}
