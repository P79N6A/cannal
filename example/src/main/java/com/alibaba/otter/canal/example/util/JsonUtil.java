package com.alibaba.otter.canal.example.util;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonUtil {
	
	/**
     * Bean����תJSON
     * 
     * @param object
     * @return
     */
    public static String beanToJson(Object object) {
        if (object != null) {
            return JSON.toJSONString(object);
        } else {
            return null;
        }
    }
	
	/**
     * StringתJSON�ַ���
     * 
     * @param key
     * @param value
     * @return
     */
    public static String stringToJsonByFastjson(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return beanToJson(map);
    }

    /**
     * ��json�ַ���ת���ɶ���
     * 
     * @param json
     * @param clazz
     * @return
     */
    public static Object jsonToBean(String json, Object clazz) {
        if (StringUtils.isBlank(json) || clazz == null) {
            return null;
        }
        return JSON.parseObject(json, clazz.getClass());
    }

    /**
     * json�ַ���תmap
     * 
     * @param json
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, Map.class);
    }

	public static String toJson(Object o){
		if(o instanceof Map){
			return toJson((Map)o);
		}else if(o instanceof List){
			return toJson((List)o);
		}else{
			return null;

		}
	}
}
