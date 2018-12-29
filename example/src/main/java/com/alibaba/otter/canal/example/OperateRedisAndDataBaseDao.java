package com.alibaba.otter.canal.example;

import com.alibaba.otter.canal.example.bean.Record;
import com.alibaba.otter.canal.example.util.JedisUtil;

/**
 * �������ݵ�VB_RECORD
 *
 * @author �߾���
 */
public class OperateRedisAndDataBaseDao {


    public static void saveRecordToRedisAndDatabase(Record r) {
        //updateRecordByItemId(r);
        saveRecordToRedis(r);
    }


    /**
     * �Ѵ�������ݴ���redis
     */
    public static void saveRecordToRedis(Record r) {
        // ����redis��list����
        try {
            //JedisUtil.lpush(r.getITEMCODE(), r);
            JedisUtil.setJSON(r.getITEMCODE(), r);
            System.out.print("�������ݱ���redis�ɹ�");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("�������ݱ���redisʧ��");
        }
    }
}
