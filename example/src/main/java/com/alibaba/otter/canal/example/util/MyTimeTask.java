package com.alibaba.otter.canal.example.util;

import java.util.Map;
import java.util.TimerTask;

public class MyTimeTask extends TimerTask {

    Map sendData ;

    public MyTimeTask(Map sendData) {
        this.sendData = sendData;
    }

    @Override
    public void run() {
        try {
            String key = (String) sendData.get("key");
            String tableName = (String) sendData.get("tableName");
            //延时发送消息时比较sendData中的事件与redis中最新的事件状态比较，状态发生变化，停止发送消息
            boolean flag = compareEvent(key , tableName , sendData);
            if (flag){
                return;
            }

            sendData.remove("key");
            sendData.remove("tableName");
            String s = HttpClientUtil.sendPost(sendData,
                    "http://172.16.72.80:8084/equip.htm?param_act=sendPost");
            System.out.println(s);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.gc();
            cancel();
        }
    }

    private boolean compareEvent(String key, String tableName, Map sendData) {
        if (tableName.equalsIgnoreCase("VB_RECORD_TODAY")) {

        } else if (tableName.equalsIgnoreCase("FI_EQ_STATE")) {
            String fiBeanStr = JedisUtil.get(key);
            Map<String, Object> fiBeanMap = JsonUtil.jsonToMap(fiBeanStr);
            if(fiBeanMap != null && fiBeanMap.size() > 0){
                String DeviceValue = (String) fiBeanMap.get("DeviceValue");
                String Quality = (String) fiBeanMap.get("Quality");
                if(Quality.equalsIgnoreCase("OPC_QUALITY_NOT_CONNECTED")){
                    DeviceValue = "33";
                }
                String event = (String) sendData.get("event");
                if(!event.equals(DeviceValue)){
                    return true;
                }
            }
        } else if (tableName.contains("st_result") && !tableName.contains("his")) {
        }

        return false;
    }

    public Map getSendData() {
        return sendData;
    }

    public void setSendData(Map sendData) {
        this.sendData = sendData;
    }
}
