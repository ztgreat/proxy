package com.proxy.client;

import com.proxy.client.handler.*;
import com.proxy.client.service.ClientBeanManager;
import com.proxy.client.service.LogBackConfigLoader;
import com.proxy.common.codec.ProxyMessageDecoder;
import com.proxy.common.codec.ProxyMessageEncoder;
import com.proxy.common.codec.http.MyHttpObjectAggregator;
import com.proxy.common.codec.http.MyHttpRequestDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClient {


    private static Logger logger = LoggerFactory.getLogger(ProxyClient.class);


    /**
     * 服务器地址,默认127.0.0.1
     */
    private String host;


    /**
     * 服务器端口,默认6666
     */
    private int port;

    /**
     * 5M
     */
    private int maxContentLength = 5 * 1024 * 1024;


    /**
     * 客户端启动器
     */
    private Bootstrap clientBootstrap;


    /**
     * 连接真实服务器启动器,使用时才初始化
     */
    private static Bootstrap realServerBootstrap;

    /**
     * NioEventLoopGroup可以理解为一个线程池,
     * 内部维护了一组线程，每个线程负责处理多个Channel上的事件,
     * 而一个Channel只对应于一个线程，这样可以回避多线程下的数据同步问题。
     */
    private NioEventLoopGroup clientGroup;

    /**
     * 用于真实服务器,使用时 才初始化
     */
    private NioEventLoopGroup realServerGroup;


    public ProxyClient() {
        this.host = "127.0.0.1";
        this.port = 6666;
        clientBootstrap = new Bootstrap();
        clientGroup = new NioEventLoopGroup();
    }

    public ProxyClient(String host, int port) {
        this.host = host;
        this.port = port;
        clientBootstrap = new Bootstrap();
        clientGroup = new NioEventLoopGroup();
    }


    /**
     * 连接代理服务器
     */
    private void start() throws InterruptedException {

        initRealServerBoot();

        clientBootstrap.group(clientGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    //初始化时将handler设置到ChannelPipeline
                    @Override
                    public void initChannel(SocketChannel ch) {
                        //ch.pipeline().addLast("logs", new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(10 * 3, 15 * 3, 20 * 3));
                        ch.pipeline().addLast(new ProxyMessageDecoder(2 * 1024 * 1024, 0, 4));
                        ch.pipeline().addLast(new ProxyMessageEncoder());
                        ch.pipeline().addLast(new LoginAuthReqHandler());
                        ch.pipeline().addLast(new HeartBeatReqHandler());
                        ch.pipeline().addLast(new ClientHandler(realServerBootstrap));
                        ch.pipeline().addLast(new MyHttpRequestDecoder());
                        ch.pipeline().addLast(new MyHttpObjectAggregator(maxContentLength));
                        ch.pipeline().addLast(new HttpReceiveHandler());
                    }
                });

        /**
         * 最多尝试5次和服务端连接(总计数,不是连续尝试次数)
         */
        doConnect(5);

        try {
            clear();
        } catch (Exception ignored) {

        }

    }

    /**
     * 初始化 连接后端真正服务器
     */
    private void initRealServerBoot() {

        //初始化
        realServerBootstrap = new Bootstrap();
        realServerGroup = new NioEventLoopGroup();


        realServerBootstrap.group(realServerGroup);
        realServerBootstrap.channel(NioSocketChannel.class);
        realServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new TCPHandler());
                ch.pipeline().addLast(new HttpResponseDecoder());
                ch.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
                ch.pipeline().addLast(new HttpSendHandler());
            }
        });
    }

    private void doConnect(int retry) throws InterruptedException {

        if (retry == 0)
            return;
        ChannelFuture future = clientBootstrap.connect(host, port);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) {
                if (futureListener.isSuccess()) {
                    Channel channel = futureListener.channel();
                    logger.info("连接服务器({})成功", host);

                    channel.closeFuture().addListeners((ChannelFutureListener) channelFuture -> {

                        //channel 关闭后的操作

                    });

                } else {
                    logger.info("连接服务器({}) 失败,10s后尝试重连", host);
                }
            }
        });
        future.channel().closeFuture().sync();
        Thread.sleep(10000);
        doConnect(retry - 1);
    }

    private void clear() {
        ClientBeanManager.getProxyService().clear();
        clientGroup.shutdownGracefully();
        realServerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {

        //加载日志
        LogBackConfigLoader.load();
        String host = ClientBeanManager.getConfigService().readConfig().get("server.host");
        String port = ClientBeanManager.getConfigService().readConfig().get("server.port");
        new ProxyClient(host, Integer.parseInt(port)).start();
    }

}
