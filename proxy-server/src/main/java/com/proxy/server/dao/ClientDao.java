package com.proxy.server.dao;


import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;
import com.proxy.common.cache.memory.MemoryCacheManager;
import com.proxy.common.entity.server.ClientNode;
import com.proxy.common.protocol.CommonConstant;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

import java.util.Map;

/**
 * 代理服务，代理服务器和每个客户端对应的代理通道通过此类进行操作
 */
public class ClientDao {

    private static CacheManager<String, ClientNode> cacheManager = new MemoryCacheManager<String, ClientNode>();

    // clientKey to ClientNode
    private static Cache<String, ClientNode> keyToNode = cacheManager.getCache("client_cache");


    public void add(String clientKey, ClientNode node) {
        keyToNode.put(clientKey, node);
    }

    public String getClientKey(Channel channel) {
        Attribute<String> attr = channel.attr(CommonConstant.ServerChannelAttributeKey.CLIENT_KEY);
        return attr == null ? null : attr.get();
    }

    public void remove(String clientKey) {
        keyToNode.remove(clientKey);
    }

    public ClientNode get(String clientKey) {
        return keyToNode.get(clientKey);
    }

    public void setNodeStatus(String clientKey, Integer status) {
        if (keyToNode.get(clientKey) != null) {
            keyToNode.get(clientKey).setStatus(status);
        }
    }

    public void setNodeChannle(String clientKey, Channel channel) {
        if (keyToNode.get(clientKey) != null) {
            keyToNode.get(clientKey).setChannel(channel);
        }
    }

    public Map<String, ClientNode> getAll() {
        return keyToNode.getAll();
    }

}


