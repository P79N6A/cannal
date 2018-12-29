package com.alibaba.otter.canal.example;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.example.action.FiAction;
import com.alibaba.otter.canal.example.action.StAction;
import com.alibaba.otter.canal.example.action.VbAction;
import com.alibaba.otter.canal.example.bean.FiBean;
import com.alibaba.otter.canal.example.bean.Record;
import com.alibaba.otter.canal.example.util.*;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.alibaba.otter.canal.example.util.DBUtil.getConnection;

public class ClientSample {
    protected final static Logger logger = LoggerFactory.getLogger(AbstractCanalClientTest.class);

    /**
     * 入口
     * @param args
     */
    public static void main(String args[]) {

        //获取需要批量更新的bean对象集合
        List<Record> recordList = new ArrayList<Record>();
        //List<Record> recordList = new ArrayList<Record>();
        //List<Record> recordList = new ArrayList<Record>();
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        int batchSize = 1024 ;
        //Connection conn = DBUtil.getConnection();
        try {
            connector.connect();
            connector.subscribe("qdqg\\..*");
            connector.rollback();
            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        synchronized (ClientSample.class) {
                            //获取数据库操作类型、表名称、记录数据
                            List list = printEntry(message.getEntries());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String format = sdf.format(new Date());
                            System.out.println("Time == " + format);
                            //执行到mysql
                            excuteToMysql(list);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

        } finally {
            connector.disconnect();
            
        }
    }



    /**
     * 修改振动当日表数据振动状态
     * @param recordList
     */
    private static void batchUpdateVbToday(List<Record> recordList) {
        Logger logger = LoggerFactory.getLogger(ClientSample.class);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            String sql = "update VB_RECORD_TODAY set ITEMCODE = ? , MONI_STATE = ? where ID = ?";
            pstmt = conn.prepareStatement(sql);

            if (recordList != null && recordList.size() > 0) {
                for (Record r : recordList) {
                    pstmt.setString(1, r.getITEMCODE());
                    pstmt.setString(2, r.getMONI_STATE());
                    pstmt.setString(3, r.getID());
                    pstmt.addBatch();
                    //pstmt.clearParameters();
                }
            }
            int[] batch = pstmt.executeBatch();
            pstmt.clearBatch();
            conn.commit();
            conn.setAutoCommit(true);
            //logger.info(String.format("batch update vb_record_today commit"));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                JDBCUtil.close(conn, pstmt, null);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 判断获取的数据库binlog日志数据，并对数据库中的数据进行增删改
     *
     * @param list
     * @throws SQLException
     */
    public static void excuteToMysql(List list) throws SQLException {
        if (list != null && list.size() > 0) {
            for (Object object : list) {
                Map map = (Map) object;
                EventType eventType = (EventType) map.get("eventType");
                String tableName = (String) map.get("tableName");
                List<Column> columnsLlist = (List) map.get("columnsList");
                //System.out.print("eventType="+eventType + "---tableName="+tableName);
                if (eventType == EventType.DELETE) {
                    //delete 暂无内容
                } else if (eventType == EventType.INSERT) {
                    //振动当日表
                    if (tableName.equalsIgnoreCase("VB_RECORD_TODAY")) {
                        //处理振动数据监控逻辑
                        VbAction vbAction = new VbAction();
                       vbAction.disposeVb(tableName , columnsLlist);
                    } else if (tableName.equalsIgnoreCase("FI_EQ_STATE")) {
                        //更新消防设备异常表并保存到redis  并对异常结束时间进行判断回写
                        FiAction fiAction = new FiAction();
                        fiAction.disposeFi(tableName , columnsLlist);


                    } else if (tableName.contains("st_result") && !tableName.contains("his")) {
                        //应力结果表
                        StAction stAction = new StAction();
                        stAction.disposeST(tableName , columnsLlist);
                        //saveSTDataBaseandRedis(tableName, columnsLlist);
                    }
                } else {
                    //update 暂无内容
                }
            }
        }
    }



    public static String getWarnType(String nowState) {
        String warntype = "";
        if ("2".equals(nowState)) {
            warntype = "1";
        } else if ("3".equals(nowState)) {
            warntype = "2";
        } else if ("4".equals(nowState)) {
            warntype = "3";
        }
        return warntype;
    }




    /**
     * 按照推送设置进行数据推送
     * @param sendSettingMap
     * @param sendData
     * @return
     */
    public static boolean send(Map sendSettingMap, Map sendData ) {
        try {
            Set set = sendSettingMap.keySet();
            for (Object object : set) {
                String key = (String)object;//1:立即推送 2:延迟推送
                if ("2".equals(key)) {
                    Map continueTimeMap = (Map) sendSettingMap.get(key);
                    Set set1 = continueTimeMap.keySet();
                    for (Object object1 : set1) {
                        String continueTime = (String) object1;//延迟推送的时长：分钟
                        Long t = Long.valueOf(continueTime) * 60 * 1000;
                        //人员添加到sendMap中
                        String person = (String)continueTimeMap.get(continueTime);
                        sendData.put("person" , person);
                        //延迟执行程序
                        Timer timer = new Timer();
                        timer.schedule(new MyTimeTask(sendData) , t);
                    }
                } else {
                    //人员添加到sendMap中
                    sendData.remove("key");
                    sendData.remove("tableName");
                    String person = (String)sendSettingMap.get(key);
                    sendData.put("person" , person);
                    HttpClientUtil.sendPost(sendData, "http://172.16.72.80:8084/equip.htm?param_act=sendPost");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 通过条件查询推送设置
     * @param tableName
     * @param parms
     * @param quality
     * @return
     */
    public static Map selectSendSetting(String tableName, Object[] parms , String quality) {
        Map sendMap = new LinkedHashMap();
        String LEVEL = (String) parms[0];
        String EQ_NA = (String) parms[1];
        String PROJECT_ID = (String) parms[2];
        if("OPC_QUALITY_NOT_CONNECTED".equals(quality)){
            parms[1] = "3";
        }
        try {
            String sql = "";
            if (tableName.equalsIgnoreCase("VB_RECORD_TODAY")) {
                sql = "";
            } else if (tableName.equalsIgnoreCase("FI_EQ_STATE")) {
                //sql = "select PLAN_CODE as PLAN_CODE , SEND_TYPE as SEND_TYPE ,CONTINUE_TIME as CONTINUE_TIME from FI_SEND_SETTING where " +
                //        " SEND_LEVEL = '"+SEND_LEVEL+"'  and PART_CODE = (select PART_CODE from FI_EQUIP where EQ_NAME = '"+EQ_NA+"' ) ";
                sql = "select PROJECT_CODE as PROJECT_CODE , PART_CODE as PART_CODE , SEND_LEVEL as SEND_LEVEL ," +
                        " SEND_TYPE as SEND_TYPE , CONTINUE_TIME as CONTINUE_TIME , DEPT_CODE as DEPT_CODE , PERSON as PERSON " +
                        " from FI_SEND_SETTING where PROJECT_CODE = '"+PROJECT_ID+"'";
            } else if (tableName.contains("st_result") && !tableName.contains("his")) {
                sql = "";
            }
            List list = JDBCUtil.query(sql, null, null);
            if(list != null && list.size() > 0){
                for (int i = 0; i<list.size() ; i++){
                    Map settindMap = (Map) list.get(i);
                    //判断
                    String eq = "";
                    String level = "";
                    if("fi_eq_state".equals(tableName)){
                        eq = (String)settindMap.get("PART_CODE");
                        level = (String)settindMap.get("SEND_LEVEL");
                    }
                    if(StringUtils.isBlank(eq) && StringUtils.isBlank(level)){
                        //查询该条件下的发送方式以及人员
                        sendMap = selectPerAndTypeByWhe(settindMap , sendMap , LEVEL);

                    }else  if(StringUtils.isBlank(eq) &&  !StringUtils.isBlank(level)){
                        if(LEVEL.equals(level)){
                            sendMap = selectPerAndTypeByWhe(settindMap , sendMap  , LEVEL);
                        }
                    }else  if(!StringUtils.isBlank(eq)  && StringUtils.isBlank(level)){
                        if(EQ_NA.equals(eq)){
                            sendMap = selectPerAndTypeByWhe(settindMap , sendMap  , LEVEL);
                        }
                    }else  if(!StringUtils.isBlank(eq) &&  !StringUtils.isBlank(level)){
                        if(eq.equals(EQ_NA) && level.equals(LEVEL)){
                            sendMap = selectPerAndTypeByWhe(settindMap , sendMap  , LEVEL);
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return sendMap;
    }

    /**
     * 根据条件查询发送方式和人员
     * @param settindMap
     * @param sendMap
     * @return
     * @throws SQLException
     */
    public static Map selectPerAndTypeByWhe(Map settindMap , Map sendMap , String level  ) throws SQLException {

        String person = (String)settindMap.get("PERSON");
        if(StringUtils.isBlank(person)){
            //人员为空时查询设置好的部门下的所有人员（部门可多选，逗号拼接，人员可多选，逗号拼接）
            String dept = (String)settindMap.get("DEPT_CODE");
            String[] deptSplit = dept.split(",");
            String deptss = "";
            for (String de : deptSplit){
                deptss += "'" + de +"',";
            }
            deptss = deptss.substring(0 , deptss.length() - 1);
            String sql1 = "select USER_CODE as USER_NAME from SF_USER where ORG_CODE in( "+deptss+")";
            List personList = JDBCUtil.query(sql1, null, null);
            if(personList != null && personList.size() > 0){
                for (int j = 0 ; j<personList.size() ; j++){
                    Map personMap = (Map) personList.get(j);
                    String USER_CODE = (String)personMap.get("USER_CODE");
                    person += USER_CODE + ",";
                }
            }
            person = person.substring(0 , person.length()-1);
        }
        String type = (String)settindMap.get("SEND_TYPE");
        if(type.equals("1")){
            String per = (String)sendMap.get(type);
            if(StringUtils.isBlank(per)){
                sendMap.put(type , person);
            }else {
                person = distinctPerName(per , person);
                sendMap.put(type , person);
            }
        }else if(type.equals("2")){
            String continueTime = (String)settindMap.get("CONTINUE_TIME");
            Map continueTimeMap = (Map) sendMap.get(type);
            //当前发送类型对应的map是否存在
            if(continueTimeMap == null){
                continueTimeMap = new LinkedHashMap();
                continueTimeMap.put(continueTime , person);
            }else {
                String per = (String)continueTimeMap.get(continueTime);
                //当前时长对应的map是否存在
                if(StringUtils.isBlank(per)){
                    continueTimeMap.put(continueTime , person);
                }else{
                    //存在，去重
                    person = distinctPerName(per , person);
                    continueTimeMap.put(continueTime , person);
                }
            }
            sendMap.put(type , continueTimeMap);
        }
        return sendMap;
    }


    /**
     * 获取到的需要发送消息的人员名称去重
     * @param oldPer
     * @param newPer
     * @return
     */
    public static String distinctPerName(String oldPer , String newPer){
        String[] first = oldPer.split(",");
        String[] second = newPer.split(",");
        //String[] first = {"1","4","5","9"};
        //String[] second = {"6","4","5","7"};
        //合并两个数组
        String[] temp = (String[]) ArrayUtils.addAll(first, second);

        //去重=========================================
        int size=temp.length;
        for(int i=0;i<temp.length;i++){
            if(temp[i]!=""){
                for(int j=i+1;j<temp.length;j++){
                    if(temp[i]==temp[j] || temp[i].equals(temp[j])){
                        temp[j]="";//将发生重复的元素赋值为-1
                        size--;
                    }
                }
            }
        }
        List<String> list = new ArrayList<String>();
        for(int j=0;j<temp.length;j++){
            if(temp[j] != ""){
                list.add(temp[j]);
                //System.out.println(temp[j]+"==");
            }
        }
        String person = "";
        if (list != null && list.size() > 0){
            for (String name : list){
                person += name + ",";
            }
            person = person.substring(0 , person.length() - 1);
        }
        return person;
    }



    /**
     * 获取事件时长，单位/分钟
     * @param currTime
     * @param excetion_time
     * @return
     */
    public static Double getEventTime(String currTime, String excetion_time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //当前数据推送时间
        Date ctd = sdf.parse(currTime);
        //时间开始时间
        Date otd = sdf.parse(excetion_time);
        Long diff = ctd.getTime() - otd.getTime();
        double v = diff / (1000 * 60.0);
        BigDecimal   b   =   new   BigDecimal(v);
        double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();

        return f1;
    }

    /**
     * 更新应力当日表数据并将记录保存到redis
     *
     * @param tableName
     * @param columnsLlist
     */
    private static void saveSTDataBaseandRedis(String tableName, List<Column> columnsLlist) throws SQLException {
        //Connection conn = DBUtil.getConnection();
        if (columnsLlist != null && columnsLlist.size() > 0) {
            String EQ_CODE = "";
            String EQ_NAME = "";
            String SENDTIME = "";
            //获取除应力值和温度值外的其他字段值
            for (Column column : columnsLlist) {
                //System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
                if (column.getName().equalsIgnoreCase("EQ_CODE")) {
                    EQ_CODE = column.getValue();
                }
                if (column.getName().equalsIgnoreCase("SENDTIME")) {
                    SENDTIME = column.getValue();
                }
                if (column.getName().equalsIgnoreCase("EQ_NAME")) {
                    EQ_NAME = column.getValue();
                }
            }
            for (Column column : columnsLlist) {
                String colnmuLowCase = column.getName().toLowerCase();
                boolean flag = false;
                //当前字段为应力值
                if (colnmuLowCase.contains("stress")) {
                    String POINT_CODE = colnmuLowCase.substring(6, colnmuLowCase.length());

                    //判断当前序号的测点记录是否为新纪录
                    String ssql = "select count(*)  as count  from ST_WARNING where POINT_CODE = '" + POINT_CODE + "'";
                    List list = JDBCUtil.query(ssql, null, null);
                    if (list != null && list.size() > 0) {
                        Map map = (Map) list.get(0);
                        Object count = map.get("count");
                        if (!"0".equals(count)) {
                            flag = true;
                        }
                    }
                    String stressValue = column.getValue();
                    //获取当前监测点对应的三种临界值
                    String sql = "select  POINT_POSITION as POINT_POSITION , WARNING_VALUE as WARNING_VALUE ," +
                            " ALARM_VALUE as ALARM_VALUE , DANGER_VALUE as DANGER_VALUE from ST_POINT where POINT_CODE = '" + POINT_CODE + "'";
                    List query = JDBCUtil.query(sql, new String[]{}, null);
                    Double WARNING_VALUE = 0d;
                    Double ALARM_VALUE = 0d;
                    Double DANGER_VALUE = 0d;
                    String POINT_POSITION = "";
                    String stSta = "1";
                    if (query != null && query.size() > 0) {
                        Map map = (Map) query.get(0);
                        WARNING_VALUE = (Double) map.get("WARNING_VALUE");
                        ALARM_VALUE = (Double) map.get("ALARM_VALUE");
                        DANGER_VALUE = (Double) map.get("DANGER_VALUE");
                        POINT_POSITION = (String) map.get("POINT_POSITION");
                    }
                    //比较当前监测点的三种临界值，判断报警级别
                    if (StringUtils.isBlank(stressValue)) {
                        continue;
                    }
                    String ALERM_LEVEL = "";
                    Double aDouble = Double.valueOf(stressValue);
                    if (aDouble >= WARNING_VALUE && aDouble < ALARM_VALUE) {
                        stSta = "2";
                        ALERM_LEVEL = "1";
                    } else if (aDouble >= ALARM_VALUE && aDouble < DANGER_VALUE) {
                        stSta = "3";
                        ALERM_LEVEL = "2";
                    } else if (aDouble >= DANGER_VALUE) {
                        stSta = "4";
                        ALERM_LEVEL = "3";
                    } else if (aDouble >= 0d && aDouble < WARNING_VALUE) {
                        stSta = "1";
                    }
                    //ALERM_LEVEL = getLevel(aDouble, WARNING_VALUE, ALARM_VALUE, DANGER_VALUE);

                    //如果报警级别不为0，推送(没有结束时间) , 如果报警级别为0 ， 推送带有结束时间的记录
                    String saveToStWarnSql = "";
                    String ALERM_END = null;
                    //判断当前插入的最新记录的状态和当前序号的测点的第二位（时间倒序）状态是否一致，不一致添加结束时间
                    //查询当前序号测点的时间倒序第二位状态
                    String saveToStWarnSql1 = "INSERT INTO ST_WARNING " +
                            "(`ID`, `EQ_ID`, `EQ_NAME`, `POINT_CODE`, `POINT_POSITION`, `ST_VALUE` ,`ALERM_START`, `ALERM_END`, `ALERM_LEVEL`," +
                            " `REMARK`, `CREATE_USER`, `CREATE_TIME`, `MODIFY_USER`, `MODIFY_TIME`, `AUD_USER`, `AUD_FLAG`, `AUD_TIME`) " +
                            "VALUES ('" + IdGenerator.getId() + "', '" + EQ_CODE + "', '" + EQ_NAME + "', '" + POINT_CODE + "', '" + POINT_POSITION + "', '"
                            + stressValue + "','" + SENDTIME + "', NULL, '" + ALERM_LEVEL + "', '', '', NULL, NULL, NULL, NULL, NULL, NULL)";

                    String saveToStWarnSql2 = "INSERT INTO ST_WARNING " +
                            "(`ID`, `EQ_ID`, `EQ_NAME`, `POINT_CODE`, `POINT_POSITION`, `ST_VALUE` ,`ALERM_START`, `ALERM_END`, `ALERM_LEVEL`," +
                            " `REMARK`, `CREATE_USER`, `CREATE_TIME`, `MODIFY_USER`, `MODIFY_TIME`, `AUD_USER`, `AUD_FLAG`, `AUD_TIME`) " +
                            "VALUES ('" + IdGenerator.getId() + "', '" + EQ_CODE + "', '" + EQ_NAME + "', '" + POINT_CODE + "', '" + POINT_POSITION + "', '"
                            + stressValue + "',NULL, NULL, '" + ALERM_LEVEL + "', '', '', NULL, NULL, NULL, NULL, NULL, NULL)";

                    //当前时间转换成字符串
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currTime = sdf.format(new Date());
                    //查询报警记录表中对应当前测点序号的记录
                    String secondSql = "select  ID as ID , ALERM_LEVEL as ALERM_LEVEL   from ST_WARNING where POINT_CODE = '" + POINT_CODE + "' order by ID desc";
                    List querySecond = JDBCUtil.query(secondSql, new String[]{}, null);
                    //if(!"1".equals(ALERM_LEVEL)) {
                    //如果当前序号没有存在记录，如果是正常状态不添加，
                    // 如果存在记录，先判断上一条记录的级别
                        //级别相同：一条记录时：
                        //级别不同：如果只有一条时修改上一条的结束时间
                                        // 如果多条，修改修改上一条的开始时间，结束时间
                    String updateFirstSql1 = "update ST_WARNING set ALERM_END = ? where ID = ?";        //更新结束时间
                    //String updateFirstSql2 = "update ST_WARNING set ALERM_START = '', ALERM_END = ? where ID = ?";      //更新开始时间、结束时间
                    if (querySecond != null && querySecond.size() >= 1) {
                        Map secondMap =  (Map) querySecond.get(0);;
                        String updateFirstSql = "";

                        String secondLevel = (String) secondMap.get("ALERM_LEVEL");
                        String ID = (String) secondMap.get("ID");
                        Object[] pares = new Object[]{};
                        if (!"1".equals(stSta)) {
                            //状态不同，保存带有开始时间的报警记录
                            if (!secondLevel.equals(ALERM_LEVEL)) {
                                saveToStWarnSql = saveToStWarnSql1;
                                //JDBCUtil.edit(saveToStWarnSql, null);
                                //if (querySecond.size() > 1) {
                                updateFirstSql = "update ST_WARNING set ALERM_END = '"+currTime+"' where ID = '"+ID+"'";        //更新结束时间
                                    JDBCUtil.edit(updateFirstSql, null);
                                //} else if (querySecond.size() == 1) {
                                //    JDBCUtil.edit(updateFirstSql1, null);
                                //}
                            } else {
                                //状态相同，报警记录的开始时间和结束时间清空
                                saveToStWarnSql = saveToStWarnSql2;
                            }
                            //直接插入报警记录
                            JDBCUtil.edit(saveToStWarnSql, null);
                        } else {
                            //如果当前测点的状态为正常：1 的情况，查看上一条该测点的记录的状态，如果不同，将上一条记录的结束时间添加上
                            if (!"1".equals(secondLevel)) {
                                updateFirstSql = "update ST_WARNING set ALERM_END = '"+currTime+"' where ID = '"+ID+"'";        //更新结束时间
                                int edit = JDBCUtil.edit(updateFirstSql, null);
                            }
                        }
                    } else {
                        //没有记录，直接插入报警记录
                        if (!"1".equals(stSta)) {
                            saveToStWarnSql = saveToStWarnSql1;
                            JDBCUtil.edit(saveToStWarnSql, null);
                        }
                    }
                    //}
                    //将应力值保存到redis
                    redisInsert("STRESS" + POINT_CODE, stressValue);
                    //JedisUtil.setString("STRESS" + POINT_CODE,stressValue);

                }
                //TODO:温度报警暂时没有设定临界值
                if (colnmuLowCase.contains("temper")) {
                    String temperValue = column.getValue();
                    if (StringUtils.isBlank(temperValue)) {
                        continue;
                    }
                    String POINT_CODE = colnmuLowCase.substring(6, colnmuLowCase.length());
                    //将应力值保存到redis
                    redisInsert("TEMPER" + POINT_CODE, temperValue);
                }
            }


        }
    }

    /**
     * 更新振动当日表数据并将记录保存到redis
     *
     * @param tableName
     * @param columnsLlist
     */
    public static <T> T getBean(String tableName, List<Column> columnsLlist) throws SQLException {
        T t = null;
        if (tableName.equalsIgnoreCase("vb_record_today")) {
            t = (T) getRecordBean(tableName, columnsLlist, Record.class);

        }
        if (tableName.equalsIgnoreCase("fi_eq_state")) {
            t = (T) getRecordBean(tableName, columnsLlist, FiBean.class);

        }
        return t;
    }

    /**
     * 获取振动数据javabean
     * @param tableName
     * @param columnsLlist
     * @param clazz
     * @param <T>
     * @return
     * @throws SQLException
     */
    private static <T> T getRecordBean(String tableName, List<Column> columnsLlist, Class<T> clazz) throws SQLException {
        T t = null;
        JSONObject json = new JSONObject();
        if (columnsLlist != null && columnsLlist.size() > 0) {
            for (Column column : columnsLlist) {
                json.put(column.getName(), column.getValue());
            }
            t = JSONObject.parseObject(json.toJSONString(), clazz);
        }
        return t;
    }

    /**
     * 打印 获取数据集合
     * @param entrys
     * @return
     */
    public static List printEntry(List<Entry> entrys) {
        List list = new ArrayList();
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }
            Map map = new LinkedHashMap();

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
            map.put("eventType", eventType);
            String tableName = entry.getHeader().getTableName();
            map.put("tableName", tableName);
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    //printColumn(rowData.getBeforeColumnsList());
                    map.put("columnsList", rowData.getBeforeColumnsList());
                    //redisDelete(rowData.getBeforeColumnsList());
                    //数据库删除操作
                    //dataBaseInsert(rowData.getAfterColumnsList() , map);
                } else if (eventType == EventType.INSERT) {
                    //printColumn(rowData.getAfterColumnsList());

                    map.put("columnsList", rowData.getAfterColumnsList());
                    //redisInsert(rowData.getAfterColumnsList() , tableName);
                    ////数据库添加操作
                    //if(tableName.equalsIgnoreCase("vb_record_today")){
                    //    //如果向振动数据当日表中推送数据，需要添加 ITEMCORD 和 MONI_STATE 两个字段
                    //
                    //
                    //    dataBaseInsert(rowData.getAfterColumnsList() , map);
                    //}
                    //dataBaseInsert(rowData.getAfterColumnsList() , map);
                } else {
                    //System.out.println("-------> before");
                    //printColumn(rowData.getBeforeColumnsList());
                    //System.out.println("-------> after");
                    //redisUpdate(rowData.getAfterColumnsList());

                    map.put("columnsList", rowData.getAfterColumnsList());
                    //数据库更新操作
                    //dataBaseInsert(rowData.getAfterColumnsList() , map);
                }
            }

            list.add(map);
        }
        return list;
    }

    /**
     * 保存到redis
     * @param key
     * @param value
     */
    public static void redisInsert(String key, String value) {
        RedisUtil.stringSet(key, value);
    }

}

