package com.vboly.util;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisUtil {

    /**
     * 默认选中 缓存域
     */
    final private static int default_db = 11;

    /**
     * 设置String的值
     * @param key     key值
     * @param val     需要存储的字符串
     * @param db      需要选中的域
     * @param seconds 有效时间
     * @return Long
     */
    public static String set(String key, String val, Integer db, Integer seconds) {
        Jedis jedis = null;
        String re = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                re = jedis.set(key, val);

                if (null != seconds) {
                    jedis.expire(key, seconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return re;
    }

    /**
     * 获取String的值
     * @param key key值
     * @param db  需要选中的域
     * @return List<String>
     */
    public static String get(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 删除 key
     * @param key key值
     * @param db  需要选中的域
     * @return Long
     */
    public static Long del(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.del(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 批量删除
     */
    public static boolean batchDel(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                /*************** 删除脚本********Map类型存储，需'key'，找出所以相关key，再删除 ******************/
                String script = "";
                script = "local t = redis.call('keys', '" + key + "') "
                        + "if #t ~= 0 then redis.call('del', unpack(redis.call('keys', '"
                        + key + "')))  end";
                String code = jedis.scriptLoad(script);
                jedis.evalsha(code);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return false;
    }

    /**
     * 查找所有符合给定模式(pattern)的 key
     * @param key key值
     * @param db  需要选中的域
     * @return Set<String>
     */
    public static Set<String> keys(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.keys(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 设置哈希的值
     * @param key     key值
     * @param field   field
     * @param val     需要存储的字符串
     * @param db      需要选中的域
     * @param seconds 有效时间
     * @return Long
     */
    public static Long hset(String key, String field, String val, Integer db, Integer seconds) {
        Jedis jedis = null;
        Long re = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                re = jedis.hset(key, field, val);

                if (null != seconds) {
                    jedis.expire(key, seconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return re;
    }

    /**
     * 根据key与field获取哈希的值
     * @param key key值
     * @param field   field
     * @param db  需要选中的域
     * @return String
     */
    public static String hget(String key, String field, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.hget(key, field);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 获取哈希的值
     * @param key key值
     * @param db  需要选中的域
     * @return Map<String, String>
     */
    public static Map<String, String> hgetAll(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.hgetAll(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 设置List集合的值
     * @param key     key值
     * @param val     需要存储的字符串
     * @param db      需要选中的域
     * @param seconds 有效时间
     * @return Long
     */
    public static Long lpush(String key, String val, Integer db, Integer seconds) {
        Jedis jedis = null;
        Long re = 0L;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                re = jedis.lpush(key, val);

                if (null != seconds) {
                    jedis.expire(key, seconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return re;
    }


    /**
     * 获取List集合的值
     * @param key key值
     * @param db  需要选中的域
     * @return List<String>
     */
    public static List<String> lrange(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.lrange(key, 0, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 获取List集合中元素的数量
     * @param key key值
     * @param db  需要选中的域
     * @return Set<String>
     */
    public static Long llen(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.llen(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 移除List集合最后一个元素，返回值为移除的元素
     * @param key key值
     * @param db  需要选中的域
     * @return List<String>
     */
    public static String rpop(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.rpop(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }


    /**
     * 添加set的值
     * @param key     key值
     * @param val     需要存储的字符串
     * @param db      需要选中的域
     * @param seconds 有效时间
     * @return Long
     */
    public static Long sadd(String key, String val, Integer db, Integer seconds) {
        Jedis jedis = null;
        long result = 0L;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                result = jedis.sadd(key, val);
                if (null != seconds) {
                    jedis.expire(key, seconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return result;
    }

    /**
     * 获取Set集合的值
     * @param key key值
     * @param db  需要选中的域
     * @return Set<String>
     */
    public static Set<String> smembers(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.smembers(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 获取Set集合中元素的数量
     * @param key key值
     * @param db  需要选中的域
     * @return Set<String>
     */
    public static Long scard(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.scard(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 移除集合中的一个或多个成员元素
     * @param key key值
     * @param val val值
     * @param db  需要选中的域
     * @return Long
     */
    public static Long srem(String key, String val, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.srem(key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }

    /**
     * 判断key值是否存在-Set
     * @param key key值
     * @param val val值
     * @param db  需要选中的域
     * @return boolean
     */
    public static boolean sismember(String key, String val, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.sismember(key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return false;
    }

    /**
     * 判断key值是否存在-String
     * @param key key值
     * @param db  需要选中的域
     * @return boolean
     */
    public static boolean exists(String key, Integer db) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                if (db == null) {
                    jedis.select(default_db);
                } else {
                    jedis.select(db);
                }
                return jedis.exists(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return false;
    }

    /**
     * 将脚本 script 添加到脚本缓存中，立即执行
     * @param script   脚本
     * @param keyCount 参数数量
     * @param script   参数
     */
    public static Object eval(String script, int keyCount, String... params) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            if (jedis != null) {
                jedis.select(default_db);
                String code = jedis.scriptLoad(script);
                return jedis.evalsha(code, keyCount, params);
                // return jedis.eval(script, keyCount, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisPool.close(jedis);
        }
        return null;
    }
}
