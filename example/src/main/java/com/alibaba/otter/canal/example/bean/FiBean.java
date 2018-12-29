package com.alibaba.otter.canal.example.bean;

import java.io.Serializable;

public class FiBean  implements Serializable {

    private String Index;                                                           //索引
    private String DeviceID;                                                     //设备名称
    private String SendTime;                                                    //发送时间
    private String EndTime;                                                      //索引结束时间
    private String DeviceValue;                                                //测量值
    //private String DataType;                                                 //数据类型
    private String Quality;                                                        //数据质量
    private String Description;                                                 //数据描述
    private String PROJECT_ID;                                                //工程编码
    private String PART_CODE;                                                //分区编码
    private String PART_NAME;                                               //分区名称
    private String LOCATION_DESCRIB;                                   //位置描述
    private String HORIZONTAL_COORDINATE;                      //横坐标
    private String VERTICAL_COORDINATE;                             //纵坐标
    private String CONTROL_CODE;                                        //控制器号
    private String ONETIME_CODE;                                         //一次码
    private String LOOP_CODE;                                              //回路号

    public FiBean() {
    }

    public FiBean(String index, String deviceID, String sendTime, String endTime, String deviceValue, String quality, String description,
                  String PROJECT_ID, String PART_CODE, String PART_NAME, String LOCATION_DESCRIB, String HORIZONTAL_COORDINATE,
                  String VERTICAL_COORDINATE, String CONTROL_CODE, String ONETIME_CODE, String LOOP_CODE) {
        Index = index;
        DeviceID = deviceID;
        SendTime = sendTime;
        EndTime = endTime;
        DeviceValue = deviceValue;
        Quality = quality;
        Description = description;
        this.PROJECT_ID = PROJECT_ID;
        this.PART_CODE = PART_CODE;
        this.PART_NAME = PART_NAME;
        this.LOCATION_DESCRIB = LOCATION_DESCRIB;
        this.HORIZONTAL_COORDINATE = HORIZONTAL_COORDINATE;
        this.VERTICAL_COORDINATE = VERTICAL_COORDINATE;
        this.CONTROL_CODE = CONTROL_CODE;
        this.ONETIME_CODE = ONETIME_CODE;
        this.LOOP_CODE = LOOP_CODE;
    }

    public String getIndex() {
        return Index;
    }

    public void setIndex(String index) {
        Index = index;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public String getSendTime() {
        return SendTime;
    }

    public void setSendTime(String sendTime) {
        SendTime = sendTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getDeviceValue() {
        return DeviceValue;
    }

    public void setDeviceValue(String deviceValue) {
        DeviceValue = deviceValue;
    }

    public String getQuality() {
        return Quality;
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPROJECT_ID() {
        return PROJECT_ID;
    }

    public void setPROJECT_ID(String PROJECT_ID) {
        this.PROJECT_ID = PROJECT_ID;
    }

    public String getPART_CODE() {
        return PART_CODE;
    }

    public void setPART_CODE(String PART_CODE) {
        this.PART_CODE = PART_CODE;
    }

    public String getPART_NAME() {
        return PART_NAME;
    }

    public void setPART_NAME(String PART_NAME) {
        this.PART_NAME = PART_NAME;
    }

    public String getLOCATION_DESCRIB() {
        return LOCATION_DESCRIB;
    }

    public void setLOCATION_DESCRIB(String LOCATION_DESCRIB) {
        this.LOCATION_DESCRIB = LOCATION_DESCRIB;
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

    public String getCONTROL_CODE() {
        return CONTROL_CODE;
    }

    public void setCONTROL_CODE(String CONTROL_CODE) {
        this.CONTROL_CODE = CONTROL_CODE;
    }

    public String getONETIME_CODE() {
        return ONETIME_CODE;
    }

    public void setONETIME_CODE(String ONETIME_CODE) {
        this.ONETIME_CODE = ONETIME_CODE;
    }

    public String getLOOP_CODE() {
        return LOOP_CODE;
    }

    public void setLOOP_CODE(String LOOP_CODE) {
        this.LOOP_CODE = LOOP_CODE;
    }

    @Override
    public String toString() {
        return "FiBean{" +
                "Index='" + Index + '\'' +
                ", DeviceID='" + DeviceID + '\'' +
                ", SendTime='" + SendTime + '\'' +
                ", EndTime='" + EndTime + '\'' +
                ", DeviceValue='" + DeviceValue + '\'' +
                ", Quality='" + Quality + '\'' +
                ", Description='" + Description + '\'' +
                ", PROJECT_ID='" + PROJECT_ID + '\'' +
                ", PART_CODE='" + PART_CODE + '\'' +
                ", PART_NAME='" + PART_NAME + '\'' +
                ", LOCATION_DESCRIB='" + LOCATION_DESCRIB + '\'' +
                ", HORIZONTAL_COORDINATE='" + HORIZONTAL_COORDINATE + '\'' +
                ", VERTICAL_COORDINATE='" + VERTICAL_COORDINATE + '\'' +
                ", CONTROL_CODE='" + CONTROL_CODE + '\'' +
                ", ONETIME_CODE='" + ONETIME_CODE + '\'' +
                ", LOOP_CODE='" + LOOP_CODE + '\'' +
                '}';
    }
}
