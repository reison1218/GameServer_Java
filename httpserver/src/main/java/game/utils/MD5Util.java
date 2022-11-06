package game.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MD5Util {
    /**
     * 16进制字符集
     */
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static MessageDigest md5Digt = null;
    private static MessageDigest sha1Digt = null;

    static {
        try {
            md5Digt = MessageDigest.getInstance("MD5");
            sha1Digt = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * * MD5加密字符串
     *
     * @param str 目标字符串
     * @return MD5加密后的字符串
     */

    public static String getMD5String(String str) {
        return getMD5String(str.getBytes());
    }

    /**
     * * MD5加密以byte数组表示的字符串
     *
     * @param bytes 目标byte数组
     * @return MD5加密后的字符串
     */

    public static String getMD5String(byte[] bytes) {
        md5Digt.update(bytes);
        return bytesToHex(md5Digt.digest());
    }

    /**
     * * 将字节数组转换成16进制字符串
     *
     * @param bytes 目标字节数组
     * @return 转换结果
     */
    private static String bytesToHex(byte bytes[]) {
        return bytesToHex(bytes, 0, bytes.length);

    }

    /**
     * * 将字节数组中指定区间的子数组转换成16进制字符串
     *
     * @param bytes 目标字节数组
     * @param start 起始位置（包括该位置）
     * @param end   结束位置（不包括该位置）
     * @return 转换结果
     */
    private static String bytesToHex(byte bytes[], int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();

    }

    /**
     * * 将单个字节码转换成16进制字符串
     *
     * @param bt 目标字节
     * @return 转换结果
     */
    private static String byteToHex(byte bt) {
        return HEX_DIGITS[(bt & 0xf0) >> 4] + "" + HEX_DIGITS[bt & 0xf];

    }

    /**
     * <pre>
     * sha1加密
     * </pre>
     */
    public static String sha1(String str) {
        if (null == str || 0 == str.length()) {
            return "";
        }
        try {
            sha1Digt.update(str.getBytes("UTF-8"));

            byte[] md = sha1Digt.digest();
            int j = md.length;
            char[] buf = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
                buf[k++] = HEX_DIGITS[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * * MD5加密map
     *
     * @param map 目标map
     * @return MD5加密后的字符串
     */

    public static String getMD5String(Map<String, Object> map, String privateKey) {
        String str = "";
        //		Map<String, Object> sortMapByKey = MapUtil.sortMapByKey(map); // 按Key进行排序
        Map<String, Object> sortMapByKey = map; // 按Key进行排序
        for (String key : sortMapByKey.keySet()) {
            str += (key + "=" + sortMapByKey.get(key));
            str += "&";
        }
        str += privateKey;
        Log.info("加密用的字符串：" + str);
        return getMD5String(str.getBytes()).toLowerCase();
    }

}
