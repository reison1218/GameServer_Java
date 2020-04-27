package com.base.mgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.base.key.PlayerKey;
import com.base.redis.RedisKey;
import com.base.redis.RedisPool;
import com.game.player.GamePlayer;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import redis.clients.jedis.Tuple;

/**
 * 排行榜mgr
 * 
 * @author reison
 *
 */
public class RankMgr {

	public static final short MAX = 100;
	public static ReentrantLock weekLock = new ReentrantLock();
	public static ReentrantLock dayLock = new ReentrantLock();

	/**
	 * 重置排行榜（删除老数据）
	 */
	public static void resetRank() {
		// 先重置日榜
		RedisPool.del(RedisKey.RANK_DAY_KEY);
		// 再重置周榜
		if (TimeUtil.getDayOfWeekIndex() == 1) {
			RedisPool.del(RedisKey.RANK_WEEK_KEY);
		}
	}

	/**
	 * 获取排行榜
	 * 
	 * @return
	 */
	public static List<RankInfo> getAllRank(String rankKey) {
		// 获取服务器排行榜数据
		Set<Tuple> set = RedisPool.zrevrangeWithScores(rankKey, 0, -1);
		if (set == null)
			return null;
		List<RankInfo> list = new ArrayList<RankInfo>();
		RankInfo ri = null;
		int index = set.size() - 1;
		String nickName = "nickName";
		String avatar = "icon";
		String value = null;
		// 封装
		for (Tuple tp : set) {
			ri = new RankInfo(Integer.parseInt(tp.getElement()));
			ri.setRank(index);
			ri.setScore((int) tp.getScore());
			value = RedisPool.hget(RedisKey.RANK_INFO_KEY, tp.getElement());
			if (!StringUtils.isEmpty(value) && value.contains(";") && value.split(";").length > 0) {
				String[] strs = value.split(";");
				nickName = strs[0];
				if (strs.length > 1) {
					avatar = strs[1];
				}
			}
			ri.setNickName(nickName);
			ri.setAvatar(avatar);
			index--;
			list.add(ri);
		}
		return list;
	}

	/**
	 * 获得玩家排行信息
	 * 
	 * @param playerId
	 * @return
	 */
	public static RankInfo getPlayerRank(int playerId, String rankKey) {

		String playerIdKey = Integer.toString(playerId);

		Long rank = RedisPool.zrevrank(rankKey, playerIdKey);
		if (rank == null)
			return null;
		// 拿到玩家分数
		Double score = RedisPool.zscore(rankKey, playerIdKey);
		// 封装成rank对象
		RankInfo rankInfo = new RankInfo(playerId);
		rankInfo.setRank(rank.intValue());
		rankInfo.setScore(score.intValue());
		// 拿到玩家昵称和头像
		String value = RedisPool.hget(RedisKey.RANK_INFO_KEY, playerIdKey);
		String nickName = "nickName";
		String avatar = "icon";
		// 封装
		if (!StringUtils.isEmpty(value) && value.contains(";") && value.split(";").length > 0) {
			String[] strs = value.split(";");
			nickName = strs[0];
			if (strs.length > 1) {
				avatar = strs[1];
			}
		}
		rankInfo.setNickName(nickName);
		rankInfo.setAvatar(avatar);
		return rankInfo;
	}

	/**
	 * 更新玩家排行榜
	 * 
	 * @param playerId
	 * @param score
	 * @return
	 */
	public static void updatePlayerRank(GamePlayer player, String rankKey, double score) {
		ReentrantLock lock = null;
		try {
			String playerIdKey = Integer.toString(player.getUserId());
			if (StringUtils.isEmpty(rankKey))
				return;
			if (rankKey.equals(RedisKey.RANK_DAY_KEY)) {
				lock = dayLock;
			} else if (rankKey.equals(RedisKey.RANK_WEEK_KEY)) {
				lock = weekLock;
			}

			lock.tryLock(5000, TimeUnit.MILLISECONDS);
			boolean exist = RedisPool.hexists(RedisKey.RANK_INFO_KEY, playerIdKey);
			if (!exist) {
				RedisPool.hset(RedisKey.RANK_INFO_KEY, playerIdKey,
						player.getNickNameWithSite() + ";" + player.getStringData(PlayerKey.AVATAR));
			}
			// 更新排行榜
			if (rankKey.equals(RedisKey.RANK_DAY_KEY)) {
				RedisPool.zincrby(rankKey, score, playerIdKey);
			} else if (rankKey.equals(RedisKey.RANK_WEEK_KEY)) {
				Double result = RedisPool.zscore(rankKey, playerIdKey);
				if (result.isNaN()) {
					RedisPool.zincrby(rankKey, score, playerIdKey);
				} else if (result.intValue() < score) {
					RedisPool.zreplace(rankKey, score, playerIdKey);
				}
			}

			// 校验是否超榜
			Long size = RedisPool.zcard(rankKey);
			if (size.intValue() > MAX) {
				Set<String> set = RedisPool.zrange(rankKey, 99, size.intValue() - 1);
				Iterator<String> itr = set.iterator();
				while (itr.hasNext()) {
					String playerId = itr.next();
					RedisPool.zrem(rankKey, playerId);
				}
			}
		} catch (InterruptedException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
