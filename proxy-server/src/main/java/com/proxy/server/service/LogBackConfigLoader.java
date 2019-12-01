package com.proxy.server.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 日志文件路径配置
 */
public class LogBackConfigLoader {

    /**
     * 加载外部的logback配置文件
     *
     * @throws IOException
     * @throws JoranException
     */
    public static void load() throws IOException, JoranException {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        InputStream in = LogBackConfigLoader.class.getClassLoader().getResourceAsStream("logback.xml");
        if (in == null) {
            String filePath = "../conf/logback.xml";
            in = new BufferedInputStream(new FileInputStream(filePath));
        }
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(in);
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

    }
}