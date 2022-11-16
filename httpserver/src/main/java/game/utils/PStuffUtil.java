/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.utils;

import com.alibaba.fastjson.JSONObject;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * <pre>
 * ProtoStuff序列化工具
 * </pre>
 * 
 * @author reison
 * @time 2017年5月24日 下午5:09:44
 */
public final class PStuffUtil {

	public final static Schema<JSONObject> SCHEMA_JSON_OBJ = RuntimeSchema.getSchema(JSONObject.class);

	/**
	 * <pre>
	 * 序列化
	 * </pre>
	 *
	 * @param schema
	 * @param packet
	 * @return
	 */
	public final static <T> byte[] serialize(Schema<T> schema, T packet) {
		if (packet == null) {
			return new byte[0];
		}
		LinkedBuffer buffer = LinkedBuffer.allocate();
		byte[] bs = null;
		try {
			bs = ProtostuffIOUtil.toByteArray(packet, schema, buffer);
		} catch (Exception e) {
			Log.error("ProtoStuff序列化异常", e);
		} finally {
			buffer.clear();
		}
		return bs;
	}

	/**
	 * <pre>
	 * 反序列化
	 * </pre>
	 *
	 * @param schema
	 * @param bs
	 * @param packet
	 * @return
	 */
	public final static <T> T deserialize(Schema<T> schema, byte[] bs, T packet) {
		if (bs == null || bs.length < 1) {
			return packet;
		}
		T tt = null;
		try {
			ProtostuffIOUtil.mergeFrom(bs, packet, schema);
			tt = packet;
		} catch (Exception e) {
			Log.error("ProtoStuff反序列化异常", e);
		}
		return tt;
	}

	public final static void benchMark() {
		String str = "{\"p3\":{\"damage\":[{\"dt\":1,\"t\":3,\"v\":9249,\"id\":\"102490180801640_3001008\"}],\"uid\":14,\"atky\":519,\"atkx\":661,\"sid\":10}}";
		JSONObject packet = (JSONObject) JsonUtil.parse(str);
		long st = System.currentTimeMillis();
		final int count = 100000;
		for (int i = 0; i < count; i++) {
			byte[] bs = serialize(SCHEMA_JSON_OBJ, packet);
			JSONObject newPacket = new JSONObject();
			deserialize(SCHEMA_JSON_OBJ, bs, newPacket);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("count:" + count + ",cost:" + ct + "ms");

	}

	public static void main(String[] args) {
		// Schema<HashMap> schema = RuntimeSchema.getSchema(HashMap.class);
		// HashMap<String, Object> map = new HashMap<>();
		// map.put("key01", 1);
		// map.put("key02", "哈哈qwe");
		// map.put("key03", new Date());
		// byte[] bs = serialize(schema, map);
		// System.out.println(bs.length);
		// HashMap<String, Object> map1 = new HashMap<>();
		// deserialize(schema, bs, map1);
		// System.out.println(map1.get("key03"));
		// Schema<JSONObject> schema = RuntimeSchema.getSchema(JSONObject.class);
		// JSONObject map = new JSONObject();
		// map.put("key01", 1);
		// map.put("key02", "哈哈qwe");
		// map.put("key03", new Date());
		// byte[] bs = serialize(schema, map);
		// System.out.println(bs.length + "," + JsonUtil.binaryify(map).length);
		// JSONObject map1 = new JSONObject();
		// deserialize(schema, bs, map1);
		// System.out.println(map1.getDate("key03"));
		benchMark();
		benchMark();
		benchMark();
	}

}
