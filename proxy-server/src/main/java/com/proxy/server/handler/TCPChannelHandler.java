package com.proxy.server.handler;


import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.entity.server.ProxyChannel;
import com.proxy.common.entity.server.ProxyRealServer;
import com.proxy.common.protocol.CommonConstant;
import com.proxy.common.protocol.Message;
import com.proxy.server.service.ServerBeanManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * 处理用户请求的handler
 *
 * @author ztgreat
 */
public class TCPChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(TCPChannelHandler.class);

    public TCPChannelHandler() {
        super();
    }

    /**
     * 为用户连接产生ID
     */
    private static Long getSessionID() {
        return ServerBeanManager.getSessionIDGenerate().generateId();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        Channel userChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();

        ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(sa.getPort());

        //如果不存在key,那么不存在该代理客户端，后续理应不再判断，暂时为了避免考虑不全，先判断多次
        if (proxyChannel == null) {
            logger.error("端口{} 没有代理客户端", sa.getPort());
            ctx.channel().close();
            return;
        }
        ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
        if (node == null || node.getChannel() == null || node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
            logger.error("端口{} 没有代理客户端", sa.getPort());
            ctx.channel().close();
            return;
        }
        long sessionID = getSessionID();
        int sPort = sa.getPort();

        sa = (InetSocketAddress) userChannel.remoteAddress();
        String ip = sa.getAddress().getHostAddress();
        int uPort = sa.getPort();

        //将sessionID，用户ip,port，服务器port，封装到消息中

        Message message = new Message();
        message.setIp(ip);
        message.setClientChannel(node.getChannel());

        message.setuPort(uPort);
        message.setsPort(sPort);
        message.setSessionID(sessionID);
        message.setType(CommonConstant.MessageType.TYPE_CONNECT_REALSERVER);
        ProxyRealServer realServer = node.getServerPort2RealServer().get(sPort);
        if (Objects.isNull(realServer)) {
            logger.error("端口{} 没有开启映射", sPort);
            ctx.channel().close();
            return;
        }
        String address = realServer.getAddress();
        message.setRemoteAddress(address);
        message.setData(address.getBytes());
        message.setProxyType(realServer.getProxyType().byteValue());
        message.setCommand((ServerBeanManager.getConfigService().getConfigure("server") + ":" + realServer.getServerPort()).getBytes());


        //将通道保存 调用UserService(会保存代理类型 type)
        ServerBeanManager.getUserSessionService().add(sessionID, ctx.channel(), realServer);

        //调用ServerService 将消息 放入队列
        ServerBeanManager.getTransferService().toClient(message);
        logger.debug("通知客户端({})与真实服务器{}建立连接 ", node.getClientKey(), address);

        ctx.channel().config().setAutoRead(false);
        logger.debug("用户请求访问通道设置为不可读");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //代理类型
        Integer type = ServerBeanManager.getUserSessionService().getType(ctx.channel());
        if (type != null) {
            if (type == CommonConstant.ProxyType.TCP) {
                //tcp 类型
                logger.debug("tcp代理");
                tcpHandler(ctx, (ByteBuf) msg, CommonConstant.ProxyType.TCP);
            } else {
                ReferenceCountUtil.release(msg);
                logger.debug("非tcp代理:丢弃消息");
            }
            return;

        }
        ReferenceCountUtil.release(msg);
        logger.debug("消息格式错误:丢弃消息");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        logger.debug("用户连接失效");
        Long sessionID = ServerBeanManager.getUserSessionService().getSessionID(ctx.channel());

        if (sessionID == null) {
            return;
        }

        //需要通知代理客户端和真实服务器断开连接
        Channel userChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();

        ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(sa.getPort());

        //如果不存在key,那么不存在该代理客户端，后续理应不再判断，暂时为了避免考虑不全，先判断多次
        if (Objects.isNull(proxyChannel)) {
            return;
        }
        ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
        if (node == null || node.getChannel() == null || node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
            return;
        }

        int sPort = sa.getPort();

        sa = (InetSocketAddress) userChannel.remoteAddress();
        String ip = sa.getAddress().getHostAddress();
        int uPort = sa.getPort();

        //将sessionID，用户ip,port，服务器port，封装到消息中

        Message message = new Message();
        message.setIp(ip);
        message.setClientChannel(node.getChannel());

        message.setuPort(uPort);
        message.setsPort(sPort);
        message.setSessionID(sessionID);
        message.setType(CommonConstant.MessageType.TYPE_DISCONNECT);
        ProxyRealServer realServer = node.getServerPort2RealServer().get(sPort);
        if (Objects.isNull(realServer)) {
            return;
        }
        String address = realServer.getAddress();
        message.setRemoteAddress(address);
        message.setData(address.getBytes());


        ServerBeanManager.getUserSessionService().remove(sessionID);
        //调用ServerService 将消息 放入队列
        ServerBeanManager.getTransferService().toClient(message);
        logger.debug("通知客户端({})与真实服务器{}断开连接 ", node.getClientKey(), address);
        closeChannle(ctx);


    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }


    /**
     * 处理tcp 请求
     */
    private void tcpHandler(ChannelHandlerContext ctx, ByteBuf buf, Integer proxyType) throws Exception {


        Channel userChannel = ctx.channel();

        InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();

        ProxyChannel proxyChannel = ServerBeanManager.getProxyChannelService().getServerProxy(sa.getPort());

        if (proxyChannel == null) {

            // 该端口还没有代理客户端
            logger.error("端口{} 没有代理客户端", sa.getPort());
            userChannel.close();
            ReferenceCountUtil.release(buf);
            return;
        }
        ClientNode node = ServerBeanManager.getClientService().get(proxyChannel.getClientKey());
        if (node == null || node.getChannel() == null || node.getStatus() != CommonConstant.ClientStatus.ONLINE) {
            logger.error("端口{} 没有代理客户端", sa.getPort());
            userChannel.close();
            ReferenceCountUtil.release(buf);
            return;
        }

        //封装消息
        Long sessionID = ServerBeanManager.getUserSessionService().getSessionID(userChannel);

        //ProxyRealServer realServer = node.getServerPort2RealServer().get(sa.getPort());

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();

        Message message = new Message();
        message.setClientChannel(node.getChannel());
        message.setData(data);
        message.setsPort(sa.getPort());
        message.setSessionID(sessionID);
        message.setType(CommonConstant.MessageType.TYPE_TRANSFER);
        message.setProxyType(proxyType.byteValue());

        logger.debug("来自{}端口的请求转发至客户端({})", sa.getPort(), node.getClientKey());

        ServerBeanManager.getTransferService().toClient(message);
    }

    /**
     * 关闭用户连接
     */
    private void closeChannle(ChannelHandlerContext ctx) {

        Channel channel = ctx.channel();

        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("发生异常({})", cause.getMessage());
        cause.printStackTrace();
        closeChannle(ctx);
    }
}
