package game.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <pre>
 * HTTP请求工具类
 * </pre>
 *
 * @author reison
 * @time 2017年5月12日 下午3:37:20
 */
public class HttpUtil {

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";

    /**
     * 连接超时
     */
    private static int CONNECT_TIME_OUT = 5000;

    /**
     * 读取数据超时
     */
    private static int READ_TIME_OUT = 5000;

    /**
     * 请求编码
     */
    private static String REQUEST_ENCODING = "UTF-8";

    /**
     * <pre>
     * 发送带参数的GET的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, Map<String, Object> paramMap) {
        return doRequest(reqUrl, paramMap, REQUEST_METHOD_GET, REQUEST_ENCODING, false);
    }

    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doPost(String reqUrl, Map<String, Object> paramMap, boolean isJson) {
        return doRequest(reqUrl, paramMap, REQUEST_METHOD_POST, REQUEST_ENCODING, isJson);
    }

    /**
     * <pre>
     * 发送带参数的GET的HTTP请求
     * ！返回JSON格式数据
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static Object doGetBackJson(String reqUrl, Map<String, Object> paramMap) {
        String str = doRequest(reqUrl, paramMap, REQUEST_METHOD_GET, REQUEST_ENCODING, false);
        if (str == null) {
            return null;
        }
        return JsonUtil.parse(str);
    }

    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * ！返回JSON格式数据
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static Object doPostBackJson(String reqUrl, Map<String, Object> paramMap, boolean isJson) {
        String str = doRequest(reqUrl, paramMap, REQUEST_METHOD_POST, REQUEST_ENCODING, isJson);
        if (str == null) {
            return null;
        }
        return JsonUtil.parse(str);
    }

    private static String doRequest(String reqUrl, Map<String, Object> paramMap, String reqMethod, String recvEncoding, boolean isJson) {
        HttpURLConnection urlCon = null;
        String responseContent = null;
        try {
            StringBuilder params = new StringBuilder();
            URL url = null;
            String fullUrl = reqUrl;
            if (paramMap != null) {

                if (isJson) {
                    params = new StringBuilder(JsonUtil.stringify(paramMap));
                } else {
                    for (Entry<String, Object> element : paramMap.entrySet()) {
                        params.append(element.getKey());
                        params.append("=");
                        Object obj = element.getValue();
                        if (obj == null) {
                            obj = "0";
                        }
                        params.append(URLEncoder.encode(obj.toString(), REQUEST_ENCODING));
                        params.append("&");
                    }

                    if (params.length() > 0) {
                        params = params.deleteCharAt(params.length() - 1);
                    }
                }
                if (reqMethod.equals(REQUEST_METHOD_GET)) {
                    fullUrl += "?";
                    fullUrl += params.toString();

                }
            }
            url = new URL(fullUrl);
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setRequestMethod(reqMethod);
            urlCon.setConnectTimeout(CONNECT_TIME_OUT);
            urlCon.setReadTimeout(READ_TIME_OUT);
            if (reqMethod.equals(REQUEST_METHOD_POST)) {
                urlCon.setDoOutput(true);
                byte[] b = params.toString().getBytes();
                urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(b.length));
                urlCon.getOutputStream().write(b, 0, b.length);
                urlCon.getOutputStream().flush();
                urlCon.getOutputStream().close();
            }

            InputStream in = urlCon.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
            urlCon.getResponseMessage();
        } catch (IOException e) {
            Log.error("urlconnection error , url " + reqUrl, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return responseContent;
    }


    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * ！返回JSON格式数据
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static JSONObject doPostBackJson(String reqUrl, JSONObject params) {
        String str = doRequest(reqUrl, params, REQUEST_METHOD_POST, REQUEST_ENCODING);
        if (str == null) {
            return null;
        }
        return (JSONObject) JsonUtil.parse(str);
    }

    public static String doPost(String reqUrl, JSONObject params) {
        return doRequest(reqUrl, params, REQUEST_METHOD_POST, REQUEST_ENCODING);
    }

    public static JSONObject doPostBackJson(String reqUrl, String params) {
        String str = doRequest(reqUrl, params, REQUEST_METHOD_POST, REQUEST_ENCODING);
        if (str == null) {
            return null;
        }
        return (JSONObject) JsonUtil.parse(str);
    }

    public static Object doGetBackJson(String reqUrl, JSONObject params) {
        String str = doRequest(reqUrl, params, REQUEST_METHOD_GET, REQUEST_ENCODING);
        if (str == null) {
            return null;
        }
        return JsonUtil.parse(str);
    }

    private static String doRequest(String reqUrl, String params, String reqMethod, String recvEncoding) {
        HttpURLConnection urlCon = null;
        String responseContent = null;
        try {
            URL url = null;
            String fullUrl = reqUrl;
            if (params != null) {
                if (reqMethod.equals(REQUEST_METHOD_GET)) {
                    fullUrl += "?";
                    fullUrl += params;

                }
            }
            url = new URL(fullUrl);
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setRequestMethod(reqMethod);
            urlCon.setConnectTimeout(CONNECT_TIME_OUT);
            urlCon.setReadTimeout(READ_TIME_OUT);
            if (reqMethod.equals(REQUEST_METHOD_POST)) {
                urlCon.setDoOutput(true);
                byte[] b = new byte[0];
                if (params != null) {
                    b = params.getBytes();
                }
                urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(b.length));
                urlCon.getOutputStream().write(b, 0, b.length);
                urlCon.getOutputStream().flush();
                urlCon.getOutputStream().close();
            }

            InputStream in = urlCon.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
            urlCon.getResponseMessage();
        } catch (IOException e) {
            Log.error("urlconnection error , url " + reqUrl, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return responseContent;
    }

    private static String doRequest(String reqUrl, JSONObject params, String reqMethod, String recvEncoding) {
        HttpURLConnection urlCon = null;
        String responseContent = null;
        try {
            StringBuilder paramsStr = new StringBuilder();
            URL url = null;
            String fullUrl = reqUrl;
            if (params != null) {

                if (reqMethod.equals(REQUEST_METHOD_GET)) {
                    for (Entry<String, Object> element : params.entrySet()) {
                        paramsStr.append(element.getKey());
                        paramsStr.append("=");
                        Object obj = element.getValue();
                        if (obj == null) {
                            obj = "0";
                        }
                        paramsStr.append(URLEncoder.encode(obj.toString(), REQUEST_ENCODING));
                        paramsStr.append("&");
                    }

                    if (paramsStr.length() > 0) {
                        paramsStr = paramsStr.deleteCharAt(paramsStr.length() - 1);
                    }

                    fullUrl += "?";
                    fullUrl += params.toString();

                } else {
                    paramsStr.append(params.toJSONString());
                }
            }
            url = new URL(fullUrl);
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setRequestMethod(reqMethod);
            urlCon.setConnectTimeout(CONNECT_TIME_OUT);
            urlCon.setReadTimeout(READ_TIME_OUT);
            if (reqMethod.equals(REQUEST_METHOD_POST)) {
                urlCon.setDoOutput(true);
                byte[] b = paramsStr.toString().getBytes();
                urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(b.length));
                urlCon.getOutputStream().write(b, 0, b.length);
                urlCon.getOutputStream().flush();
                urlCon.getOutputStream().close();
            }

            InputStream in = urlCon.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
            urlCon.getResponseMessage();
        } catch (IOException e) {
            Log.error("urlconnection error , url " + reqUrl, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return responseContent;
    }

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("auth", "DYluhhXUNHFsv1mAzGRrl9n1W4Jkvdsv");
        paramMap.put("key", "qwe123");
        paramMap.put("uid", "1");
        paramMap.put("head", "2");
        paramMap.put("name", "qwe1");
        paramMap.put("vip", "1");
        paramMap.put("card", "3");
        Object msg = doGetBackJson("http://10.10.10.95:8181/server/reg", paramMap);
        paramMap.put("key", "qwe456");
        paramMap.put("uid", "2");
        msg = doGetBackJson("http://10.10.10.95:8181/server/reg", paramMap);
        System.out.println(msg);
    }

}
