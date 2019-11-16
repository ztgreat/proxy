package com.proxy.client.dao;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ConfigDao {

    private static Map<String, String> config;

    public Map<String, String> readConfig() throws Exception {
        if (config != null) {
            return config;
        }
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("client.properties");
        if (in == null) {
            String filePath = "../conf/client.properties";
            in = new BufferedInputStream(new FileInputStream(filePath));
        }
        Properties prop = new Properties();
        prop.load(in);

        Set keys = prop.keySet();
        config = new HashMap<>();
        for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
            String key = it.next();
            config.put(key, prop.getProperty(key));
        }
        return config;
    }

}
