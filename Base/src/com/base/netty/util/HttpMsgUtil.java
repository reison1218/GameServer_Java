/*
 * Copyright 2014 The Netty Project The Netty Project licenses this file to you under the Apache License, version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.base.netty.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.utils.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

public final class HttpMsgUtil {

	/**
	 * Returns {@code true} if and only if the connection can remain open and thus 'kept alive'. This methods respects the value of the {@code "Connection"} header first and then the return value of {@link HttpVersion#isKeepAliveDefault()}.
	 */
	public static boolean isKeepAlive(HttpMessage message) {
		CharSequence connection = message.headers().get(HttpHeaderNames.CONNECTION);
		if (connection != null && HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
			return false;
		}

		if (message.protocolVersion().isKeepAliveDefault()) {
			return !HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection);
		} else {
			return HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connection);
		}
	}

	/**
	 * Sets the value of the {@code "Connection"} header depending on the protocol version of the specified message. This getMethod sets or removes the {@code "Connection"} header depending on what the default keep alive mode of the message's protocol version is, as specified by {@link HttpVersion#isKeepAliveDefault()}.
	 * <ul>
	 * <li>If the connection is kept alive by default:
	 * <ul>
	 * <li>set to {@code "close"} if {@code keepAlive} is {@code false}.</li>
	 * <li>remove otherwise.</li>
	 * </ul>
	 * </li>
	 * <li>If the connection is closed by default:
	 * <ul>
	 * <li>set to {@code "keep-alive"} if {@code keepAlive} is {@code true}.</li>
	 * <li>remove otherwise.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	public static void setKeepAlive(HttpMessage message, boolean keepAlive) {
		HttpHeaders h = message.headers();
		if (message.protocolVersion().isKeepAliveDefault()) {
			if (keepAlive) {
				h.remove(HttpHeaderNames.CONNECTION);
			} else {
				h.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
			}
		} else {
			if (keepAlive) {
				h.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			} else {
				h.remove(HttpHeaderNames.CONNECTION);
			}
		}
	}

	/**
	 * Returns the length of the content. Please note that this value is not retrieved from {@link HttpContent#content()} but from the {@code "Content-Length"} header, and thus they are independent from each other.
	 *
	 * @return the content length
	 * @throws NumberFormatException if the message does not have the {@code "Content-Length"} header or its value is not a number
	 */
	public static long getContentLength(HttpMessage message) {
		Integer value = message.headers().getInt(HttpHeaderNames.CONTENT_LENGTH);
		if (value != null) {
			return value;
		}

		// We know the content length if it's a Web Socket message even if
		// Content-Length header is missing.
		Integer webSocketContentLength = getWebSocketContentLength(message);
		if (webSocketContentLength >= 0) {
			return webSocketContentLength;
		}

		// Otherwise we don't.
		throw new NumberFormatException("header not found: " + HttpHeaderNames.CONTENT_LENGTH);
	}

	/**
	 * Returns the length of the content. Please note that this value is not retrieved from {@link HttpContent#content()} but from the {@code "Content-Length"} header, and thus they are independent from each other.
	 *
	 * @return the content length or {@code defaultValue} if this message does not have the {@code "Content-Length"} header or its value is not a number
	 */
	public static long getContentLength(HttpMessage message, long defaultValue) {
		Integer value = message.headers().getInt(HttpHeaderNames.CONTENT_LENGTH);
		if (value != null) {
			return value;
		}

		// We know the content length if it's a Web Socket message even if
		// Content-Length header is missing.
		Integer webSocketContentLength = getWebSocketContentLength(message);
		if (webSocketContentLength >= 0) {
			return webSocketContentLength;
		}

		// Otherwise we don't.
		return defaultValue;
	}

	/**
	 * Returns the content length of the specified web socket message. If the specified message is not a web socket message, {@code -1} is returned.
	 */
	private static int getWebSocketContentLength(HttpMessage message) {
		// WebSockset messages have constant content-lengths.
		HttpHeaders h = message.headers();
		if (message instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) message;
			if (HttpMethod.GET.equals(req.method()) && h.contains(HttpHeaderNames.SEC_WEBSOCKET_KEY1) && h.contains(HttpHeaderNames.SEC_WEBSOCKET_KEY2)) {
				return 8;
			}
		} else if (message instanceof HttpResponse) {
			HttpResponse res = (HttpResponse) message;
			if (res.status().code() == 101 && h.contains(HttpHeaderNames.SEC_WEBSOCKET_ORIGIN) && h.contains(HttpHeaderNames.SEC_WEBSOCKET_LOCATION)) {
				return 16;
			}
		}

		// Not a web socket message
		return -1;
	}

	/**
	 * Sets the {@code "Content-Length"} header.
	 */
	public static void setContentLength(HttpMessage message, int length) {
		message.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, length);
	}

	public static boolean isContentLengthSet(HttpMessage m) {
		return m.headers().contains(HttpHeaderNames.CONTENT_LENGTH);
	}

	/**
	 * Returns {@code true} if and only if the specified message contains the {@code "Expect: 100-continue"} header.
	 */
	public static boolean is100ContinueExpected(HttpMessage message) {
		// Expect: 100-continue is for requests only.
		if (!(message instanceof HttpRequest)) {
			return false;
		}

		// It works only on HTTP/1.1 or later.
		if (message.protocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
			return false;
		}

		// In most cases, there will be one or zero 'Expect' header.
		CharSequence value = message.headers().get(HttpHeaderNames.EXPECT);
		if (value == null) {
			return false;
		}
		if (HttpHeaderValues.CONTINUE.contentEqualsIgnoreCase(value)) {
			return true;
		}

		// Multiple 'Expect' headers. Search through them.
		return message.headers().contains(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE, true);
	}

	/**
	 * Sets or removes the {@code "Expect: 100-continue"} header to / from the specified message. If the specified {@code value} is {@code true}, the {@code "Expect: 100-continue"} header is set and all other previous {@code "Expect"} headers are removed. Otherwise, all {@code "Expect"} headers are removed completely.
	 */
	public static void set100ContinueExpected(HttpMessage message, boolean expected) {
		if (expected) {
			message.headers().set(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
		} else {
			message.headers().remove(HttpHeaderNames.EXPECT);
		}
	}

	/**
	 * Checks to see if the transfer encoding in a specified {@link HttpMessage} is chunked
	 *
	 * @param message The message to check
	 * @return True if transfer encoding is chunked, otherwise false
	 */
	public static boolean isTransferEncodingChunked(HttpMessage message) {
		return message.headers().contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
	}

	public static void setTransferEncodingChunked(HttpMessage m, boolean chunked) {
		if (chunked) {
			m.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
			m.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
		} else {
			List<String> values = m.headers().getAll(HttpHeaderNames.TRANSFER_ENCODING);
			if (values.isEmpty()) {
				return;
			}
			Iterator<String> valuesIt = values.iterator();
			while (valuesIt.hasNext()) {
				CharSequence value = valuesIt.next();
				if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(value)) {
					valuesIt.remove();
				}
			}
			if (values.isEmpty()) {
				m.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
			} else {
				m.headers().set(HttpHeaderNames.TRANSFER_ENCODING, values);
			}
		}
	}

	static void encodeAscii0(CharSequence seq, ByteBuf buf) {
		int length = seq.length();
		for (int i = 0; i < length; i++) {
			buf.writeByte((byte) seq.charAt(i));
		}
	}

	public static Map<String, Object> parse(FullHttpRequest fullReq) throws IOException {
		Map<String, Object> parmMap = new HashMap<>();
		String data = "none";
		try {
			HttpMethod method = fullReq.method();
			if (HttpMethod.GET == method) {
				// 是GET请求
				data = fullReq.uri();
				QueryStringDecoder decoder = new QueryStringDecoder(data);
				decoder.parameters().entrySet().forEach(entry -> {
					// entry.getValue()是一个List, 只取第一个元素
					parmMap.put(entry.getKey(), entry.getValue().get(0));
				});
			} else if (HttpMethod.POST == method) {
				data = fullReq.content().toString(Charset.forName("UTF-8"));
				try {
					// 使用JSON.parseObject，因为需要在外部捕获异常
					parmMap.putAll(JSON.parseObject(data));
				} catch (Exception e) {
					QueryStringDecoder decoder = new QueryStringDecoder(data, false);
					decoder.parameters().entrySet().forEach(entry -> {
						parmMap.put(entry.getKey(), entry.getValue().get(0));
					});
				}
			} else {
				// 不支持其它方法
				Log.error("不支持的HttpMethod：" + method);
			}
		} catch (Exception e) {
			Log.error("格式化接收到的http数据异常,data:" + data, e);
		}
		return parmMap;
	}

	/**
	 * <pre>
	 * 构建http默认返回信息
	 * </pre>
	 *
	 * @param req
	 * @param msg
	 * @return
	 */
	public final static FullHttpResponse buildHttpMsg(FullHttpRequest req, String msg) {
		return buildHttpMsg(req, msg, "text/plain; charset=UTF-8");
	}

	/**
	 * <pre>
	 * 构建http返回信息
	 * </pre>
	 *
	 * @param req
	 * @param msg
	 * @param type
	 * @return
	 */
	public final static FullHttpResponse buildHttpMsg(FullHttpRequest req, String msg, String type) {
		HttpResponseStatus status = HttpResponseStatus.OK;
		if (req != null) {
			status = req.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST;
		}
		final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
		try {
			response.content().writeBytes(msg.getBytes("UTF-8"));
			response.headers().set("Access-Control-Allow-Origin", "*"); // 跨域
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		} catch (Exception e) {
			response.content().writeBytes("error".getBytes());
			Log.error("buildHttpMsg异常", e);
		}

		return response;
	}

	public final static String getClientIp(Channel channel, HttpRequest mReq) {
		String clientIP = mReq.headers().get("X-Forwarded-For");

		if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
			if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
				clientIP = mReq.headers().get("Proxy-Client-IP");
			}
			if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
				clientIP = mReq.headers().get("WL-Proxy-Client-IP");
			}
			if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
				clientIP = mReq.headers().get("HTTP_CLIENT_IP");
			}
			if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
				clientIP = mReq.headers().get("HTTP_X_FORWARDED_FOR");
			}
			if (clientIP == null || clientIP.length() == 0 || "unknown".equalsIgnoreCase(clientIP)) {
				InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
				clientIP = insocket.getAddress().getHostAddress();
			}
		} else if (clientIP.length() > 15) {
			String[] ips = clientIP.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					clientIP = strIp;
					break;
				}
			}
		}
		return clientIP;
	}

	private HttpMsgUtil() {
	}
}
