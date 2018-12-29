package com.alibaba.otter.canal.example.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataBean implements Serializable {

    private static final long serialVersionUID = 2575058090938199362L;
    private String dictCode=null;
    private String pkField="ID";
    private String keyWhere = null;
    private String orderby = null;


    /** 存放属性的值集 */
    private Map<String, Object> values = new HashMap<String, Object>();

    public DataBean() {

    }

    public DataBean(String dictCode) {
        this.dictCode=dictCode;
    }
    public String getId(){
        return (String)values.get(pkField);
    }
    public void setId(String Id){
        values.put(pkField, Id);
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getPkField() {
        return pkField;
    }

    public void setPkField(String pkField) {
        this.pkField = pkField;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }
    public Object get(String key) {
        if (values.get(key) == null) {
            return "";
        } else {
            return values.get(key);
        }

    }

    public String getString(String key){
        if ((String)values.get(key) == null) {
            return "";
        } else {
            return (String)values.get(key);
        }
    }

    public Map getValues() {
        return values;
    }

    public void clear() {
        this.values.clear();
    }

    public String getKeyWhere() {
        return keyWhere;
    }

    public void setKeyWhere(String keyWhere) {
        this.keyWhere = keyWhere;
    }

    public String getOrderby() {
        return orderby;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

}
