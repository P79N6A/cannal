package com.alibaba.otter.canal.example;

import com.alibaba.otter.canal.example.bean.Record;
import com.alibaba.otter.canal.example.util.JedisUtil;

/**
 * 保存数据到VB_RECORD
 *
 * @author 高靖达
 */
public class OperateRedisAndDataBaseDao {


    public static void saveRecordToRedisAndDatabase(Record r) {
        //updateRecordByItemId(r);
        saveRecordToRedis(r);
    }


    /**
     * 把传入的数据存入redis
     */
    public static void saveRecordToRedis(Record r) {
        // 保存redis的list集合
        try {
            //JedisUtil.lpush(r.getITEMCODE(), r);
            JedisUtil.setJSON(r.getITEMCODE(), r);
            System.out.print("插入数据保存redis成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("插入数据保存redis失败");
        }
    }
}
