package com.proxy.server.service;


import com.proxy.common.protocol.Message;
import com.proxy.server.task.TransferMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于 将用户信息转发给客户端
 */
public class TransferService {


    private ExecutorService executorService;

    /**
     * 将消息转发给客户端
     *
     * @param message 消息
     */
    public void toClient(Message message) {
        executorService.submit(new TransferMessage(message));
    }

    /**
     * 启动转发服务
     */
    public void start() {
        int threads = Runtime.getRuntime().availableProcessors();


        /**
         * 踩坑记录:当线程池的数量为1时，意味着采用单线程来进行转发消息,消息的转发是串行的
         * 当使用多个线程同时进行转发时,则并发的转发消息
         * 在进行ssh 文件上传测试的时候遇到了问题,文件上传很慢,或者不行,存在问题
         * 问题原因应该在数据包的顺序上面,但是tcp协议是自动组包,理应不应该出现这种问题,
         * 另一种原因也可能和上传工具采用的上传协议有关
         * 使用pscp 报:packet corrupt 错误,也就是说数据包到达顺序的不一致会导致该问题,难道文件传输必须串行执行？
         * 猜想:应该采用了特定的文件传输协议,并不是简单的二进制流，当接收到的数据包无法和前面的数据包进行组合的时候,
         * 可能就是这种错,需要研究一下文件传输的具体格式
         *
         * 这个就不太好处理了,单线程会效率低,多线程某些协议下可能会出现问题。
         *
         * 解决办法还是有,就是特别麻烦,同一个通道的数据和特定的一个线程进行绑定,
         * 这样就可以保证同一个通道的数据是串行转发的，不同通道的数据是并行转发的
         *
         * TODO 这个问题需要研究
         */

        executorService = Executors.newFixedThreadPool(1);
    }

}
