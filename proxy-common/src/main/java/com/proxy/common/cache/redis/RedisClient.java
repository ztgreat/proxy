package com.proxy.common.cache.redis;

import java.util.Set;

/**
 * redis 操作接口
 * @author  ztgreat
 */
public interface RedisClient {

	byte[] get(byte[] key, Integer dbIndex);

	String set(byte[] key, byte[] value, Integer dbIndex);

	String set(byte[] key, byte[] value, int second, Integer dbIndex);
    //将 key 中储存的数字值增一
	long incr(byte[] key, Integer dbIndex);

	//将哈希表 hkey 中的域 key 的值设为 value 
	Long hset(byte[] hkey, byte[] key, byte[] value, Integer dbIndex);

	byte[] hget(byte[] hkey, byte[] key, Integer dbIndex);

	
	Long del(byte[] key, Integer dbIndex);

	Long hdel(byte[] hkey, byte[] key, Integer dbIndex);

	// 设置过期时间
	Long expire(byte[] key, int second, Integer dbIndex);

	Set<byte[]>keys(byte[] pattern, Integer dbIndex);
	
	RedisClient getRedisClient();

}
