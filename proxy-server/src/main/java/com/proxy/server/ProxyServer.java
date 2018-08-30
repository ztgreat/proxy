package com.proxy.server;

import com.proxy.common.codec.ProxyMessageDecoder;
import com.proxy.common.codec.ProxyMessageEncoder;
import com.proxy.common.entity.ClientNode;
import com.proxy.common.entity.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.server.handler.*;
import com.proxy.server.service.LogBackConfigLoader;
import com.proxy.server.service.ServerBeanManager;
import com.proxy.server.service.SharableHandlerManager;
import com.proxy.server.util.ProxyUtil;
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

public class ProxyServer {


    /**
     * 最大帧长度
     */
    private static final int MAX_FRAME_LENGTH = 2 * 1024 * 1024;
    /**
     * 长度域偏移
     */
    private static final int LENGTH_FIELD_OFFSET = 0;
    /**
     * 长度域字节数
     */
    private static final int LENGTH_FIELD_LENGTH = 4;
    /**
     * 跳过的字节数
     */
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    /**
     * 数据长度修正
     */
    private static final int LENGTH_ADJUSTMENT = 0;
    /**
     * 并发量
     */
    public static  int concurrent = 1000;
    private static Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    /**
     * 绑定端口,默认6666
     */
    public int port;
    /**
     * http 代理通道
     */
    public Integer httpPort;





    public ProxyServer() {
        this.port = 6666;
    }

    public ProxyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args)throws  Exception{


        //加载日志
        LogBackConfigLoader.load();

        try {
            new ProxyServer().start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private  ChannelFuture bind()  {

        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup  workerGroup=new NioEventLoopGroup();
        ServerBootstrap bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                        socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(10*6, 15*6, 20*6));
                        socketChannel.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH,LENGTH_FIELD_OFFSET,LENGTH_FIELD_LENGTH));
                        socketChannel.pipeline().addLast(new ProxyMessageEncoder());
                        socketChannel.pipeline().addLast(new LoginAuthRespHandler());
                        socketChannel.pipeline().addLast(new HeartBeatRespHandler());
                        socketChannel.pipeline().addLast(new ServerChannelHandler());
                    }
                });
        ServerBeanManager.setBootstrap(bootstrap);
        ChannelFuture future=null;
        try{
            future=bootstrap.bind(port);

            future.channel().closeFuture().addListeners(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    logger.info("等待代理服务退出...");
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            logger.info("服务器监听端口 {}",port);
        }catch (Exception e){
            e.printStackTrace();
        }
        return future;
    }

    public  void start() throws  Exception{

        //读取代理服务配置文件
        ServerBeanManager.getConfigService().readServerConfig();

        if(ServerBeanManager.getConfigService().getConfigure("port")!=null){
            this.port= (int) ServerBeanManager.getConfigService().getConfigure("port");
        }
        if(ServerBeanManager.getConfigService().getConfigure("concurrent")!=null){
            ProxyServer.concurrent = (int)ServerBeanManager.getConfigService().getConfigure("concurrent");
        }

        if(ServerBeanManager.getConfigService().getConfigure("httpPort")!=null) {
            //启动http 转发服务
            this.httpPort = (int) ServerBeanManager.getConfigService().getConfigure("httpPort");
        }


        try {
            //配置代理信息
            configurProxy();
        }catch (Exception e){
            return;
        }

        ChannelFuture mainFuture=null;

        ChannelFuture httpFuture=null;
        try {
            //启动主程序
            mainFuture=startMainServer();

            //启动http服务(如果有配置)
            httpFuture=startHttpServer();

            // 启动转发服务，暂定这样
            ServerBeanManager.getTransferService().start();

            mainFuture.channel().closeFuture().sync();
        }catch (Exception e){
            if (mainFuture != null){
                mainFuture.channel().close();
            }
            if (httpFuture !=null){
                httpFuture.channel().close();
            }
        }
    }

    public ChannelFuture startMainServer(){



        //根据配置文件启动服务
        ChannelFuture future=bind();
        return future;
    }

    /**
     * 配置代理信息
     * @throws Exception
     */
    public void configurProxy() throws Exception{
        Map<String,List<Map<String,Object>>> nodes = (Map<String,List<Map<String,Object>>>) ServerBeanManager.getConfigService().getConfigure("client");
        for (Map.Entry<String,List<Map<String,Object>>> m:nodes.entrySet()){
            ClientNode clientNode=new ClientNode();
            clientNode.setClientKey(m.getKey());
            clientNode.setStatus(CommonConstant.ClientStatus.ACTIVE);
//            clientNode.setName(m.getByServerPort("name"));

            List<Map<String,Object>> reals = m.getValue();

            for (Map<String,Object> real : reals){
                ProxyRealServer proxy=new ProxyRealServer();
                proxy.setClientKey(m.getKey());
                proxy.setRealHost((String) real.get("realhost"));
                proxy.setRealHostPort((Integer) real.get("realhostport"));
                proxy.setDescription((String) real.get("description"));
                String proxyType= (String) real.get("proxyType");
                if (proxyType.equalsIgnoreCase("http")){
                    proxy.setDomain((String) real.get("domain"));
                    Integer serverport = (Integer) real.get("serverport");
                    String domain = proxy.getDomain();

                    if (domain != null && this.httpPort==null){
                        logger.error("配置文件出错,http域名代理需要配置httpPort端口");
                        throw  new RuntimeException();
                    }

                    if(StringUtils.isBlank(domain) && serverport == null){
                        logger.error("配置文件出错,http代理至少要有serverport或者domain一种");
                        throw  new RuntimeException();
                    }
                    String forward = (String) real.get("forward");

                    if(forward != null){

                        if(CommonConstant.HeaderAttr.Forwarded_Default.equals(forward)){
                            //指定为服务器ip
                            proxy.setForward(CommonConstant.HeaderAttr.Forwarded_Default);
                        }else if(CommonConstant.HeaderAttr.Forwarded_Random.equals(forward)){
                            //随机ip
                            proxy.setForward(CommonConstant.HeaderAttr.Forwarded_Random);
                        }else if(ProxyUtil.isIpAddr(forward)){
                            //用户指定ip
                            proxy.setForward(forward);
                        }else if(!CommonConstant.HeaderAttr.Forwarded_None.equals(forward)){
                            logger.error("配置文件出错,http代理forward 配置错误");
                            throw  new RuntimeException();
                        }
                    }

                    proxy.setServerPort(serverport);
                    proxy.setProxyType(CommonConstant.ProxyType.HTTP);
                    clientNode.addRealServer(proxy.getDomain()==null?serverport:proxy.getDomain(),proxy);
                }else if(proxyType.equalsIgnoreCase("tcp")){
                    proxy.setProxyType(CommonConstant.ProxyType.TCP);
                    proxy.setServerPort((Integer) real.get("serverport"));
                    clientNode.addRealServer(proxy.getServerPort(),proxy);
                }else {
                    continue;
                }
            }
            ServerBeanManager.getClientService().add(clientNode.getClientKey(), clientNode);
        }
    }

    public ChannelFuture startHttpServer() throws  RuntimeException{

        if(this.httpPort!=null){
            //启动http 转发服务

            //绑定客户端服务端口
            NioEventLoopGroup serverWorkerGroup=new NioEventLoopGroup();
            NioEventLoopGroup serverBossGroup=new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(SharableHandlerManager.getTrafficLimitHandler());
                            ch.pipeline().addLast(SharableHandlerManager.getTrafficCollectionHandler());
                            //http请求消息解码器
                            ch.pipeline().addLast("httpDecoder",new HttpRequestDecoder());
                            ch.pipeline().addLast("connectHandler",new HttpNoticeChannelHandler());
                            //解析 HTTP POST 请求
                            ch.pipeline().addLast("httpObject",new HttpObjectAggregator(2*1024*1024));
                            ch.pipeline().addLast("transferHandler",new HttpChannelHandler());
                        }
                    });
            try {
                //绑定服务端口,会更新代理状态
                ChannelFuture future= ServerBeanManager.getProxyChannelService().bind(this.httpPort,bootstrap,CommonConstant.ProxyType.HTTP,this.httpPort);
                future.channel().closeFuture().addListeners(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.info("等待http代理服务退出...");
                        serverWorkerGroup.shutdownGracefully();
                        serverBossGroup.shutdownGracefully();
                    }
                });
                return  future;
            } catch (Exception e) {
                logger.error("http服务端口 {} 绑定失败:"+e.getMessage(),this.httpPort);
                throw  new RuntimeException();
            }
        }
        return null;
    }

}
