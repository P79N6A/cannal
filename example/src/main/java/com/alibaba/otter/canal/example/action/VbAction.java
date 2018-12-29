package com.alibaba.otter.canal.example.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.example.ClientSample;
import com.alibaba.otter.canal.example.bean.Record;
import com.alibaba.otter.canal.example.util.IdGenerator;
import com.alibaba.otter.canal.example.util.JDBCUtil;
import com.alibaba.otter.canal.example.util.JedisUtil;
import com.alibaba.otter.canal.example.util.JsonUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static com.alibaba.otter.canal.example.util.DBUtil.getConnection;

public class VbAction {

    public VbAction() {
    }

    /**
     * 处理振动数据监控逻辑
     * @param tableName
     * @param columnsLlist
     */
    public static void disposeVb(String tableName, List<CanalEntry.Column> columnsLlist) {
        //更新当日表并保存到redis
        try {
            //获取Javabean
            Record record = ClientSample.getBean(tableName, columnsLlist);

            String item_id = record.getITEM_ID();
            String[] split = item_id.split("\\.");
            String secondNodeName = split[1];
            double moni_value = record.getMONI_VALUE();

            //从缓存中获取振动数据的标准    青岛港卸船机.1#卸船机XC.起升差动减速箱小车驱动输出端.Radial [Tach].速度 频谱 1000 Hz_VB_RECORD
            String key =  item_id + "_VB_RECORD";
            String string = JedisUtil.get(key);
            if(!StringUtils.isBlank(string)){

                Map<String, Object> vbStandMap = JsonUtil.jsonToMap(string);
                Double WARNING_VALUE =  ((BigDecimal)vbStandMap.get("WARNING_VALUE")).doubleValue();
                Double ALARM_VALUE =  ((BigDecimal)vbStandMap.get("ALARM_VALUE")).doubleValue();
                Double DANGER_VALUE =  ((BigDecimal)vbStandMap.get("DANGER_VALUE")).doubleValue();
                //获取到标准之后，判断当前记录的状态
                String MONI_STATE = "";
                if (moni_value >= WARNING_VALUE && moni_value < ALARM_VALUE) {
                    MONI_STATE = "2";
                } else if (moni_value >= ALARM_VALUE && moni_value < DANGER_VALUE) {
                    MONI_STATE = "3";
                } else if (moni_value >= DANGER_VALUE) {
                    MONI_STATE = "4";
                } else if (moni_value >= 0d && moni_value < WARNING_VALUE) {
                    MONI_STATE = "1";
                }

                //查询振动当日表中当前 ITEM_ID 对应的最新数据的振动状态，
                // 如果振动状态相同，不改变该设备的数量分布字符串 ，
                //如果振动状态不同，+1-1，重新生成设备下测量点状态数量的分布字符串
                String beforeState = (String) vbStandMap.get("MONI_STATE");
                String AMOUNT_DISTRIBUTE = JedisUtil.get("VB_" + secondNodeName + "_AMOUNT_DISTRIBUTE");//振动设备（第二节点的名称）数量分布
                Map amountDistributeMap = getVbAmountDistributeMap(AMOUNT_DISTRIBUTE ,beforeState , MONI_STATE);
                String toJson = JSONObject.toJSONString(amountDistributeMap);
                JedisUtil.setString("VB_" + secondNodeName + "_AMOUNT_DISTRIBUTE" , toJson);

                //保存实时报警数据到报警记录表
                record.setMONI_STATE(MONI_STATE);
                record.setITEMCODE((String)vbStandMap.get("ITEMCODE"));
                record = batchInsertVbWarning(record);

                //异常状态情况下获取推送设置，需要传递的数据，通过推送设置来发送数据
                if(!"1".equals(MONI_STATE)){

                }
                //保存到redis
                if(MONI_STATE.equalsIgnoreCase("1")){
                    record.setMONI_TIME("");
                }
                vbStandMap.put("MONI_VALUE", record.getMONI_VALUE());
                vbStandMap.put("MONI_STATE", record.getMONI_STATE());
                vbStandMap.put("LEVEL", record.getLEVEL());
                String jsonString = JSON.toJSONString(vbStandMap);
                ClientSample.redisInsert(key, jsonString);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备状态数量分布
     * @param AMOUNT_DISTRIBUTE
     * @param before_state
     * @param now_state
     * @return
     */
    public static Map getVbAmountDistributeMap(String AMOUNT_DISTRIBUTE , String before_state , String now_state) {
        Map<String, Object> amountDistributeMap = JsonUtil.jsonToMap(AMOUNT_DISTRIBUTE);
        int NORMAL = (Integer) amountDistributeMap.get("NORMAL");           //正常
        int WARNING = (Integer) amountDistributeMap.get("WARNING");       //预警
        int ALARM = (Integer) amountDistributeMap.get("ALARM");                 //报警
        int DANGER = (Integer) amountDistributeMap.get("DANGER");            //危险

        if(!before_state.equals(now_state)){
            //比较当前 ITEM_ID 对应的上一次记录的状态与新记录的状态
            if(before_state.equals("1")){
                NORMAL = NORMAL - 1;
                amountDistributeMap.put("NORMAL",NORMAL);
            }else if(before_state.equals("2")){
                WARNING = WARNING - 1;
                amountDistributeMap.put("WARNING",WARNING);
            }else if(before_state.equals("3")){
                ALARM = ALARM - 1;
                amountDistributeMap.put("ALARM",ALARM);
            }else if(before_state.equals("4")){
                DANGER = DANGER - 1;
                amountDistributeMap.put("DANGER",DANGER);
            }
            if(now_state.equals("1")){
                NORMAL = NORMAL + 1;
                amountDistributeMap.put("NORMAL",NORMAL);
            }else if(now_state.equals("2")){
                WARNING = WARNING + 1;
                amountDistributeMap.put("WARNING",WARNING);
            }else if(now_state.equals("3")){
                ALARM = ALARM + 1;
                amountDistributeMap.put("ALARM",ALARM);
            }else if(now_state.equals("4")){
                DANGER = DANGER + 1;
                amountDistributeMap.put("DANGER",DANGER);
            }
        }
        return amountDistributeMap;
    }

    /**
     * 添加数据到振动报警记录表
     * @param record
     */
    private static Record batchInsertVbWarning(Record record) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `qdqg`.`VB_WARNING`" +
                    " (`ID`, `ITEMCODE`, `ITEM_ID`, `MONI_TIME` ,`END_TIME` , `MONI_VALUE`, `WARN_TYPE`, " +
                    "`REFER_VALUE`, `REMARK`, `CREATE_USER`, `CREATE_TIME`, `MODIFY_USER`, " +
                    "`MODIFY_TIME`, `AUD_USER`, `AUD_FLAG`, `AUD_TIME` ,`EVENT_TIME`) " +
                    "VALUES  (?, ? , ?, ? , ? ,? , ? , ?, ?, ?, ?, ?, ?, ?, ? , ? , ?) ";
            conn = getConnection();
            conn.setAutoCommit(false);
            //String sql = "update VB_WARNING set ITEMCODE = ? , MONI_STATE = ? where ID = ?";
            pstmt = conn.prepareStatement(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currTime = record.getMONI_TIME();
            //String currTime = sdf.format(new Date());
            String moni_state = record.getMONI_STATE();
            //判断开始时间、结束时间
            //查询当前编码对应的上一条记录的状态
            String recordStr = JedisUtil.get(record.getITEM_ID() + "_VB_RECORD");
            Map<String, Object> recordMap = JsonUtil.jsonToMap(recordStr);
            String MONI_STATE = (String) recordMap.get("MONI_STATE");
            //当前监控到的数据状态和该测点的前一状态不同
            if (!record.getMONI_STATE().equals(MONI_STATE)){

                //监测到的数据状态为正常  如果正常，表示该测点的报警结束，记录结束时间，计算事件时长
                //从报警记录表中获取该测点的最新的记录
                String getProLevelSql = "select WARN_TYPE as WARN_TYPE , ID as ID , MONI_TIME as MONI_TIME from VB_WARNING where ITEM_ID = '"
                        +record.getITEM_ID()+ "' order by ID desc";
                List query = JDBCUtil.query(getProLevelSql, new Object[]{}, null);
                if(query != null && query.size() > 0) {
                    Map proLevelMap = (Map) query.get(0);
                    String proID = (String) proLevelMap.get("ID");
                    String proMONI_TIME = (String) proLevelMap.get("MONI_TIME");
                    //获取事件时长  单位/分钟
                    Double eventTime = ClientSample.getEventTime(currTime , proMONI_TIME);
                    String time = eventTime.toString();
                    //将事件结束时间以及时间时长回写到数据库
                    String updateSql = "update VB_WARNING set EVENT_TIME = '"+time+"', END_TIME = '" +currTime+ "' where ID = '" + proID + "' ";
                    JDBCUtil.edit(updateSql , null);
                }
                //监测到的数据状态非正常
                if (!"1".equals(record.getMONI_STATE())){
                    String warntype = ClientSample.getWarnType(record.getMONI_STATE());
                    record.setLEVEL(warntype);
                    pstmt.setString(1, IdGenerator.getId());
                    pstmt.setString(2, record.getITEMCODE());
                    pstmt.setString(3, record.getITEM_ID());
                    pstmt.setString(4, record.getMONI_TIME());
                    pstmt.setString(5, null);
                    pstmt.setDouble(6, record.getMONI_VALUE());
                    pstmt.setString(7, warntype);
                    pstmt.setString(8, null);
                    pstmt.setString(9, record.getREMARK());
                    pstmt.setString(10, null);
                    pstmt.setString(11, null);
                    pstmt.setString(12, null);
                    pstmt.setString(13, null);
                    pstmt.setString(14, null);
                    pstmt.setString(15, null);
                    pstmt.setString(16, null);
                    pstmt.setString(17, null);
                    pstmt.addBatch();
                }
            }
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
        return record;
    }

}
