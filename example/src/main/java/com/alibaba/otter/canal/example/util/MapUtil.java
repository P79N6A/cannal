package com.alibaba.otter.canal.example.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Map������
 *
 * @author jqlin
 */
public class MapUtil {

    /**
     * ��map�����л�ȡ����ֵ
     * 
     * @param <E>
     * @param map
     *            map����
     * @param key
     *            ����
     * @param defaultValue
     *            Ĭ��ֵ
     * @return
     * @author jiqinlin
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final static <E> E get(Map map, Object key, E defaultValue) {
        Object o = map.get(key);
        if (o == null)
            return defaultValue;
        return (E) o;
    }
    
    /**
     * Map���϶���ת���� JavaBean���϶���
     * 
     * @param javaBean JavaBeanʵ������
     * @param mapList Map���ݼ�����
     * @return
     * @author jqlin
     */
    @SuppressWarnings({ "rawtypes" })
    public static <T> List<T> map2Java(T javaBean, List<Map> mapList) {
        if(mapList == null || mapList.isEmpty()){
            return null;
        }
        List<T> objectList = new ArrayList<T>();
        
        T object = null;
        for(Map map : mapList){
            if(map != null){
                object = map2Java(javaBean, map);
                objectList.add(object);
            }
        }
        
        return objectList;
        
    }
    
    /**
     * Map����ת���� JavaBean����
     * 
     * @param javaBean JavaBeanʵ������
     * @param map Map����
     * @return
     * @author jqlin
     */
    @SuppressWarnings({ "rawtypes","unchecked", "hiding" })
    public static <T> T map2Java(T javaBean, Map map) {
        try {
            // ��ȡjavaBean����
            BeanInfo beanInfo = Introspector.getBeanInfo(javaBean.getClass());
            // ���� JavaBean ����
            Object obj = javaBean.getClass().newInstance();

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                String propertyName = null; // javaBean������
                Object propertyValue = null; // javaBean����ֵ
                for (PropertyDescriptor pd : propertyDescriptors) {
                    propertyName = pd.getName();
                    if (map.containsKey(propertyName)) {
                        propertyValue = map.get(propertyName);
                        pd.getWriteMethod().invoke(obj, new Object[] { propertyValue });
                    }
                }
                return (T) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * JavaBean����ת����Map����
     * 
     * @param javaBean
     * @return
     * @author jqlin
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map java2Map(Object javaBean) {
        Map map = new HashMap();
         
        try {
            // ��ȡjavaBean����
            BeanInfo beanInfo = Introspector.getBeanInfo(javaBean.getClass());

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                String propertyName = null; // javaBean������
                Object propertyValue = null; // javaBean����ֵ
                for (PropertyDescriptor pd : propertyDescriptors) {
                    propertyName = pd.getName();
                    if (!propertyName.equals("class")) {
                        Method readMethod = pd.getReadMethod();
                        propertyValue = readMethod.invoke(javaBean, new Object[0]);
                        map.put(propertyName, propertyValue);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }  
        
        return map;
    }
 
}
