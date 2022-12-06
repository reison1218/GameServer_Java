/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * JSON工具类
 * </pre>
 *
 * @author reison
 */
public class JsonUtil {

    // static {
    // JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // }

    /**
     * <pre>
     * 对象序列化为json字符串
     * </pre>
     */
    public final static <T> String stringify(T t) {
        String json = "{}";
        try {
            json = JSON.toJSONString(t, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.UseISO8601DateFormat,
                    SerializerFeature.WriteNullStringAsEmpty);
        } catch (Exception e) {
            Log.error("", e);
        }
        return json;
    }

    /**
     * <pre>
     * 对象序列化为二进制json
     * </pre>
     */
    public final static <T> byte[] binaryify(T t) {
        byte[] json = null;
        try {
            json = JSON.toJSONBytes(t, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.UseISO8601DateFormat,
                    SerializerFeature.WriteNullStringAsEmpty);
        } catch (Exception e) {
            Log.error("", e);
        }
        return json;
    }

    /**
     * <pre>
     * 字符串反序列化为对象
     * </pre>
     */
    public final static <T> T parse(String str, Class<T> valueType) {
        T obj = null;
        try {
            obj = JSON.parseObject(str, valueType, Feature.AllowISO8601DateFormat);
        } catch (Exception e) {
            Log.error("对象反序列化错误,jsonStr:" + str, e);
        }
        return obj;
    }

    /**
     * <pre>
     * 字符串反序列化为复杂对象
     * </pre>
     */
    public final static <T> T parse(String str, TypeReference<T> type) {
        T obj = null;
        try {
            obj = JSON.parseObject(str, type.getType(), Feature.AllowISO8601DateFormat);
        } catch (Exception e) {
            Log.error("对象反序列化错误,jsonStr:" + str, e);
        }
        return obj;
    }

    /**
     * <pre>
     * 二进制json反序列化为对象
     * </pre>
     */
    public final static <T> T parse(byte[] bs, Class<T> valueType) {
        T obj = null;
        try {
            obj = JSON.parseObject(bs, valueType, Feature.AllowISO8601DateFormat);
        } catch (Exception e) {
            Log.error("二进制反序列化错误,bs:" + Arrays.toString(bs), e);
        }
        return obj;
    }

    /**
     * <pre>
     * 字符串反序列化为对象
     * ！{}型json返回JsonObject相当于一个HashMap
     * ！[]型json返回JsonArray相当于一个ArrayList
     * </pre>
     */
    public final static Object parse(String str) {
        Object obj = null;
        try {
            obj = JSON.parse(str, Feature.InitStringFieldAsEmpty);
        } catch (Exception e) {
            Log.error("集合反序列化错误,jsonStr：" + str, e);
        }
        return obj;
    }

    /**
     * <pre>
     * 二进制反序列化为对象
     * ！{}型json返回JsonObject相当于一个HashMap
     * ！[]型json返回JsonArray相当于一个ArrayList
     * </pre>
     */
    public final static Object parse(byte[] bs) {
        if (bs == null) {
            return null;
        }
        Object obj = null;
        try {
            obj = JSON.parse(new String(bs), Feature.AllowISO8601DateFormat);
        } catch (Exception e) {
            try {
                Log.error("集合反序列化错误,jsonStr：" + new String(bs, "UTF-8"), e);
            } catch (Exception e1) {
                Log.error("", e);
            }
        }
        return obj;
    }

    public final static void testNumKey() {
        Map<Object, Object> dataMap = new HashMap<>();
        dataMap.put("1", "value01");
        dataMap.put("2", "value02");
        dataMap.put("3", "value03");
        dataMap.put(3, "value03");
        String jsonStr = stringify(dataMap);
        System.out.println(jsonStr);
    }

    public static void main(String[] args) {
        testNumKey();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "JsonUtil []";
    }

    static class TestJson {
        private int id;
        @JSONField(serialize = false)
        private String name;

        public TestJson(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public final int getId() {
            return id;
        }

        public final void setId(int id) {
            this.id = id;
        }

        public final String getName() {
            return name;
        }

        public final void setName(String name) {
            this.name = name;
        }

    }
}
