package com.alibaba.otter.canal.example.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * redis 数据增删改查
 * 保存[key:string/list]
 * 集群时SharedJedis替换Jedis
 * @author zhaolong
 */
public class JedisUtil {

    //public static Jedis getJedis(){
    //    //1.设置连接池的配置对象
    //    JedisPoolConfig config = new JedisPoolConfig();
    //    //设置池中最大连接数
    //    config.setMaxTotal(50);
    //    //设置空闲时池中保有的最大连接数
    //    config.setMaxIdle(10);
    //    //2.设置连接池对象
    //    JedisPool pool = new JedisPool(config,"127.0.0.1",6379);
    //    //3.从池中获取连接对象
    //    Jedis jedis = pool.getResource();
    //    System.out.println(jedis.hget("hash", "uname"));
    //    return jedis;
    //}

    private static Logger logger = LoggerFactory.getLogger(JedisUtil.class);
    //public static JedisPool jedisPool = ContextUtil.getJedisPool();

    private JedisUtil() {
    }

    // 判断key是否存在
    public static boolean exists(String key) {
        boolean isBroken = false;
        Jedis jedis = null;
        Boolean flag =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                flag = jedis.exists(key);
            }
        } catch (Exception e) {
                isBroken = true;
        } finally {
                RedisUtil.closeResource(jedis, isBroken);
        }
        return flag;
    }

    // 保存string string 键值对 成功后返回OK
    public static String setString(String key, String value) {
        boolean isBroken = false;
        Jedis jedis = null;
        String temp =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                temp = jedis.set(key, value);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        return temp;

    }

    /**
     * 定时保存String
     *
     * @return OK :保存成功
     */
    public static String setString(String key, String value, int overtime) {
        boolean isBroken = false;
        Jedis jedis = null;
        String temp =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                temp = jedis.set(key, value);
                jedis.expire(key, overtime);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        return temp;
    }

    // 取值
    public static String get(String key) {
        boolean isBroken = false;
        Jedis jedis = null;
        String temp =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                if (key == null || key == "") {
                    return null;
                }
                temp = jedis.get(key);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        return temp;
    }


    // 保存object
    public static String setJSON(String key, Object value) {
        return setString(key, JSON.toJSONString(value));
    }

    /**
     * 定时保存Object
     *
     * @return OK :保存成功
     */
    public static String setJSON(String key, Object value, int overtime) {
        return setString(key, JSON.toJSONString(value), overtime);
    }

    // 取值 返回泛型
    public static <T> T get(String key, Class<T> clazz) {
        return JSON.parseObject(get(key), clazz);
    }

    // 取值返回jsonObject
    public static JSONObject getJSONObject(String key) {
        return JSON.parseObject(get(key));
    }

    /**
     * 列表插入bean对象;列表长度为100
     */
    public static void lpush(String key, Object value) {
        boolean isBroken = false;
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                jedis.lpush(key, JSON.toJSONString(value));
                jedis.ltrim(key, 0, 99);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
    }

    /**
     * 列表取出最新数据
     */
    public static JSONObject lrangeFirst(String key) {
        boolean isBroken = false;
        Jedis jedis = null;
        List<String> list =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                list = jedis.lrange(key, 0, 0);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        return JSONObject.parseObject(list.get(0));
    }

    /**
     * 列表取出所有数据
     */
    public static <T> List<T> lrange(String key, Class<T> clazz) {
        boolean isBroken = false;
        Jedis jedis = null;
        List<String> list =null;
        try {
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                list = jedis.lrange(key, 0, 0);
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        List<T> Tlist = new ArrayList<T>();
        for (String s : list) {
            Tlist.add(JSONObject.parseObject(s, clazz));
        }
        return Tlist;

    }

    // 删除
    public static long del(String key) {
        boolean isBroken = false;
        Jedis jedis = null;
        long temp = 0;
        try {
            jedis = RedisUtil.getJedis();
            temp = jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
            isBroken = true;
        }finally {
            RedisUtil.closeResource(jedis, isBroken);
        }
        return temp;
    }

    // 把对象转为json对象
    public static String getJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * Description: 获取泛型List
     */
    public static <T> List<T> getArray(String key, Class<T> clazz) {
        return JSON.parseArray(get(key), clazz);
    }

}
