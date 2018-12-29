package com.alibaba.otter.canal.example;

import com.alibaba.otter.canal.example.util.MyTimeTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;

public class TestAction {
    public static void main(String[] args){
        String tableName = "FI_EQ_STATE";
        Object[] parms = {"1" , "0770300703" };
        String quality = "";
        Map map = ClientSample.selectSendSetting(tableName, parms, quality);
        //Map map = new HashMap();
        //map.put("ID" , "1");
        //map.put("name" , "lisi");

        //延迟1000ms执行程序
        Timer timer = new Timer();
        timer.schedule(new MyTimeTask(map) , 5000);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(new Date());
        System.out.println("TIME1:"+format);
        if(map != null && map.size() > 0){
            String ID = (String) map.get("ID");
            System.out.println(ID);
        }
    }

}
