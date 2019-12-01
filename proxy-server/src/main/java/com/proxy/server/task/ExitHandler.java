package com.proxy.server.task;

import com.proxy.server.service.ServerBeanManager;

/**
 * ctrl +c
 * 当在控制台使用 ctrl+c退出时,清理数据,准备退出
 */
public class ExitHandler extends Thread {
    @Override
    public void run() {
        System.out.println("正在退出...");
        try {
            ServerBeanManager.getProxyServer().shutDown();
            ServerBeanManager.getProxyChannelService().shutDown();
        } catch (Exception ignored) {

        }

    }
}
