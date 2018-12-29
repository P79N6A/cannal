package com.alibaba.otter.canal.example.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.example.ClientSample;
import com.alibaba.otter.canal.example.bean.FiBean;
import com.alibaba.otter.canal.example.util.IdGenerator;
import com.alibaba.otter.canal.example.util.JDBCUtil;
import com.alibaba.otter.canal.example.util.JedisUtil;
import com.alibaba.otter.canal.example.util.JsonUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.alibaba.otter.canal.example.util.DBUtil.getConnection;

public class FiAction {

    public FiAction() {
    }

    /**
     * 处理消防数据监控逻辑
     * @param tableName
     * @param columnsLlist
     */
    public static void disposeFi(String tableName, List<CanalEntry.Column> columnsLlist) {

        //调用定时器的时间差计算
        Long start = 0l;
        Long end = 0l;

        //更新当日表并保存到redis
        try {
            //获取Javabean
            FiBean fiBean = ClientSample.getBean(tableName, columnsLlist);
            String deviceID = fiBean.getDeviceID();
            String fiBeanStr = JedisUtil.get(deviceID + "_FI_RECORD");
            Map<String, Object> fiBeanMap = JsonUtil.jsonToMap(fiBeanStr);
            if(fiBeanMap == null || fiBeanMap.size() < 1){
                String sql = "select e.PROJECT_ID , e.PART_CODE , p.PART_NAME , e.LOCATION_DESCRIB , e.HORIZONTAL_COORDINATE , " +
                        "e.VERTICAL_COORDINATE from FI_EQUIP e , FI_PART p where e.PART_CODE = p.ID and e.EQ_NAME = '" +deviceID+ "'";
                List query = JDBCUtil.query(sql, new Object[]{}, null);
                if(query != null && query.size() > 0){
                    fiBeanMap = (Map<String, Object>) query.get(0);
                }
            }
            ////赋值
            String PROJECT_ID = (String) fiBeanMap.get("PROJECT_ID");                                                            //工程编码
            String PART_CODE = (String) fiBeanMap.get("PART_CODE");                                                            //分区编码
            String proPartKey = PROJECT_ID +"_"+ PART_CODE;
            if(fiBean.getQuality().equals("OPC_QUALITY_NOT_CONNECTED")){
                fiBean.setDeviceValue("-1");
            }
            //查询异常数量分布
            String AMOUNT_DISTRIBUTE = JedisUtil.get("FI_" + proPartKey + "_AMOUNT_DISTRIBUTE");//振动设备（第二节点的名称）数量分布
            Map amountDistributeMap = getFiAmountDistributeMap(AMOUNT_DISTRIBUTE , (String)fiBeanMap.get("DeviceValue") , fiBean.getDeviceValue());
            String toJson = JSONObject.toJSONString(amountDistributeMap);
            JedisUtil.setString("FI"+proPartKey+"_AMOUNT_DISTRIBUTE" , toJson);

            String deviceValue = fiBean.getDeviceValue();
            String quality = fiBean.getQuality();
            //异常结束时间
            //保存实时报警数据到报警记录表
            if(quality.equals("OPC_QUALITY_NOT_CONNECTED") || deviceValue.equals("1") || deviceValue.equals("4")){
                batchInsertFiWarning(quality , fiBean , fiBeanMap);


                //***********************************************************************************************
                //根据条件（设备）查询推送设置，获取该设备的推送方式
                Object[] parms = new Object[]{ fiBean.getDeviceValue() , fiBean.getDeviceID() , PROJECT_ID};
                Map sendSettingMap = (Map)ClientSample.selectSendSetting(tableName , parms , quality);

                //获取推送信息的数据 (火警时查询应急预案)

                Map sendData = new LinkedHashMap();
                sendData.put("equip" , fiBeanMap.get("PART_NAME") ); //设备
                sendData.put("location" , fiBeanMap.get("LOCATION_DESCRIB") ); //位置
                sendData.put("code" , deviceID); //设备
                sendData.put("time" , fiBean.getSendTime() ); //时间
                String event = "";
                if(quality.equals("OPC_QUALITY_NOT_CONNECTED")){
                    event = "33";
                }else {
                    if(deviceValue .equals("1")){
                        event = "1";
                    }else if(deviceValue .equals("4")){
                        event = "4";
                    }
                }
                sendData.put("event" , event ); //状态
                if("1".equals(event)){
                    String PLAN_CODE = (String)sendSettingMap.get("PLAN_CODE");
                    String sql = "select PLAN_DESCRIBE from CONTINGENCY_PLAN where PLAN_CODE = '"+PLAN_CODE+"'";
                    List query = JDBCUtil.query(sql, null, null);
                    if(query != null && query.size() > 0){
                        Map planMap = (Map)query.get(0);
                        String plan = (String)planMap.get("PLAN_DESCRIBE");
                        sendData.put("plan" , plan);
                    }
                }

                //向sendData中保存redis的key  用于延时发送消息时判断状态是否改变，状态改变将不会再发送消息
                sendData.put("key" , fiBean.getDeviceID() +"_Fi_RECORD");
                sendData.put("tableName" , tableName);
                //调用接口进行数据推送
                boolean flag = ClientSample.send(sendSettingMap , sendData);

                //***********************************************************************************************
            }
            //赋值
            fiBeanMap.put("Index" , fiBean.getIndex());
            fiBeanMap.put("DeviceID" , fiBean.getDeviceID());
            fiBeanMap.put("SendTime" , fiBean.getSendTime());
            fiBeanMap.put("EndTime" , fiBean.getEndTime());
            fiBeanMap.put("DeviceValue" , deviceValue );
            fiBeanMap.put("Quality" , fiBean.getQuality());
            fiBeanMap.put("Description" , fiBean.getDescription());
            //保存到redis
            String key = fiBean.getDeviceID() +"_Fi_RECORD";
            String jsonString = JSON.toJSONString(fiBeanMap);
            ClientSample.redisInsert(key, jsonString);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 报警数据添加到报警记录表中
     * @param fiBean
     * @param fiBeanMap
     */
    private static void batchInsertFiWarning(String quality , FiBean fiBean  , Map<String, Object> fiBeanMap) {
        StringBuilder sb = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `qdqg`.`FI_EXCEPTION` (`ID`, `PROJECT_ID`, `EXCETION_TIME`, `CONTROL_CODE`, `LOCATION`, " +
                    "`EQ_CODE`, `EQ_NAME`, `ONETIME_CODE`, `LOOP_CODE`, `PART_CODE`, `EQ_STATE`, `CREATE_USER`, `CREATE_TIME`," +
                    " `MODIFY_USER`, `MODIFY_TIME`, `AUD_USER`, `AUD_FLAG`, `AUD_TIME`, `END_TIME`, `EVENT_TIME` ) " +
                    "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?  )";
            conn = getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currTime = fiBean.getSendTime();
            //String currTime = sdf.format(new Date());
            //判断上条数据的状态，并且根据状态回写结束时间和事件时长
            String deviceValue = fiBean.getDeviceValue();
            if(fiBeanMap != null && fiBeanMap.size() > 0){
                String DeviceValue = (String) fiBeanMap.get("DeviceValue");
                //判断开始时间、结束时间
                //查询当前编码对应的上一条记录的状态
                //当前监控到的数据状态和改测点的前一状态不同
                if (quality.equals("OPC_QUALITY_NOT_CONNECTED")  || !deviceValue.equals(DeviceValue)){
                    //监测到的数据状态为正常  如果正常，表示该测点的报警结束，记录结束时间，计算事件时长
                    //从报警记录表中获取该测点的最新的记录
                    String getProLevelSql = "select ID as ID, EXCETION_TIME as EXCETION_TIME  from FI_EXCEPTION where EQ_CODE = '"
                            +fiBean.getDeviceID()+ "' order by EXCETION_TIME desc";
                    List query = JDBCUtil.query(getProLevelSql, new Object[]{}, null);
                    if(query != null && query.size() > 0) {
                        Map proLevelMap = (Map) query.get(0);
                        String proID = (String) proLevelMap.get("ID");
                        String EXCETION_TIME = (String) proLevelMap.get("EXCETION_TIME");
                        String EVENT_TIME = (String) proLevelMap.get("EVENT_TIME");
                        if(StringUtils.isBlank(EVENT_TIME)){
                            //获取事件时长  单位/分钟
                            Double eventTime = ClientSample.getEventTime(currTime , EXCETION_TIME);
                            String time = eventTime.toString();

                            //将事件结束时间以及时间时长回写到数据库
                            String updateSql = "update FI_EXCEPTION set EVENT_TIME = '"+time+"', END_TIME = '" +currTime+ "' where ID = '" + proID + "' ";
                            JDBCUtil.edit(updateSql , null);
                            sb.append("update before |");
                        }
                    }
                    //监测到的数据状态非正常
                    if ("1".equals(deviceValue) || "4".equals(deviceValue) || quality.equals("OPC_QUALITY_NOT_CONNECTED")  ){
                        pstmt.setString(1, IdGenerator.getId());
                        pstmt.setString(2,(String)fiBeanMap.get("PROJECT_ID"));
                        pstmt.setString(3, fiBean.getSendTime());
                        pstmt.setString(4, (String)fiBeanMap.get("CONTROL_CODE"));
                        pstmt.setString(5, (String)fiBeanMap.get("LOCATION_DESCRIB"));
                        pstmt.setString(6, fiBean.getDeviceID());
                        pstmt.setString(7, (String)fiBeanMap.get("EQ_NAME"));
                        pstmt.setString(8, (String)fiBeanMap.get("ONETIME_CODE"));
                        pstmt.setString(9, (String)fiBeanMap.get("LOOP_CODE"));
                        pstmt.setString(10, (String)fiBeanMap.get("PART_CODE"));
                        pstmt.setString(11, fiBean.getDeviceValue());
                        pstmt.setString(12, null);
                        pstmt.setString(13, null);
                        pstmt.setString(14, null);
                        pstmt.setString(15, null);
                        pstmt.setString(16, null);
                        pstmt.setString(17, null);
                        pstmt.setString(18, null);
                        pstmt.setString(19, null);
                        pstmt.setString(20, null);
                        pstmt.addBatch();
                        sb.append("insert ...|");
                    }
                }
            }
            //logger.info(sb.toString());
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            JDBCUtil.close(conn, pstmt, null);

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

    }

    private static Map getFiAmountDistributeMap(String amount_distribute, String oldValue, String newValue) {
        Map<String, Object> amountDistributeMap = JsonUtil.jsonToMap(amount_distribute);
        int NORMAL = (Integer) amountDistributeMap.get("NORMAL");                       //正常
        int FIREALARM = (Integer) amountDistributeMap.get("FIREALARM");                //火警
        int BREAKDOWN = (Integer) amountDistributeMap.get("BREAKDOWN");          //故障
        int DEVSTART = (Integer) amountDistributeMap.get("DEVSTART");                    //启动
        int DEVSTOP = (Integer) amountDistributeMap.get("DEVSTOP");                      //停止
        int CTLERASURE = (Integer) amountDistributeMap.get("CTLERASURE");            //控制器消音
        int CTLRESTORATION = (Integer) amountDistributeMap.get("CTLRESTORATION");        //控制器复位
        int CONNECTSTATE = (Integer) amountDistributeMap.get("CONNECTSTATE");        //连接状态
        //if(!newValue.equals("5") && !newValue.equals("6") &&!newValue.equals("9") && !newValue.equals("11")  ){
        if(!StringUtils.isBlank(oldValue)){
            if(!newValue.equals(oldValue)){
                if(oldValue.equals("0")){
                    NORMAL = NORMAL - 1;
                    amountDistributeMap.put("NORMAL",NORMAL);
                }else if(oldValue.equals("1")){
                    FIREALARM = FIREALARM - 1;
                    amountDistributeMap.put("FIREALARM",FIREALARM);
                }else if(oldValue.equals("4")){
                    BREAKDOWN = BREAKDOWN - 1;
                    amountDistributeMap.put("BREAKDOWN",BREAKDOWN);
                }else if(oldValue.equals("5")){
                    DEVSTART = DEVSTART - 1;
                    amountDistributeMap.put("DEVSTART",DEVSTART);
                }else if(oldValue.equals("6")){
                    DEVSTOP = DEVSTOP - 1;
                    amountDistributeMap.put("DEVSTOP",DEVSTOP);
                }else if(oldValue.equals("9")){
                    CTLERASURE = CTLERASURE - 1;
                    amountDistributeMap.put("CTLERASURE",CTLERASURE);
                }else if(oldValue.equals("11")){
                    CTLRESTORATION = CTLRESTORATION - 1;
                    amountDistributeMap.put("CTLRESTORATION",CTLRESTORATION);
                }else if(oldValue.equals("-1")){
                    CONNECTSTATE = CONNECTSTATE - 1;
                    amountDistributeMap.put("CONNECTSTATE",CONNECTSTATE);
                }

                if(newValue.equals("0")){
                    NORMAL = NORMAL + 1;
                    amountDistributeMap.put("NORMAL",NORMAL);
                }else if(newValue.equals("1")){
                    FIREALARM = FIREALARM + 1;
                    amountDistributeMap.put("FIREALARM",FIREALARM);
                }else if(newValue.equals("4")){
                    BREAKDOWN = BREAKDOWN + 1;
                    amountDistributeMap.put("BREAKDOWN",BREAKDOWN);
                }else if(newValue.equals("5")){
                    DEVSTART = DEVSTART + 1;
                    amountDistributeMap.put("DEVSTART",DEVSTART);
                }else if(newValue.equals("6")){
                    DEVSTOP = DEVSTOP + 1;
                    amountDistributeMap.put("DEVSTOP",DEVSTOP);
                }else if(newValue.equals("9")){
                    CTLERASURE = CTLERASURE + 1;
                    amountDistributeMap.put("CTLERASURE",CTLERASURE);
                }else if(newValue.equals("1")){
                    CTLRESTORATION = CTLRESTORATION + 1;
                    amountDistributeMap.put("CTLRESTORATION",CTLRESTORATION);
                }else if(newValue.equals("-1")){
                    CONNECTSTATE = CONNECTSTATE + 1;
                    amountDistributeMap.put("CONNECTSTATE",CONNECTSTATE);
                }
            }
        }else{
            if(newValue.equals("0")){
                NORMAL = NORMAL + 1;
                amountDistributeMap.put("NORMAL",NORMAL);
            }else if(newValue.equals("1")){
                FIREALARM = FIREALARM + 1;
                amountDistributeMap.put("FIREALARM",FIREALARM);
            }else if(newValue.equals("4")){
                BREAKDOWN = BREAKDOWN + 1;
                amountDistributeMap.put("BREAKDOWN",BREAKDOWN);
            }else if(newValue.equals("5")){
                DEVSTART = DEVSTART + 1;
                amountDistributeMap.put("DEVSTART",DEVSTART);
            }else if(newValue.equals("6")){
                DEVSTOP = DEVSTOP + 1;
                amountDistributeMap.put("DEVSTOP",DEVSTOP);
            }else if(newValue.equals("9")){
                CTLERASURE = CTLERASURE + 1;
                amountDistributeMap.put("CTLERASURE",CTLERASURE);
            }else if(newValue.equals("1")){
                CTLRESTORATION = CTLRESTORATION + 1;
                amountDistributeMap.put("CTLRESTORATION",CTLRESTORATION);
            }else if(newValue.equals("-1")){
                CONNECTSTATE = CONNECTSTATE + 1;
                amountDistributeMap.put("CONNECTSTATE",CONNECTSTATE);
            }
        }


        //}

        return amountDistributeMap;
    }

}
