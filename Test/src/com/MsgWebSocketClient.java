package com;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import proto.UserProto;
import proto.BattleProto.BattleSettleReq;
import proto.UserProto.ReceiveLineOffGoldReq;

public class MsgWebSocketClient extends WebSocketClient {

	static AtomicInteger j = new AtomicInteger(0);

	public MsgWebSocketClient(String url) throws URISyntaxException {
		super(new URI(url));
	}

	@Override
	public void setConnectionLostTimeout(int connectionLostTimeout) {
		super.setConnectionLostTimeout(connectionLostTimeout);
	}

	@Override
	public void onOpen(ServerHandshake shake) {
		System.out.println("握手...");
		for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext();) {
			String key = it.next();
			System.out.println(key + ":" + shake.getFieldValue(key));
		}
		try {
//			Thread.sleep(1000);
//			this.send(lixian().array());
			Thread.sleep(1000);
			while (true) {
				Thread.sleep(1000);
				this.send(tujian().array());
				// this.send(buySlime().array());
				// this.send(roomSettle().array());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMessage(ByteBuffer bytes) {
		System.out.println("收到二进制消息:" + bytes.array());
	}

	@Override
	public void onMessage(String paramString) {
		System.out.println("接收到文本消息：" + paramString);
	}

	@Override
	public void onClose(int paramInt, String paramString, boolean paramBoolean) {
		System.out.println("关闭..." + paramString);
		System.out.println("客户端数量：" + j.get());
		System.exit(0);
	}

	@Override
	public void onError(Exception e) {
		System.out.println("异常" + e);
	}

	@Override
	public void send(ByteBuffer bytes) {
		super.send(bytes);
	}

	public static void main(String[] aa) {

		// ws://xiaoyouxi.game.i66wan.com/slm/ws
		// ws://192.168.0.147:16801/ws
		// wss://xiaoyouxi.game.i66wan.com/slm/ws
		//
		try {
			// int i = 10;
			//for (int i = 1; i < 99999; i++) {
				// 创建websocket客户端
				MsgWebSocketClient client = new MsgWebSocketClient("ws://192.168.12.3:9999/ws");
				client.setConnectionLostTimeout(99999);
				// 连接
				client.connect();

				WebSocket ws = client.getConnection();
				while (!ws.isOpen()) {
					Thread.sleep(100);
				}
				// 登录
				ws.send(login(1).array());

				j.incrementAndGet();
				//System.out.println("===============================================" + i);
			//}

			System.out.println("客户端数量：" + j.get());

			// 购买史莱姆
			// ws.send(buySlime().array());
			// 请求史莱姆图鉴
			// ws.send(tujian().array());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("客户端数量：" + j.get());
		}
	}

	public static ByteBuf yace(int i) {
		ByteBuf result = Unpooled.buffer();

		UserProto.LoginReq.Builder builder = UserProto.LoginReq.newBuilder();

		builder.setUserId(i);
		builder.setNickname("test");
		builder.setAvatar("test");

		int size = 4 + 4 + 8 + builder.build().toByteArray().length;
		result.writeInt(size);
		result.writeInt(1002);
		result.writeInt(0);
		result.writeInt(0);
		result.writeBytes(builder.build().toByteArray());
		return result;
	}

	public static ByteBuf login(int id) {
		ByteBuf result = Unpooled.buffer();

		UserProto.LoginReq.Builder builder = UserProto.LoginReq.newBuilder();

		builder.setUserId(id);
		builder.setNickname("test" + id);
		builder.setAvatar("test" + id);

		int size = 4 + 4 + 8 + builder.build().toByteArray().length;
		result.writeInt(size);
		result.writeInt(1002);
		result.writeInt(0);
		result.writeInt(0);
		result.writeBytes(builder.build().toByteArray());
		return result;
	}

	public static ByteBuf buySlime() {
		ByteBuf result = Unpooled.buffer();

		int size = 4 + 4 + 8;
		result.writeInt(size);
		result.writeInt(1004);
		result.writeInt(0);
		result.writeInt(0);
		return result;
	}

	public static ByteBuf roomSettle() {
		ByteBuf result = Unpooled.buffer();

		BattleSettleReq.Builder reqBuilder = BattleSettleReq.newBuilder();
		reqBuilder.setCheckPoint(1);
		reqBuilder.setJump(1);
		reqBuilder.setScore(10);
		reqBuilder.setMultiple(2);
		reqBuilder.setAdvertId(2);
		int size = 4 + 4 + 8 + reqBuilder.build().toByteArray().length;
		result.writeInt(size);
		result.writeInt(1010);
		result.writeInt(0);
		result.writeInt(0);
		result.writeBytes(reqBuilder.build().toByteArray());
		return result;
	}

	public static ByteBuf tujian() {
		ByteBuf result = Unpooled.buffer();

		int size = 4 + 4 + 8;
		result.writeInt(size);
		result.writeInt(1013);
		result.writeInt(0);
		result.writeInt(0);
		return result;
	}

	public static ByteBuf lixian() {
		ByteBuf result = Unpooled.buffer();
		ReceiveLineOffGoldReq.Builder reqBuilder = ReceiveLineOffGoldReq.newBuilder();
		reqBuilder.setAdvertId(2);
		int size = 4 + 4 + 8 + reqBuilder.build().toByteArray().length;
		result.writeInt(size);
		result.writeInt(1017);
		result.writeInt(0);
		result.writeInt(0);
		result.writeBytes(reqBuilder.build().toByteArray());
		return result;
	}
}