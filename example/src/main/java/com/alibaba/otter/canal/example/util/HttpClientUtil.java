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

    //����΢����Ϣ��URL
    final static String URL = "";

    /**
     * ����HTTPClient3.1����post����
     *
     * @param sendMap        ��Ҫ���ݵĲ��� ��ʽ json
     * @param requestUrl    �����url
     * @return ��Ӧ��Ϣ
     * @throws IOException
     */
    //@param authorization ��֤��Ϣ 64λ���루username+pwd��
    //public static String sendPost(String params, String requestUrl, String authorization) throws IOException {
    public static String sendPost(Map sendMap, String requestUrl) throws IOException {
        String params = JSON.toJSONString(sendMap);         //������ת��json�������ݵ�΢�ŷ�����
        byte[] requestBytes = params.getBytes("GBK"); // ������תΪ��������
        HttpClient httpClient = new HttpClient();// �ͻ���ʵ����
        PostMethod postMethod = new PostMethod(requestUrl);
        postMethod.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        //NameValuePair[] param = map2NameValuePair(sendMap);
        //��������ͷAuthorization
        //postMethod.setRequestHeader("Authorization", "Basic " + authorization);
        // ��������ͷ  Content-Type
        postMethod.setRequestHeader("Content-Type", "application/xml");
        InputStream inputStream = new ByteArrayInputStream(requestBytes, 0,
                requestBytes.length);
        RequestEntity requestEntity = new InputStreamRequestEntity(inputStream,
                requestBytes.length, "application/xml; charset=utf-8"); // ������
        postMethod.setRequestEntity(requestEntity);
        //postMethod.setRequestBody(param);

        int stat = httpClient.executeMethod(postMethod);// ִ������
        InputStream soapResponseStream = postMethod.getResponseBodyAsStream();// ��ȡ���ص���
        byte[] datas = null;
        try {
            datas = readInputStream(soapResponseStream);// ���������ж�ȡ����
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //System.out.println("�����߳�" + Thread.currentThread().getName() + "," + System.currentTimeMillis() + ", �����߳�����ɣ���ʼ������һ����");
            soapResponseStream.close();
        }
        String result = new String(datas, "GBK");// ����������תΪString
        return result;

    }

    /**
     * �����ݵ����ݱ��HttpClient��Ҫ�ĸ�ʽ
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
     * @return ������base 64 code
     */
    public static String base64Encode(String uname, String pwd) {
        String auth = uname + ":" + pwd;
        byte[] encodedAuth = auth.getBytes(Charset.forName("US-ASCII"));
        return new BASE64Encoder().encode(encodedAuth);
    }

    /**
     * ���������ж�ȡ����
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