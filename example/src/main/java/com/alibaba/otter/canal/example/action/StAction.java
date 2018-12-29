package com.alibaba.otter.canal.example.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.example.ClientSample;
import com.alibaba.otter.canal.example.bean.StBean;
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
import java.util.List;
import java.util.Map;

import static com.alibaba.otter.canal.example.util.DBUtil.getConnection;

public class StAction {
    public StAction() {
    }

    /**
     * 处理应力数据监控逻辑
     * @param tableName
     * @param columnsLlist
     * @throws SQLException
     */
    public static void disposeST(String tableName, List<CanalEntry.Column> columnsLlist) throws SQLException {
        //获取推送数据的
        String EQ_CODE = "";                                   //监控设备编码
        String EQ_NAME = "";                                  //监控设备名称
        String SENDTIME = "";                                 //数据推送时间
        for (CanalEntry.Column column : columnsLlist) {
            String columnName = column.getName();
            if(columnName.equalsIgnoreCase("EQ_CODE")){
                EQ_CODE = column.getValue();
                String sql = "select EQ_CODE from ST_EQUIP where ID = '"+EQ_CODE+"'";
                List list = JDBCUtil.query(sql, null, null);
                Map eqMap = (Map)list.get(0);
                EQ_CODE = (String)eqMap.get("EQ_CODE");
                System.out.println(EQ_CODE);
            }
            if(columnName.equalsIgnoreCase("EQ_NAME")){
                EQ_NAME = column.getValue();
            }
            if(columnName.equalsIgnoreCase("SENDTIME")){
                SENDTIME = column.getValue();
            }
        }

        //获取缓存中的当前设备下所有测点的map
        //Map<"100001_门机上部运行部分",Map<"1",stBeanJson>>
        String EQ_EQUIP_CODE = EQ_CODE;         //实体设备编码
        String key = EQ_EQUIP_CODE + "_" + EQ_NAME;
        //处理应力数据
        disposeSTwarning(columnsLlist ,key , SENDTIME);
    }

    private static void disposeSTwarning(List<CanalEntry.Column> columnsLlist, String key , String SENDTIME) {
        Map<String, Object> eqAllPointMap = null;

        String eqAllPointMapStr = JedisUtil.get(key);
        if(!StringUtils.isBlank(eqAllPointMapStr)){
            eqAllPointMap = JsonUtil.jsonToMap(eqAllPointMapStr);
        }
        //遍历  //状态数量 //报警记录表 //redis
        for (CanalEntry.Column column : columnsLlist) {
            StringBuilder sb = new StringBuilder();
            String columnName = column.getName();
            String columnValue = column.getValue();
            //温度暂不处理
            //if(columnName.contains("temper")){
            //    stBean.setTEMPER(Double.valueOf(columnValue));
            //    continue;
            //}
            //应力值保存
            if(!StringUtils.isBlank(columnName) && !StringUtils.isBlank(columnValue) && columnName.contains("STRESS")){
                sb.append(columnName).append(":").append(columnValue).append("|");
                //应力值保存
                String pointCode = columnName.substring(6, columnName.length());
                JSONObject stBeanJson =  (JSONObject)eqAllPointMap.get(pointCode);
                if(stBeanJson != null){
                    // stBean 通过redis获取的上一条记录的bean对象
                    //StBean stBean = (StBean) JsonUtil.jsonToBean(stBeanJson, StBean.class);
                    StBean stBean = JSON.parseObject(stBeanJson.toJSONString(), StBean.class);
                    //上一条记录的报警级别和状态
                    Double warning_value = stBean.getWARNING_VALUE();   //预警阈值
                    Double alarm_value = stBean.getALARM_VALUE();            //报警阈值
                    Double danger_value = stBean.getDANGER_VALUE();       //危险阈值
                    String beforeState = stBean.getSTATE();                                   //上一条记录的应力状态
                    //状态数量 //报警记录表 //redis

                    //新记录的应力值，该应力值对应的状态和报警级别
                    Double stress = Double.valueOf(columnValue);                                    //当前记录的应力值
                    String nowSta = "";
                    String nowLevel = "";
                    if (stress >= 0d && stress < warning_value) {
                        nowSta = "1";
                    }else if (stress >= warning_value && stress < alarm_value) {
                        nowSta = "2";
                        nowLevel = "1";
                    } else if (stress >= alarm_value && stress < danger_value) {
                        nowSta = "3";
                        nowLevel = "2";

                    } else if (stress >= danger_value) {
                        nowSta = "4";
                        nowLevel = "3";
                    }
                    sb.append("nowState:").append(nowSta).append("|").append("beforeState:").append(beforeState).append("|");
                    //更新状态数量分布
                    //String key = EQ_EQUIP_CODE + "_" + EQ_NAME;  //100001_门机下部运行部分
                    String AMOUNT_DISTRIBUTE = JedisUtil.get("ST_" + key + "_AMOUNT_DISTRIBUTE");//应力设备状态数量分布
                    Map amountDistributeMap = VbAction.getVbAmountDistributeMap(AMOUNT_DISTRIBUTE ,beforeState , nowSta);
                    String toJson = JSON.toJSONString(amountDistributeMap);
                    JedisUtil.setString("VB_" + key + "_AMOUNT_DISTRIBUTE" , toJson);

                    //保存实时报警数据到报警记录表
                    stBean.setSTATE(nowSta);
                    stBean.setLEVEL(nowLevel);
                    sb.append("pointCode:").append(pointCode).append("|").append("stress:").append(stress).append("|").append("state:").append(nowSta).append("|");
                    stBean.setSTRESS(Double.valueOf(columnValue));
                    stBean.setSENDTIME(SENDTIME);
                    stBean = batchInsertStWarning(stBean , eqAllPointMap , pointCode , sb);
                    //将bean类保存到map中
                    eqAllPointMap.put(pointCode , stBean);
                }
            }
            //包含当前实体设备下所有测点的 map 保存到redis中
            String s = JedisUtil.setString(key, JSON.toJSONString(eqAllPointMap));
            //System.out.println(s);
            //logger.info(sb.toString() +s);

        }
    }

    /**
     * 添加数据到振动报警记录表
     * @param stBean
     */
    private static StBean batchInsertStWarning(StBean stBean , Map eqAllPointMap , String pointCode ,StringBuilder sb) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `qdqg`.`ST_WARNING` (`ID`, `EQ_ID`, `EQ_NAME`, `POINT_CODE`, `POINT_POSITION`, " +
                    "`ALERM_START`, `ALERM_END`, `ALERM_LEVEL`, `REMARK`, `CREATE_USER`, `CREATE_TIME`, `MODIFY_USER`, " +
                    "`MODIFY_TIME`, `AUD_USER`, `AUD_FLAG`, `AUD_TIME`,  `ST_VALUE`) " +
                    "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?  )";
            conn = getConnection();
            conn.setAutoCommit(false);
            //String sql = "update VB_WARNING set ITEMCODE = ? , MONI_STATE = ? where ID = ?";
            pstmt = conn.prepareStatement(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currTime = stBean.getSENDTIME();
            //String currTime = sdf.format(new Date());
            String nowState = stBean.getSTATE();
            //判断开始时间、结束时间
            //查询当前编码对应的上一条记录的状态
            JSONObject stBeanJson =  (JSONObject)eqAllPointMap.get(pointCode);
            // stBean 通过redis获取的上一条记录的bean对象
            //StBean stBean = (StBean) JsonUtil.jsonToBean(stBeanJson, StBean.class);
            StBean oldStBean = JSON.parseObject(stBeanJson.toJSONString(), StBean.class);
            String oldStBeanSTATE = oldStBean.getSTATE();
            //当前监控到的数据状态和改测点的前一状态不同
            if (!nowState.equals(oldStBeanSTATE)){

                //监测到的数据状态为正常  如果正常，表示该测点的报警结束，记录结束时间，计算事件时长
                //从报警记录表中获取该测点的最新的记录
                String getProLevelSql = "select ID as ID , ALERM_START as ALERM_START from ST_WARNING where POINT_CODE = '"
                        +pointCode+ "' order by ALERM_START desc";
                List query = JDBCUtil.query(getProLevelSql, new Object[]{}, null);
                if(query != null && query.size() > 0) {
                    Map proLevelMap = (Map) query.get(0);
                    String proID = (String) proLevelMap.get("ID");
                    String ALERM_START = (String) proLevelMap.get("ALERM_START");
                    //获取事件时长  单位/分钟
                    Double eventTime = ClientSample.getEventTime(currTime , ALERM_START);
                    String time = eventTime.toString();
                    //将事件结束时间以及时间时长回写到数据库
                    String updateSql = "update ST_WARNING set EVENT_TIME = '"+time+"', ALERM_END = '" +currTime+ "' where ID = '" + proID + "' ";
                    JDBCUtil.edit(updateSql , null);
                    sb.append("update before |");
                }
                //监测到的数据状态非正常
                if (!"1".equals(nowState)){
                    String warntype = ClientSample.getWarnType(nowState);
                    stBean.setLEVEL(warntype);
                    pstmt.setString(1, IdGenerator.getId());
                    pstmt.setString(2,stBean.getEQ_CODE());
                    pstmt.setString(3, stBean.getEQ_NAME());
                    pstmt.setString(4, pointCode);
                    pstmt.setString(5, stBean.getPOINT_POSITION());
                    pstmt.setString(6, stBean.getSENDTIME());
                    pstmt.setString(7, null);
                    pstmt.setString(8, stBean.getLEVEL());
                    pstmt.setString(9, null);
                    pstmt.setString(10, null);
                    pstmt.setString(11, null);
                    pstmt.setString(12, null);
                    pstmt.setString(13, null);
                    pstmt.setString(14, null);
                    pstmt.setString(15, null);
                    pstmt.setString(16, null);
                    pstmt.setDouble(17, stBean.getSTRESS());
                    pstmt.addBatch();
                    sb.append("insert ...|");
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
        return stBean;
    }
}
