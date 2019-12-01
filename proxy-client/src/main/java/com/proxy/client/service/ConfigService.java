package com.proxy.client.service;


import com.proxy.client.dao.ConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理客户端配置文件读取
 *
 * @author ztgreat
 */
public class ConfigService {

    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private ConfigDao configDao = new ConfigDao();

    public Map<String, String> readConfig() {
        Map<String, String> config;
        try {
            config = configDao.readConfig();
        } catch (Exception e) {
            logger.error("读取客户端配置文件 失败({})", e.getMessage());
            return new HashMap<>();
        }
        return config;

    }

}
