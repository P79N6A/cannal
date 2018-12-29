package com.alibaba.otter.canal.example.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class HttpClientUtil {

    //推送微信消息的URL
    final static String URL = "";

    /**
     * 基于HTTPClient3.1发送post请求
     *
     * @param sendMap        需要传递的参数 格式 json
     * @param requestUrl    请求的url
     * @return 响应信息
     * @throws IOException
     */
    //@param authorization 验证信息 64位编码（username+pwd）
    //public static String sendPost(String params, String requestUrl, String authorization) throws IOException {
    public static String sendPost(Map sendMap, String requestUrl) throws IOException {
        String params = JSON.toJSONString(sendMap);         //将参数转成json串，传递到微信服务器
        byte[] requestBytes = params.getBytes("GBK"); // 将参数转为二进制流
        HttpClient httpClient = new HttpClient();// 客户端实例化
        PostMethod postMethod = new PostMethod(requestUrl);
        postMethod.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        //NameValuePair[] param = map2NameValuePair(sendMap);
        //设置请求头Authorization
        //postMethod.setRequestHeader("Authorization", "Basic " + authorization);
        // 设置请求头  Content-Type
        postMethod.setRequestHeader("Content-Type", "application/xml");
        InputStream inputStream = new ByteArrayInputStream(requestBytes, 0,
                requestBytes.length);
        RequestEntity requestEntity = new InputStreamRequestEntity(inputStream,
                requestBytes.length, "application/xml; charset=utf-8"); // 请求体
        postMethod.setRequestEntity(requestEntity);
        //postMethod.setRequestBody(param);

        int stat = httpClient.executeMethod(postMethod);// 执行请求
        InputStream soapResponseStream = postMethod.getResponseBodyAsStream();// 获取返回的流
        byte[] datas = null;
        try {
            datas = readInputStream(soapResponseStream);// 从输入流中读取数据
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //System.out.println("请求线程" + Thread.currentThread().getName() + "," + System.currentTimeMillis() + ", 所有线程已完成，开始进入下一步！");
            soapResponseStream.close();
        }
        String result = new String(datas, "GBK");// 将二进制流转为String
        return result;

    }

    /**
     * 将传递的数据变成HttpClient需要的格式
     * @param sendMap
     * @return
     */
    private static NameValuePair[] map2NameValuePair(Map sendMap) {
        if(sendMap != null && sendMap.size() > 0){
            int size = sendMap.size();
            NameValuePair[] arr = new NameValuePair[size];
            Set set = sendMap.keySet();
            Iterator iterator = set.iterator();
            int i = 0;
            while (iterator.hasNext()){
                String key = (String)iterator.next();
                String value = (String)sendMap.get(key);
                NameValuePair nameValuePair = new NameValuePair(key, value);
                arr[i] = nameValuePair;
                i++;
            }
            return arr;
        }
        return null;
    }

    /**
     * base 64 encode
     *
     * @param uname
     * @return 编码后的base 64 code
     */
    public static String base64Encode(String uname, String pwd) {
        String auth = uname + ":" + pwd;
        byte[] encodedAuth = auth.getBytes(Charset.forName("US-ASCII"));
        return new BASE64Encoder().encode(encodedAuth);
    }

    /**
     * 从输入流中读取数据
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }
}