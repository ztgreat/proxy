package com.proxy.server;

import com.proxy.common.codec.ProxyMessageDecoder;
import com.proxy.common.codec.ProxyMessageEncoder;
import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.server.handler.*;
import com.proxy.server.service.ConfigService;
import com.proxy.server.service.LifeCycle;
import com.proxy.server.service.LogBackConfigLoader;
import com.proxy.server.service.ServerBeanManager;
import com.proxy.server.task.ExitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ProxyServer implements LifeCycle {

    private static Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    /**
     * 最大帧长度 5M
     */
    private static final int MAX_FRAME_LENGTH = 5 * 1024 * 1024;
    /**
     * 长度域偏移
     */
    private static final int LENGTH_FIELD_OFFSET = 0;
    /**
     * 长度域字节数
     */
    private static final int LENGTH_FIELD_LENGTH = 4;

    /**
     * 绑定端口,默认6666
     */
    private int port;
    /**
     * http 代理通道
     */
    private Integer httpPort;

    /**
     * 服务端channel
     */
    public Channel channel;


    public ProxyServer() {
        this.port = 6666;
    }

    public static void main(String[] args) throws Exception {

        //加载日志
        LogBackConfigLoader.load();
        try {
            //退出钩子
            Runtime.getRuntime().addShutdownHook(new ExitHandler());
            //创建代理服务器,如果没有指定端口，则默认使用 6666 端口
            ProxyServer proxyServer = new ProxyServer();
            //将代理服务保存,方便后续使用
            ServerBeanManager.setProxyServer(proxyServer);
            //开启代理服务
            proxyServer.start();
        } catch (Exception e) {
            logger.error("启动代理服务失败：", e);
        }

    }

    private ChannelFuture bind() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                        socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(10 * 6, 15 * 6, 20 * 6));
                        socketChannel.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH));
                        socketChannel.pipeline().addLast(new ProxyMessageEncoder());
                        socketChannel.pipeline().addLast(new LoginAuthRespHandler());
                        socketChannel.pipeline().addLast(new HeartBeatRespHandler());
                        socketChannel.pipeline().addLast(new ServerChannelHandler());
                    }
                });
        ServerBeanManager.setBootstrap(bootstrap);
        ChannelFuture future = null;
        try {
            future = bootstrap.bind(port);

            future.channel().closeFuture().addListeners((ChannelFutureListener) channelFuture -> {
                logger.info("等待代理服务退出...");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });

            logger.info("服务器监听端口 {}", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return future;
    }

    private void start() {

        //读取代理服务配置文件
        ServerBeanManager.getConfigService().readServerConfig();

        ConfigService configService = ServerBeanManager.getConfigService();

        // 获取端口
        if (configService.getConfigure("port") != null) {
            this.port = (int) configService.getConfigure("port");
        }

        //启动http 转发服务
        if (configService.getConfigure("httpPort") != null) {
            this.httpPort = (int) configService.getConfigure("httpPort");
        }

        //配置代理信息
        configurProxy();

        ChannelFuture mainFuture = null;

        ChannelFuture httpFuture = null;
        try {
            //启动主程序
            mainFuture = startMainServer();

            //启动http服务(如果有配置)
            httpFuture = startHttpServer();

            // 启动转发服务，暂定这样
            ServerBeanManager.getTransferService().start();

            mainFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            if (mainFuture != null) {
                mainFuture.channel().close();
            }
            if (httpFuture != null) {
                httpFuture.channel().close();
            }
            logger.error("代理服务 启动失败：", e);
        }
    }

    private ChannelFuture startMainServer() {

        //根据配置文件启动服务
        ChannelFuture future = bind();
        this.channel = future.channel();
        return future;
    }

    /**
     * 配置代理信息
     */
    private void configurProxy() {
        //获取客户端配置信息
        Map<String, List<Map<String, Object>>> nodes = (Map<String, List<Map<String, Object>>>) ServerBeanManager.getConfigService().getConfigure("client");
        for (Map.Entry<String, List<Map<String, Object>>> m : nodes.entrySet()) {
            ClientNode clientNode = new ClientNode();
            clientNode.setClientKey(m.getKey());
            clientNode.setStatus(CommonConstant.ClientStatus.ACTIVE);
            List<Map<String, Object>> reals = m.getValue();
            for (Map<String, Object> real : reals) {
                ProxyRealServer proxy = new ProxyRealServer();
                proxy.setClientKey(m.getKey());
                proxy.setRealHost((String) real.get("realhost"));
                proxy.setRealHostPort((Integer) real.get("realhostport"));
                proxy.setDescription((String) real.get("description"));
                String proxyType = (String) real.get("proxyType");
                if (proxyType.equalsIgnoreCase("http")) {
                    buildHttp(proxy, real, clientNode);
                    continue;
                }
                if (proxyType.equalsIgnoreCase("tcp")) {
                    buildTcp(proxy, real, clientNode);
                    continue;
                }
                logger.warn("目前只支持http,tcp,不支持:{}", proxyType);
            }
            ServerBeanManager.getClientService().add(clientNode.getClientKey(), clientNode);
        }
    }

    /**
     * 构建tcp 代理信息
     *
     * @param proxy      真实服务
     * @param real       配置信息
     * @param clientNode 客户端节点
     */
    private void buildTcp(ProxyRealServer proxy, Map<String, Object> real, ClientNode clientNode) {
        proxy.setProxyType(CommonConstant.ProxyType.TCP);
        proxy.setServerPort((Integer) real.get("serverport"));
        clientNode.addRealServer(proxy.getServerPort(), proxy);
    }

    /**
     * 构建 http 代理信息
     *
     * @param proxy      真实服务
     * @param real       配置信息
     * @param clientNode 客户端节点
     */
    private void buildHttp(ProxyRealServer proxy, Map<String, Object> real, ClientNode clientNode) {

        proxy.setDomain((String) real.get("domain"));
        Integer serverport = (Integer) real.get("serverport");
        String domain = proxy.getDomain();

        if (domain != null && this.httpPort == null) {
            logger.error("配置文件出错,http域名代理需要配置httpPort端口");
            throw new RuntimeException();
        }

        if (StringUtils.isBlank(domain) && serverport == null) {
            logger.error("配置文件出错,http代理至少要有serverport或者domain一种");
            throw new RuntimeException();
        }
        proxy.setServerPort(serverport);
        proxy.setProxyType(CommonConstant.ProxyType.HTTP);
        clientNode.addRealServer(proxy.getDomain() == null ? serverport : proxy.getDomain(), proxy);
    }

    private ChannelFuture startHttpServer() throws RuntimeException {

        if (httpPort == null) {
            return null;
        }

        //启动http 转发服务

        //绑定客户端服务端口
        NioEventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
        NioEventLoopGroup serverBossGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        //暂时先关闭
                        //ch.pipeline().addLast(SharableHandlerManager.getTrafficLimitHandler());
                        //ch.pipeline().addLast(SharableHandlerManager.getTrafficCollectionHandler());
                        //http请求消息解码器
                        ch.pipeline().addLast("httpDecoder", new HttpRequestDecoder());
                        ch.pipeline().addLast("connectHandler", new HttpNoticeChannelHandler());
                        //解析 HTTP POST 请求
                        ch.pipeline().addLast("httpObject", new HttpObjectAggregator(2 * 1024 * 1024));
                        ch.pipeline().addLast("transferHandler", new HttpChannelHandler());
                    }
                });
        try {
            //绑定服务端口,会更新代理状态
            ChannelFuture future = ServerBeanManager.getProxyChannelService().bind(this.httpPort, bootstrap, CommonConstant.ProxyType.HTTP, this.httpPort);
            future.channel().closeFuture().addListeners((ChannelFutureListener) channelFuture -> {
                logger.info("等待http代理服务退出...");
                serverWorkerGroup.shutdownGracefully();
                serverBossGroup.shutdownGracefully();
            });
            return future;
        } catch (Exception e) {
            logger.error("http服务端口 {} 绑定失败:" + e.getMessage(), this.httpPort);
            throw new RuntimeException();
        }
    }

    @Override
    public void shutDown() {
        try {
            this.channel.close();
            logger.debug("{}端口:代理服务退出:", this.port);
        } catch (Exception e) {
            logger.error("代理服务退出异常:", e);
        }
    }
}
