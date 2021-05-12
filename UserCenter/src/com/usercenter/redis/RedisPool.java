package com.usercenter.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.utils.Log;
import com.usercenter.base.config.Config;
import com.usercenter.base.config.ConfigKey;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

public class RedisPool {
	/** 非切片额客户端连接 */
	public static Jedis jedis;
	/** 非切片连接池 */
	public static JedisPool jedisPool;
	/** 切片额客户端连接 */
	public static ShardedJedis shardedJedis;
	/** 切片连接池 */
	public static ShardedJedisPool shardedJedisPool;

	public static boolean init() {
		try {
			initialPool();
//			initialShardedPool();
//			shardedJedis = shardedJedisPool.getResource();
			jedis = jedisPool.getResource();
			// jedis.auth("123456");
			Log.info("redis连接池初始化成功~");
		} catch (Exception e) {
			Log.error(e.getMessage());
			return false;
		}

		return true;
	}

	public static void zincrby(String key, double score, String playerIdKey) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zincrby(key, score, playerIdKey);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
	}

	public static void zreplace(String key, double score, String playerIdKey) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zrem(key, playerIdKey);
			jedis.zincrby(key, score, playerIdKey);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
	}

	public static boolean hexists(String key, String field) {
		boolean size = false;
		Jedis jedis = jedisPool.getResource();
		try {
			size = jedis.hexists(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return size;
	}

	public static long zrem(String key, String... members) {
		Jedis jedis = jedisPool.getResource();
		long result = 0;
		try {
			result = jedis.zrem(key, members);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return result;
	}

	public static Long zcard(String key) {
		Long size = null;
		Jedis jedis = jedisPool.getResource();
		try {
			size = jedis.zcard(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return size;
	}

	public static Set<String> zrange(String key, long start, long end) {
		Set<String> set = null;
		Jedis jedis = jedisPool.getResource();
		try {
			set = jedis.zrange(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			recyleClient(jedis);
		}
		return set;
	}

	public static void del(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
	}

	public static Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
		Jedis jedis = jedisPool.getResource();
		Set<Tuple> set = null;
		try {
			set = jedis.zrevrangeWithScores(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return set;
	}

	public static Long zrevrank(String key, String filed) {
		Jedis jedis = jedisPool.getResource();
		Long rank = null;

		try {
			rank = jedis.zrevrank(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return rank;
	}

	public static Double zscore(String key, String filed) {
		Double score = null;
		Jedis jedis = jedisPool.getResource();

		try {
			score = jedis.zscore(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}
		return score;
	}

	public static String hget(String key, String filed) {
		Jedis jedis = jedisPool.getResource();
		String tId = null;
		try {
			tId = jedis.hget(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recyleClient(jedis);
		}

		return tId;
	}
	
	public static String hgetWithIndex(int index,String key, String filed) {
		Jedis jedis = jedisPool.getResource();
		String tId = null;
		try {
			jedis.select(index);
			tId = jedis.hget(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jedis.select(0);
			recyleClient(jedis);
		}

		return tId;
	}
	
	/**
	 * 判断是否存在key
	 * @param key
	 * @return
	 */
	public static boolean exists(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			boolean exist = jedis.exists(key);
			return exist;
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			recyleClient(jedis);
		}
		return false;
	}
	
	/**
	 * 给key加上过期时间
	 * @param key
	 * @return
	 */
	public static long expire(String key,int lazyDropTime) {
		Jedis jedis = jedisPool.getResource();
		try {
			boolean exist = jedis.exists(key);
			if(!exist) {
				return 0;
			}
			long result = jedis.expire(key, lazyDropTime);
			return result;
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			recyleClient(jedis);
		}
		return 0;
	}

	public static void hset(String key, String filed, String value,int lazyDropTime) {
		Jedis jedis = jedisPool.getResource();
		try {
			boolean exist = jedis.exists(key);
			jedis.hset(key, filed, value);
			if(lazyDropTime <=0)
				return;
			if(exist) {
				return;
			}
			long result = jedis.expire(key, lazyDropTime);
			System.out.println("result:"+result);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			recyleClient(jedis);
		}
	}
	
	public static void hsetWithIndex(int index,String key, String filed, String value,int lazyDropTime) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.select(index);
			boolean exist = jedis.exists(key);
			jedis.hset(key, filed, value);
			if(lazyDropTime <=0) {
				return;
			}
			if(exist) {
				return;
			}
			long result = jedis.expire(key, lazyDropTime);
			System.out.println("result:"+result);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			jedis.select(0);
			recyleClient(jedis);
		}
	}

	public static void hdel(int index,String key, String filed) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.select(index);
			jedis.hdel(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jedis.select(0);
			recyleClient(jedis);
		}

	}

	/**
	 * <pre>
	 * 回收连接
	 * </pre>
	 *
	 * @param client
	 */
	@SuppressWarnings("deprecation")
	public static void recyleClient(Jedis client) {
		if (client == null) {
			return;
		}
		try {
			// client..quit();
			client.close();
			return;
//			if (!isValid(client)) {
//				jedisPool.returnBrokenResource(client);
//			} else {
//				jedisPool.returnResource(client);
//			}
		} catch (Exception e) {
			Log.error("回收Redis连接异常", e);
		}
	}

	/**
	 * <pre>
	 * 连接是否有效
	 * </pre>
	 *
	 * @param client
	 * @return
	 */
	private static boolean isValid(Jedis client) {
		if (client == null || !client.isConnected() || client.getClient().isBroken()) {
			return false;
		}
		return true;
	}

	/**
	 * 初始化非切片池
	 */
	public static void initialPool() {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		// 最大空闲连接数
		config.setMaxIdle(100);
		// 最大连接数
		config.setMaxTotal(512);
		// 最小空闲连接数, 默认0
		config.setMinIdle(8);
		// 是否启用后进先出, 默认true
		config.setLifo(true);
		// 最大等待阻塞时间-5s
		config.setMaxWaitMillis(5000);
		// 获取连接的时候检查有效性
		config.setTestOnBorrow(true);
		// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
		config.setBlockWhenExhausted(true);
		// 是否启用pool的jmx管理功能, 默认true
		config.setJmxEnabled(true);
		// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断
		// (默认逐出策略)
		config.setSoftMinEvictableIdleTimeMillis(1800000);
		// jedisPool = new JedisPool(config, "127.0.0.1", 6379);
		String pass = Config.getConfig(ConfigKey.REDIS_PASS);
		jedisPool = new JedisPool(config, "127.0.0.1", 6379, 5000, pass);
	}

	/**
	 * 初始化切片池
	 */
	public void initialShardedPool() {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(5);
		config.setTestOnBorrow(false);
		// slave链接
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		JedisShardInfo jsi = new JedisShardInfo("127.0.0.1", 6379, "master");
		jsi.setPassword("123456");
		shards.add(jsi);

		// 构造池
		shardedJedisPool = new ShardedJedisPool(config, shards);

	}

	public void show() {
//		KeyOperate();
//		StringOperate();
//		ListOperate();
//		SetOperate();
//		SortedSetOperate();
		HashOperate();
//		test();
//		jedisPool.returnResource(jedis);
//		shardedJedisPool.returnResource(shardedJedis);
	}

	public void KeyOperate() {
		System.out.println("======================key==========================");
		// 清空数据
		System.out.println("清空库中所有数据：" + jedis.flushDB());
		// 判断key否存在
		System.out.println("判断key999键是否存在：" + shardedJedis.exists("key999"));
		System.out.println("新增key001,value001键值对：" + shardedJedis.set("key001", "value001"));
		System.out.println("判断key001是否存在：" + shardedJedis.exists("key001"));
		// 输出系统中所有的key
		System.out.println("新增key002,value002键值对：" + shardedJedis.set("key002", "value002"));
		System.out.println("系统中所有键如下：");
		Set<String> keys = jedis.keys("*");
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			System.out.println(key);
		}
		// 删除某个key,若key不存在，则忽略该命令。
		System.out.println("系统中删除key002: " + jedis.del("key002"));
		System.out.println("判断key002是否存在：" + shardedJedis.exists("key002"));
		// 设置 key001的过期时间
		System.out.println("设置 key001的过期时间为5秒:" + jedis.expire("key001", 5));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		// 查看某个key的剩余生存时间,单位【秒】.永久生存或者不存在的都返回-1
		System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));
		// 移除某个key的生存时间
		System.out.println("移除key001的生存时间：" + jedis.persist("key001"));
		System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));
		// 查看key所储存的值的类型
		System.out.println("查看key所储存的值的类型：" + jedis.type("key001"));
		/*
		 * 一些其他方法：1、修改键名：jedis.rename("key6", "key0");
		 * 2、将当前db的key移动到给定的db当中：jedis.move("foo", 1)
		 */
	}

	public void StringOperate() {
		System.out.println("======================String_1==========================");
		// 清空数据
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		jedis.set("key001", "value001");
		jedis.set("key002", "value002");
		jedis.set("key003", "value003");
		System.out.println("已新增的3个键值对如下：");
		System.out.println(jedis.get("key001"));
		System.out.println(jedis.get("key002"));
		System.out.println(jedis.get("key003"));

		System.out.println("=============删=============");
		System.out.println("删除key003键值对：" + jedis.del("key003"));
		System.out.println("获取key003键对应的值：" + jedis.get("key003"));

		System.out.println("=============改=============");
		// 1、直接覆盖原来的数据
		System.out.println("直接覆盖key001原来的数据：" + jedis.set("key001", "value001-update"));
		System.out.println("获取key001对应的新值：" + jedis.get("key001"));
		// 2、直接覆盖原来的数据
		System.out.println("在key002原来值后面追加：" + jedis.append("key002", "+appendString"));
		System.out.println("获取key002对应的新值" + jedis.get("key002"));

		System.out.println("=============增，删，查（多个）=============");
		/**
		 * mset,mget同时新增，修改，查询多个键值对 等价于： jedis.set("name","ssss");
		 * jedis.set("jarorwar","xxxx");
		 */
		System.out.println("一次性新增key201,key202,key203,key204及其对应值："
				+ jedis.mset("key201", "value201", "key202", "value202", "key203", "value203", "key204", "value204"));
		System.out.println(
				"一次性获取key201,key202,key203,key204各自对应的值：" + jedis.mget("key201", "key202", "key203", "key204"));
		System.out.println("一次性删除key201,key202：" + jedis.del(new String[] { "key201", "key202" }));
		System.out.println(
				"一次性获取key201,key202,key203,key204各自对应的值：" + jedis.mget("key201", "key202", "key203", "key204"));
		System.out.println();

		// jedis具备的功能shardedJedis中也可直接使用，下面测试一些前面没用过的方法
		System.out.println("======================String_2==========================");
		// 清空数据
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============新增键值对时防止覆盖原先值=============");
		System.out.println("原先key301不存在时，新增key301：" + shardedJedis.setnx("key301", "value301"));
		System.out.println("原先key302不存在时，新增key302：" + shardedJedis.setnx("key302", "value302"));
		System.out.println("当key302存在时，尝试新增key302：" + shardedJedis.setnx("key302", "value302_new"));
		System.out.println("获取key301对应的值：" + shardedJedis.get("key301"));
		System.out.println("获取key302对应的值：" + shardedJedis.get("key302"));

		System.out.println("=============超过有效期键值对被删除=============");
		// 设置key的有效期，并存储数据
		System.out.println("新增key303，并指定过期时间为2秒" + shardedJedis.setex("key303", 2, "key303-2second"));
		System.out.println("获取key303对应的值：" + shardedJedis.get("key303"));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		System.out.println("3秒之后，获取key303对应的值：" + shardedJedis.get("key303"));

		System.out.println("=============获取原值，更新为新值一步完成=============");
		System.out.println("key302原值：" + shardedJedis.getSet("key302", "value302-after-getset"));
		System.out.println("key302新值：" + shardedJedis.get("key302"));

		System.out.println("=============获取子串=============");
		System.out.println("获取key302对应值中的子串：" + shardedJedis.getrange("key302", 5, 7));
	}

	public void ListOperate() {
		System.out.println("======================list==========================");
		// 清空数据
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "ArrayList");
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "LinkedList");
		shardedJedis.lpush("stringlists", "MapList");
		shardedJedis.lpush("stringlists", "SerialList");
		shardedJedis.lpush("stringlists", "HashList");
		shardedJedis.lpush("numberlists", "3");
		shardedJedis.lpush("numberlists", "1");
		shardedJedis.lpush("numberlists", "5");
		shardedJedis.lpush("numberlists", "2");
		System.out.println("所有元素-stringlists：" + shardedJedis.lrange("stringlists", 0, -1));
		System.out.println("所有元素-numberlists：" + shardedJedis.lrange("numberlists", 0, -1));

		System.out.println("=============删=============");
		// 删除列表指定的值 ，第二个参数为删除的个数（有重复时），后add进去的值先被删，类似于出栈
		System.out.println("成功删除指定元素个数-stringlists：" + shardedJedis.lrem("stringlists", 2, "vector"));
		System.out.println("删除指定元素之后-stringlists：" + shardedJedis.lrange("stringlists", 0, -1));
		// 删除区间以外的数据
		System.out.println("删除下标0-3区间之外的元素：" + shardedJedis.ltrim("stringlists", 0, 3));
		System.out.println("删除指定区间之外元素后-stringlists：" + shardedJedis.lrange("stringlists", 0, -1));
		// 列表元素出栈
		System.out.println("出栈元素：" + shardedJedis.lpop("stringlists"));
		System.out.println("元素出栈后-stringlists：" + shardedJedis.lrange("stringlists", 0, -1));

		System.out.println("=============改=============");
		// 修改列表中指定下标的值
		shardedJedis.lset("stringlists", 0, "hello list!");
		System.out.println("下标为0的值修改后-stringlists：" + shardedJedis.lrange("stringlists", 0, -1));
		System.out.println("=============查=============");
		// 数组长度
		System.out.println("长度-stringlists：" + shardedJedis.llen("stringlists"));
		System.out.println("长度-numberlists：" + shardedJedis.llen("numberlists"));
		// 排序
		/*
		 * list中存字符串时必须指定参数为alpha，如果不使用SortingParams，而是直接使用sort("list")， 会出现
		 * "ERR One or more scores can't be converted into double"
		 */
		SortingParams sortingParameters = new SortingParams();
		sortingParameters.alpha();
		sortingParameters.limit(0, 3);
		System.out.println("返回排序后的结果-stringlists：" + shardedJedis.sort("stringlists", sortingParameters));
		System.out.println("返回排序后的结果-numberlists：" + shardedJedis.sort("numberlists"));
		// 子串： start为元素下标，end也为元素下标；-1代表倒数一个元素，-2代表倒数第二个元素
		System.out.println("子串-第二个开始到结束：" + shardedJedis.lrange("stringlists", 1, -1));
		// 获取列表指定下标的值
		System.out.println("获取下标为2的元素：" + shardedJedis.lindex("stringlists", 2) + "\n");
	}

	public void SetOperate() {
		System.out.println("======================set==========================");
		// 清空数据
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		System.out.println("向sets集合中加入元素element001：" + jedis.sadd("sets", "element001"));
		System.out.println("向sets集合中加入元素element002：" + jedis.sadd("sets", "element002"));
		System.out.println("向sets集合中加入元素element003：" + jedis.sadd("sets", "element003"));
		System.out.println("向sets集合中加入元素element004：" + jedis.sadd("sets", "element004"));
		System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("集合sets中删除元素element003：" + jedis.srem("sets", "element003"));
		System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));
		/*
		 * System.out.println("sets集合中任意位置的元素出栈："+jedis.spop("sets"));//注：
		 * 出栈元素位置居然不定？--无实际意义
		 * System.out.println("查看sets集合中的所有元素:"+jedis.smembers("sets"));
		 */
		System.out.println();

		System.out.println("=============改=============");
		System.out.println();

		System.out.println("=============查=============");
		System.out.println("判断element001是否在集合sets中：" + jedis.sismember("sets", "element001"));
		System.out.println("循环查询获取sets中的每个元素：");
		Set<String> set = jedis.smembers("sets");
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			System.out.println(obj);
		}
		System.out.println();

		System.out.println("=============集合运算=============");
		System.out.println("sets1中添加元素element001：" + jedis.sadd("sets1", "element001"));
		System.out.println("sets1中添加元素element002：" + jedis.sadd("sets1", "element002"));
		System.out.println("sets1中添加元素element003：" + jedis.sadd("sets1", "element003"));
		System.out.println("sets1中添加元素element003：" + jedis.sadd("sets1", "element003"));
		System.out.println("sets1中添加元素element002：" + jedis.sadd("sets2", "element002"));
		System.out.println("sets1中添加元素element003：" + jedis.sadd("sets2", "element003"));
		System.out.println("sets1中添加元素element004：" + jedis.sadd("sets2", "element004"));
		System.out.println("查看sets1集合中的所有元素:" + jedis.smembers("sets1"));
		System.out.println("查看sets2集合中的所有元素:" + jedis.smembers("sets2"));
		System.out.println("sets1和sets2交集：" + jedis.sinter("sets1", "sets2"));
		System.out.println("sets1和sets2并集：" + jedis.sunion("sets1", "sets2"));
		System.out.println("sets1和sets2差集：" + jedis.sdiff("sets1", "sets2"));// 差集：set1中有，set2中没有的元素
	}

	public void SortedSetOperate() {
		System.out.println("======================zset==========================");
		// 清空数据
		System.out.println(jedis.flushDB());

		System.out.println("=============增=============");
		System.out.println("zset中添加元素element001：" + shardedJedis.zadd("zset", 7.0, "element001"));
		System.out.println("zset中添加元素element002：" + shardedJedis.zadd("zset", 8.0, "element002"));
		System.out.println("zset中添加元素element003：" + shardedJedis.zadd("zset", 2.0, "element003"));
		System.out.println("zset中添加元素element004：" + shardedJedis.zadd("zset", 3.0, "element004"));
		System.out.println("zset集合中的所有元素：" + shardedJedis.zrange("zset", 0, -1));// 按照权重值排序
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("zset中删除元素element002：" + shardedJedis.zrem("zset", "element002"));
		System.out.println("zset集合中的所有元素：" + shardedJedis.zrange("zset", 0, -1));
		System.out.println();

		System.out.println("=============改=============");
		System.out.println();

		System.out.println("=============查=============");
		System.out.println("统计zset集合中的元素中个数：" + shardedJedis.zcard("zset"));
		System.out.println("统计zset集合中权重某个范围内（1.0——5.0），元素的个数：" + shardedJedis.zcount("zset", 1.0, 5.0));
		System.out.println("查看zset集合中element004的权重：" + shardedJedis.zscore("zset", "element004"));
		System.out.println("查看下标1到2范围内的元素值：" + shardedJedis.zrange("zset", 1, 2));
	}

	public void HashOperate() {
		System.out.println("======================hash==========================");
		// 清空数据
		System.out.println(jedis.flushDB());

		System.out.println("=============增=============");
		System.out.println("hashs中添加key001和value001键值对：" + shardedJedis.hset("hashs", "key001", "value001"));
		System.out.println("hashs中添加key002和value002键值对：" + shardedJedis.hset("hashs", "key002", "value002"));
		System.out.println("hashs中添加key003和value003键值对：" + shardedJedis.hset("hashs", "key003", "value003"));
		System.out.println("新增key004和4的整型键值对：" + shardedJedis.hincrBy("hashs", "key004", 4L));
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("hashs中删除key002键值对：" + shardedJedis.hdel("hashs", "key002"));
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============改=============");
		System.out.println("key004整型键值的值增加100：" + shardedJedis.hincrBy("hashs", "key004", 100L));
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============查=============");
		System.out.println("判断key003是否存在：" + shardedJedis.hexists("hashs", "key003"));
		System.out.println("获取key004对应的值：" + shardedJedis.hget("hashs", "key004"));
		System.out.println("批量获取key001和key003对应的值：" + shardedJedis.hmget("hashs", "key001", "key003"));
		System.out.println("获取hashs中所有的key：" + shardedJedis.hkeys("hashs"));
		System.out.println("获取hashs中所有的value：" + shardedJedis.hvals("hashs"));
		System.out.println();
	}

	public static void main(String[] aa) {
//		getJedis().select(0);
//		getJedis().zincrby("rank", 1, "d");
	}

	public void test() {
		System.out.println();
		System.out.println("=============查=============");
		System.out.println("判断key003是否存在：" + shardedJedis.hexists("hashs", "key003"));
		System.out.println("获取key004对应的值：" + shardedJedis.hget("hashs", "key004"));
		System.out.println("批量获取key001和key003对应的值：" + shardedJedis.hmget("hashs", "key001", "key003"));
		System.out.println("获取hashs中所有的key：" + shardedJedis.hkeys("hashs"));
		System.out.println("获取hashs中所有的value：" + shardedJedis.hvals("hashs"));
	}
}
