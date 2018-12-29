package com.alibaba.otter.canal.example.bean;

import java.io.Serializable;

public class StBean implements Serializable {

    private String ID;                                                                                         //测点序号
    private String EQ_EQUIP;                                                                            //实体设备编码
    private String EQ_CODE;                                                                             //监测设备编码
    private String EQ_NAME;                                                                            //监测设备名称
    private String SENDTIME;                                                                           //发送时间
    private String RESULT_TABLE;                                                                     //结果表
    private Double WARNING_VALUE;                                                              //预警阈值
    private Double ALARM_VALUE;                                                                   //报警阈值
    private Double DANGER_VALUE;                                                                 //危险阈值
    private Double STRESS;                                                                               //应力值
    private Double TEMPER;                                                                              //温度值
    private String POINT_POSITION;                                                                 //位置描述
    private String HORIZONTAL_COORDINATE;                                                //横坐标
    private String VERTICAL_COORDINATE;                                                      //纵坐标
    private String STATE;                                                                                    //状态
    private String LEVEL;                                                                                    //级别

    public StBean() { }

    public StBean(String ID, String EQ_EQUIP, String EQ_CODE, String EQ_NAME, String SENDTIME, String RESULT_TABLE,
                  Double WARNING_VALUE, Double ALARM_VALUE, Double DANGER_VALUE, Double STRESS, Double TEMPER,
                  String POINT_POSITION, String HORIZONTAL_COORDINATE, String VERTICAL_COORDINATE, String STATE, String LEVEL) {
        this.ID = ID;
        this.EQ_EQUIP = EQ_EQUIP;
        this.EQ_CODE = EQ_CODE;
        this.EQ_NAME = EQ_NAME;
        this.SENDTIME = SENDTIME;
        this.RESULT_TABLE = RESULT_TABLE;
        this.WARNING_VALUE = WARNING_VALUE;
        this.ALARM_VALUE = ALARM_VALUE;
        this.DANGER_VALUE = DANGER_VALUE;
        this.STRESS = STRESS;
        this.TEMPER = TEMPER;
        this.POINT_POSITION = POINT_POSITION;
        this.HORIZONTAL_COORDINATE = HORIZONTAL_COORDINATE;
        this.VERTICAL_COORDINATE = VERTICAL_COORDINATE;
        this.STATE = STATE;
        this.LEVEL = LEVEL;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEQ_EQUIP() {
        return EQ_EQUIP;
    }

    public void setEQ_EQUIP(String EQ_EQUIP) {
        this.EQ_EQUIP = EQ_EQUIP;
    }

    public String getEQ_CODE() {
        return EQ_CODE;
    }

    public void setEQ_CODE(String EQ_CODE) {
        this.EQ_CODE = EQ_CODE;
    }

    public String getEQ_NAME() {
        return EQ_NAME;
    }

    public void setEQ_NAME(String EQ_NAME) {
        this.EQ_NAME = EQ_NAME;
    }

    public String getSENDTIME() {
        return SENDTIME;
    }

    public void setSENDTIME(String SENDTIME) {
        this.SENDTIME = SENDTIME;
    }

    public String getRESULT_TABLE() {
        return RESULT_TABLE;
    }

    public void setRESULT_TABLE(String RESULT_TABLE) {
        this.RESULT_TABLE = RESULT_TABLE;
    }

    public Double getWARNING_VALUE() {
        return WARNING_VALUE;
    }

    public void setWARNING_VALUE(Double WARNING_VALUE) {
        this.WARNING_VALUE = WARNING_VALUE;
    }

    public Double getALARM_VALUE() {
        return ALARM_VALUE;
    }

    public void setALARM_VALUE(Double ALARM_VALUE) {
        this.ALARM_VALUE = ALARM_VALUE;
    }

    public Double getDANGER_VALUE() {
        return DANGER_VALUE;
    }

    public void setDANGER_VALUE(Double DANGER_VALUE) {
        this.DANGER_VALUE = DANGER_VALUE;
    }

    public Double getSTRESS() {
        return STRESS;
    }

    public void setSTRESS(Double STRESS) {
        this.STRESS = STRESS;
    }

    public Double getTEMPER() {
        return TEMPER;
    }

    public void setTEMPER(Double TEMPER) {
        this.TEMPER = TEMPER;
    }

    public String getPOINT_POSITION() {
        return POINT_POSITION;
    }

    public void setPOINT_POSITION(String POINT_POSITION) {
        this.POINT_POSITION = POINT_POSITION;
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

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    public String getLEVEL() {
        return LEVEL;
    }

    public void setLEVEL(String LEVEL) {
        this.LEVEL = LEVEL;
    }

    @Override
    public String toString() {
        return "StBean{" +
                "ID='" + ID + '\'' +
                ", EQ_EQUIP='" + EQ_EQUIP + '\'' +
                ", EQ_CODE='" + EQ_CODE + '\'' +
                ", EQ_NAME='" + EQ_NAME + '\'' +
                ", SENDTIME='" + SENDTIME + '\'' +
                ", RESULT_TABLE='" + RESULT_TABLE + '\'' +
                ", WARNING_VALUE=" + WARNING_VALUE +
                ", ALARM_VALUE=" + ALARM_VALUE +
                ", DANGER_VALUE=" + DANGER_VALUE +
                ", STRESS=" + STRESS +
                ", TEMPER=" + TEMPER +
                ", POINT_POSITION='" + POINT_POSITION + '\'' +
                ", HORIZONTAL_COORDINATE='" + HORIZONTAL_COORDINATE + '\'' +
                ", VERTICAL_COORDINATE='" + VERTICAL_COORDINATE + '\'' +
                ", STATE='" + STATE + '\'' +
                ", LEVEL='" + LEVEL + '\'' +
                '}';
    }
}
