package com.recharge.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.utils.TimeUtil;
import com.recharge.base.db.HikariDBPool;

public class PayOrderDao {

	private static final PayOrderDao instance = new PayOrderDao();

	public static final PayOrderDao getInstance() {
		return instance;
	}

	/**
	 * 通过我方订单号找订单信息
	 * 
	 * @param no
	 * @return
	 */
	public PayOrder findByOutTradeNo(double no) {
		PayOrder po = new PayOrder();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from weixin_pay_order where out_trade_no=" + no;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("user_id");
				Date ctime = rs.getDate("ctime");
				int gameId = rs.getInt("game_id");
				String channelId = rs.getString("channel_id");
				int transactionId = rs.getInt("transaction_id");
				double outTradeNo = rs.getDouble("out_trade_no");
				Date timeEnd = rs.getDate("time_end");
				int totalFee = rs.getInt("total_fee");
				String tradeType = rs.getString("trade_type");
				int goodsId = rs.getInt("goods_id");
				String resultCode = rs.getString("result_code");
				int state = rs.getInt("state");
				po.setId(id);
				po.setUserId(userId);
				po.setcTime(ctime);
				po.setGameId(gameId);
				po.setChannelId(channelId);
				po.setTransactionId(transactionId);
				po.setOutTradeNo(outTradeNo);
				po.setTimeEnd(timeEnd);
				po.setTotalFee(totalFee);
				po.setTradeType(tradeType);
				po.setGoodsId(goodsId);
				po.setResultCode(resultCode);
				po.setState(state);
				ps.close();
				rs.close();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return po;
	}

	/**
	 * 新增订单，写库
	 * 
	 * @param payOrder
	 * @return
	 */
	public boolean insertPayOrder(PayOrder payOrder) {
		StringBuffer sql = new StringBuffer();
		sql.append(
				"INSERT INTO `weixin_pay_order` (user_id,ctime,game_id,channel_id,transaction_id,out_trade_no,time_end,total_fee,trade_type,goods_id,result_code) VALUES ");
		sql.append("(");
		sql.append(payOrder.getUserId()).append(",'");
		sql.append(getStrTime(payOrder.getcTime())).append("',");
		sql.append(payOrder.getGameId()).append(",'");
		sql.append(payOrder.getTimeEnd()).append("',");
		sql.append(payOrder.getTotalFee()).append(",'");
		sql.append(payOrder.getTradeType()).append("',");
		sql.append(payOrder.getGoodsId()).append(",'");
		sql.append(payOrder.getResultCode()).append("')");
		Connection conn = HikariDBPool.getDataConn();
		boolean result = false;
		try {
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			result = ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean updatePayOrderState(double ourNo) {
		String sql = "update weixin_pay_order set state = 1 where out_trade_no = '" + ourNo + "'";
		Connection conn = HikariDBPool.getDataConn();
		boolean result = false;
		try {
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			result = ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getStrTime(Date date) {
		return TimeUtil.getNumDateFormat(date);
	}

}
