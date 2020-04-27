package com.recharge.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.utils.HttpUtil;
import com.utils.Log;
import com.utils.MD5Util;
import com.utils.RandomStrUtil;
import com.utils.RandomUtil;
import com.utils.TimeUtil;
import com.recharge.entity.PayAppInfo;
import com.recharge.entity.PayAppInfoDao;
import com.recharge.entity.PayOrder;
import com.recharge.entity.PayOrderDao;

public class WXHandler extends AbstractHandler {

	public static String NOTIFY_URL = "http://www.i66wan.com/weixinpay/orderReceive";

	public static String KEY = "PivYw69JnBQ5ScAJpoXAFjOwdGhBFl7P";

	public static String MCH_ID = "1490012662";

	public static String REQUEST_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	public static int SUCCESS = 200;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/pay/weixinInitOrder".equals(target)) {
			Log.info("target!=/pay/weixinInitOrder");
			return;
		}
		Log.info("收到订单请求");
		orderHandler(request, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void orderHandler(HttpServletRequest request, HttpServletResponse response) {
		int gameId = Integer.parseInt(request.getParameter("gameId"));
		String channelId = request.getParameter("channelId");
		String body = request.getParameter("body");
		int totalFee = Integer.parseInt(request.getParameter("totalFee"));// 总金额
		int goodsId = Integer.parseInt(request.getParameter("goodsId"));// 总金额, //商品id
		int userId = Integer.parseInt(request.getParameter("userId"));// 总金额, //商品id
		Log.info("微信订单生成，userIs= " + userId);

		String appId = null;
		PayAppInfo payAppInfo = PayAppInfoDao.getInstance().findByGameId(gameId);
		if (payAppInfo == null)
			return;
		appId = payAppInfo.getAppId();

		// 生成随机数字字符串
		String nonce_str = RandomStrUtil.randomStr(20);
		Long timeStamp = System.currentTimeMillis();

		// 生成订单号
		String orderNumber = System.currentTimeMillis() + "" + RandomUtil.rand10000();
		// 调用IP
		String spbill_create_ip = request.getRemoteAddr();

		// 组装参数
		Map<String, Object> data = new HashMap<>();
		data.put("appid", appId);
		data.put("mch_id", MCH_ID);
		data.put("nonce_str", nonce_str);
		data.put("body", body);
		data.put("out_trade_no", orderNumber);
		data.put("total_fee", totalFee + "");
		data.put("spbill_create_ip", spbill_create_ip);
		data.put("notify_url", NOTIFY_URL);
		data.put("trade_type", "APP");

		// 对参数key进行排序
		StringBuilder stringSignTemp = sort(data);

		stringSignTemp.append("key").append("=").append(KEY);

		// 签名
		String sign = MD5Util.getMD5String(stringSignTemp.toString());

		data.put("sign", sign);
		// 请求接口

		String requset_params = MapToXml(data);
		String s = HttpUtil.doPost(REQUEST_URL + requset_params, null,false);

		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			responseMap = StrXmlTOMap(s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!responseMap.get("return_code").equals("SUCCESS")) {
			Log.error("微信返回结果，return_code!=200!");
			return;
		}

		PayOrder payOrder = PayOrderDao.getInstance().findByOutTradeNo(Long.parseLong(orderNumber));

		// 不等null证明订单已经存在了，直接返回
		if (payOrder != null)
			return;

		// 生成新订单信息
		payOrder = new PayOrder();
		payOrder.setGameId(gameId);
		payOrder.setChannelId(channelId);
		payOrder.setGoodsId(goodsId);
		payOrder.setTotalFee(totalFee);
		payOrder.setOutTradeNo(Long.parseLong(orderNumber));
		payOrder.setResultCode(responseMap.get("result_code").toString());
		payOrder.setcTime(new Date());
		payOrder.setUserId(userId);
		// 新增订单信息，写库
		PayOrderDao.getInstance().insertPayOrder(payOrder);
		if (!responseMap.get("result_code").equals("SUCCESS")) {
			return;
		}

		// 重新签名
		timeStamp = System.currentTimeMillis();
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("appid", appId);
		responseData.put("noncestr", nonce_str);
		responseData.put("package", "Sign=WXPay");
		responseData.put("partnerid", MCH_ID);
		responseData.put("prepayid", responseMap.get("prepay_id").toString());
		responseData.put("timestamp", timeStamp);

		// 对参数key进行排序
		StringBuilder stringSign = sort(responseData);

		stringSign.append("key").append("=").append(KEY);

		sign = MD5Util.getMD5String(stringSign.toString());
		responseData.put("sign", sign.toUpperCase());
	}

	/**
	 * Map转换成Xml
	 */
	public static String MapToXml(Map<String, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
				sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
			} else {
				sb.append("<" + k + ">" + v + "</" + k + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * xml转化成map
	 *
	 * @param strXml
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> StrXmlTOMap(String strXml) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		Document docment = DocumentHelper.parseText(strXml);
		Element root = docment.getRootElement();
		List<Element> list = root.elements();
		for (Element ele : list) {

			map.put(ele.getName(), ele.getText());
		}

		return map;
	}

	/**
	 * xml 数据流转化为map
	 */
	public static Map<String, String> xmlTOMap(HttpServletRequest request) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		InputStream ins = null;
		try {
			ins = request.getInputStream();
		} catch (IOException e) {

			e.printStackTrace();
		}
		Document docment = reader.read(ins);// 读取流对象
		Element root = docment.getRootElement();
		List<Element> list = root.elements();
		for (Element ele : list) {

			map.put(ele.getName(), ele.getText());
		}

		return map;

	}

	private StringBuilder sort(Map<String, Object> data) {
		List<String> keyList = new ArrayList<>(data.keySet());
		Collections.sort(keyList);
		StringBuilder stringSignTemp = new StringBuilder();
		for (String key : keyList) {
			stringSignTemp.append(key).append("=").append(data.get(key)).append("&");
		}
		return stringSignTemp;
	}

	public String getStrTime(Date date) {
		return TimeUtil.getNumDateFormat(date);
	}

}
