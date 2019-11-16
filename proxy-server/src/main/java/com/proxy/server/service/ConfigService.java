package com.proxy.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * 代理服务器配置文件读取
 *
 * @author ztgreat
 */
public class ConfigService {

    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private static Map<String, Object> config = null;

    public Map<String, Object> readServerConfig() {

        Map<String, Object> loaded = null;
        try {

            InputStream in = this.getClass().getClassLoader().getResourceAsStream("proxy.yaml");
            if (in == null) {
                String filePath = "../conf/proxy.yaml";
                in = new BufferedInputStream(new FileInputStream(filePath));
            }
            Yaml yaml = new Yaml();
            loaded = (Map<String, Object>) yaml.load(in);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return config = loaded;
    }

    public synchronized Object getConfigure(String key) {
        if (config == null)
            readServerConfig();
        return config.get(key);
    }


}
