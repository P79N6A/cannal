package com.alibaba.otter.canal.example.bean;

import java.io.Serializable;

/**
 * 振动实时表javabean
 */
public class Record implements Serializable {
    private String ID;                                                          //ID
    private String ITEMCODE;                                            //测点编码
    private String ITEM_ID;                                                //ITEM_ID
    private double MONI_VALUE;                                      //振动值
    private String MONI_STATE;                                        //振动状态
    private String MONI_TIME;                                          //推送时间
    private double TEMPER_VALUE;                                   //温度值
    private Long EVENT_TIME;                                          //事件时长
    private String REMARK;                                               //备注
    private String LEVEL;                                                   //报警级别
    private String NUMBER;                                              //序号
    private String HORIZONTAL_COORDINATE;               //横坐标
    private String VERTICAL_COORDINATE;                      //纵坐标

    public Record() {
        super();
    }

    public Record(String ID, String ITEMCODE, String ITEM_ID, double MONI_VALUE, String MONI_STATE, String MONI_TIME, double TEMPER_VALUE,
                  Long EVENT_TIME, String REMARK, String LEVEL, String NUMBER, String HORIZONTAL_COORDINATE, String VERTICAL_COORDINATE) {
        this.ID = ID;
        this.ITEMCODE = ITEMCODE;
        this.ITEM_ID = ITEM_ID;
        this.MONI_VALUE = MONI_VALUE;
        this.MONI_STATE = MONI_STATE;
        this.MONI_TIME = MONI_TIME;
        this.TEMPER_VALUE = TEMPER_VALUE;
        this.EVENT_TIME = EVENT_TIME;
        this.REMARK = REMARK;
        this.LEVEL = LEVEL;
        this.NUMBER = NUMBER;
        this.HORIZONTAL_COORDINATE = HORIZONTAL_COORDINATE;
        this.VERTICAL_COORDINATE = VERTICAL_COORDINATE;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getITEMCODE() {
        return ITEMCODE;
    }

    public void setITEMCODE(String ITEMCODE) {
        this.ITEMCODE = ITEMCODE;
    }

    public String getITEM_ID() {
        return ITEM_ID;
    }

    public void setITEM_ID(String ITEM_ID) {
        this.ITEM_ID = ITEM_ID;
    }

    public double getMONI_VALUE() {
        return MONI_VALUE;
    }

    public void setMONI_VALUE(double MONI_VALUE) {
        this.MONI_VALUE = MONI_VALUE;
    }

    public String getMONI_STATE() {
        return MONI_STATE;
    }

    public void setMONI_STATE(String MONI_STATE) {
        this.MONI_STATE = MONI_STATE;
    }

    public String getMONI_TIME() {
        return MONI_TIME;
    }

    public void setMONI_TIME(String MONI_TIME) {
        this.MONI_TIME = MONI_TIME;
    }

    public double getTEMPER_VALUE() {
        return TEMPER_VALUE;
    }

    public void setTEMPER_VALUE(double TEMPER_VALUE) {
        this.TEMPER_VALUE = TEMPER_VALUE;
    }

    public Long getEVENT_TIME() {
        return EVENT_TIME;
    }

    public void setEVENT_TIME(Long EVENT_TIME) {
        this.EVENT_TIME = EVENT_TIME;
    }

    public String getREMARK() {
        return REMARK;
    }

    public void setREMARK(String REMARK) {
        this.REMARK = REMARK;
    }

    public String getLEVEL() {
        return LEVEL;
    }

    public void setLEVEL(String LEVEL) {
        this.LEVEL = LEVEL;
    }

    public String getNUMBER() {
        return NUMBER;
    }

    public void setNUMBER(String NUMBER) {
        this.NUMBER = NUMBER;
    }

    public String getHORIZONTAL_COORDINATE() {
        return HORIZONTAL_COORDINATE;
    }

    public void setHORIZONTAL_COORDINATE(String HORIZONTAL_COORDINATE) {
        this.HORIZONTAL_COORDINATE = HORIZONTAL_COORDINATE;
    }

    public String getVERTICAL_COORDINATE() {
        return VERTICAL_COORDINATE;
    }

    public void setVERTICAL_COORDINATE(String VERTICAL_COORDINATE) {
        this.VERTICAL_COORDINATE = VERTICAL_COORDINATE;
    }

    @Override
    public String toString() {
        return "Record{" +
                "ID='" + ID + '\'' +
                ", ITEMCODE='" + ITEMCODE + '\'' +
                ", ITEM_ID='" + ITEM_ID + '\'' +
                ", MONI_VALUE=" + MONI_VALUE +
                ", MONI_STATE='" + MONI_STATE + '\'' +
                ", MONI_TIME='" + MONI_TIME + '\'' +
                ", TEMPER_VALUE=" + TEMPER_VALUE +
                ", EVENT_TIME=" + EVENT_TIME +
                ", REMARK='" + REMARK + '\'' +
                ", LEVEL='" + LEVEL + '\'' +
                ", NUMBER='" + NUMBER + '\'' +
                ", HORIZONTAL_COORDINATE='" + HORIZONTAL_COORDINATE + '\'' +
                ", VERTICAL_COORDINATE='" + VERTICAL_COORDINATE + '\'' +
                '}';
    }
}
