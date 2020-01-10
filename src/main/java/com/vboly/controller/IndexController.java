package com.vboly.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vboly.entity.PostResult;
import com.vboly.util.CookieUtil;
import com.vboly.util.MyWebSocketServerHandler;
import com.vboly.util.RedisUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 抽奖
 */
@Controller
public class IndexController {

    protected final Log log = LogFactory.getLog(IndexController.class);

    private static final String KEY1 = "luckDrawUid";
    private static final String KEY2 = "luckDrawRid";
    private static final String KEY3 = "luckDrawData";
    private static final String KEY4 = "luckDrawName";

    private static final String KEY5 = "packNum";
    private static final String KEY6 = "packDownTime";
    private static final String KEY7 = "packBatch";
    private static final String KEY8 = "packLog";
    private static final String KEY9 = "packRobUid";

    private static final int TIMEOUT = 60 * 60 * 24 * 180;


    private static String robPackScript
            = "local isUser = redis.call('sismember', KEYS[1], KEYS[7])\n"
            + "if isUser == 0 then\n"
            + "  return -1\n"
            + "else\n"
            + "  local dateTime = redis.call('get', KEYS[5]);\n"
            + "  if dateTime then\n"
            + "    local pnKey = KEYS[3] .. ':' .. dateTime;\n"
            + "    local prKey = KEYS[6] .. ':' .. dateTime;\n"
            + "    local packNum = redis.call('get', pnKey)\n"
            + "    print(packNum)\n"
            + "    if tonumber(packNum) > 0 then\n"
            + "      if redis.call('ttl', KEYS[4]) < 0 then\n"
            + "        redis.call('srem', KEYS[1], KEYS[7]);\n"
            + "        redis.call('lpush', KEYS[2], KEYS[7]);\n"
            + "        redis.call('lpush', prKey, KEYS[7]);\n"
            + "        redis.call('decr', pnKey);\n"
            + "        return 1\n"
            + "      end\n"
            + "      return -3\n"
            + "    end\n"
            + "    return -2\n"
            + "  end\n"
            + "  return -4\n"
            + "end\n"
            + "return nil";

    /**
     * 抽奖页面
     */
    @RequestMapping(value = "index")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    /**
     * 用户页面
     * @param unionId 微信唯一标识
     */
    @RequestMapping(value = "draw")
    public ModelAndView draw(HttpServletRequest request, HttpServletResponse response, String unionId) {
        String loginId = CookieUtil.getCookieByName(request, "draw");
        if(loginId == null){
            CookieUtil.setCookieByName(response,"draw", unionId, null);
        }else{
            unionId = loginId;
        }
        List<String> removeList = RedisUtil.lrange(KEY2, null);
        boolean bl = removeList != null && removeList.contains(unionId);
        return new ModelAndView("draw")
                .addObject("isLucky", bl);
    }

    /**
     * 记录页面
     */
    @RequestMapping(value = "operate")
    public ModelAndView operate() {
        return new ModelAndView("operate");
    }

    /**
     * 验证是否填写信息
     * @param unionId 微信唯一标识
     * @return 1.用户已填写信息 2.用户未填写信息 -1.验证异常
     */
    @RequestMapping(value = "isRegister", method = RequestMethod.POST)
    @ResponseBody
    public PostResult isRegister(@RequestParam("unionId") String unionId) {
        PostResult postResult;
        try {
            List<String> removeList = RedisUtil.lrange(KEY2, null);
            boolean bl1 = removeList != null && removeList.contains(unionId);
            boolean bl2 = RedisUtil.sismember(KEY1, unionId, null);
            if (bl1 || bl2) {
                postResult = new PostResult(1, "用户已填写信息");
            }else{
                postResult = new PostResult(2, "用户未填写信息");
            }
        } catch (Exception e) {
            e.printStackTrace();
            postResult = new PostResult(-1, "验证异常");
        }
        return postResult;
    }

    /**
     * 添加抽奖用户
     * @param unionId  微信唯一标识
     * @param name     微信昵称
     * @param imgUrl   微信头像
     * @param realName 真实姓名
     * @param tableNum 桌号
     * @param seatNum  座号
     * @return 1.扫描成功 -1.扫描异常 -3.微信号已存在 -4.姓名已存在
     */
    @RequestMapping(value = "insertUser", method = RequestMethod.POST)
    @ResponseBody
    public PostResult insertUser(String unionId, String name, String imgUrl, String realName, String tableNum, String seatNum) {
        PostResult postResult;
        if (StringUtils.isEmpty(unionId) || StringUtils.isEmpty(name) || StringUtils.isEmpty(realName)
                || StringUtils.isEmpty(tableNum) || StringUtils.isEmpty(seatNum)) {
            return new PostResult(-2, "参数不正确");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("unionId", unionId);
        param.put("name", name);
        param.put("imgUrl", imgUrl);
        param.put("realName", realName);
        param.put("tableNum", tableNum);
        param.put("seatNum", seatNum);
        try {
            List<String> removeList = RedisUtil.lrange(KEY2, null);
            boolean bl1 = removeList != null && removeList.contains(unionId);
            boolean bl2 = RedisUtil.sismember(KEY1, unionId, null);
            boolean bl3 = RedisUtil.sismember(KEY4, realName, null);
            if (!bl1 && !bl2) {
                if (!bl3) {
                    String message = JSON.toJSON(param).toString();
                    // KEY1-添加可中奖用户
                    RedisUtil.sadd(KEY1, unionId, null, TIMEOUT);
                    // KEY3-添加用户信息
                    RedisUtil.hset(KEY3, unionId, message, null, TIMEOUT);
                    // KEY4-添加用户真实姓名
                    RedisUtil.sadd(KEY4, realName, null, TIMEOUT);

                    // 把用户信息推送到前端
                    // MyWebSocketServerHandler.sendGroup(message);
                    postResult = new PostResult(1, "扫描成功");
                } else {
                    postResult = new PostResult(-4, "姓名已存在");
                }
            } else {
                postResult = new PostResult(-3, "微信号已存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            postResult = new PostResult(-1, "扫描异常");
        }
        System.out.println(postResult.toString());
        return postResult;
    }

    /**
     * 添加抽奖信息
     *
     * @param num 抽取数量
     */
    @RequestMapping(value = "insertPack", method = RequestMethod.POST)
    @ResponseBody
    public PostResult insertPack(@RequestParam("num") int num) {
        PostResult postResult;
        try {
            Long dateTime = System.currentTimeMillis() / 1000;
            // KEY5-添加抽奖数量
            String re = RedisUtil.set(KEY5 + ":" + dateTime, num + "", null, TIMEOUT);
            System.out.println(re);
            // KEY5-添加抽奖倒计时
            RedisUtil.set(KEY6, "5", null, 5);
            RedisUtil.set(KEY7, dateTime + "", null, TIMEOUT);
            // KEY7-记录抽奖信息
            JSONObject json = new JSONObject();
            json.put("dateTime", dateTime);
            json.put("num", num);
            RedisUtil.lpush(KEY8, json.toJSONString(), null, TIMEOUT);

            postResult = new PostResult(1, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            postResult = new PostResult(-1, "添加异常");
        }
        System.out.println(postResult.toString());
        return postResult;
    }

    /**
     * 抽奖
     */
    @RequestMapping(value = "robPack", method = RequestMethod.POST)
    @ResponseBody
    public PostResult robPack(HttpServletRequest request) {
        PostResult postResult;
        String unionId = CookieUtil.getCookieByName(request, "draw");
        if(unionId != null){
            try {
                Object object = RedisUtil.eval(robPackScript, 7, KEY1, KEY2, KEY5, KEY6, KEY7, KEY9, unionId);
                if (object != null) {
                    int result = Integer.parseInt(object.toString());
                    if (result == 1) {
                        String message = RedisUtil.hget(KEY3, unionId, null);
                        JSONObject json = JSONObject.parseObject(message);
                        json.put("directive", "DRAW");
                        MyWebSocketServerHandler.sendGroup(json.toJSONString());
                        postResult = new PostResult(1, "抽奖成功！");
                    } else if (result == -1) {
                        postResult = new PostResult(-5, "您没有抽奖名额！");
                    } else if (result == -2) {
                        postResult = new PostResult(-4, "很遗憾，您未中奖！");
                    } else if (result == -3) {
                        postResult = new PostResult(-3, "倒计时中，请稍后再尝试抽奖！");
                    } else if (result == -4) {
                        postResult = new PostResult(-3, "抽奖未开始！");
                    } else{
                        postResult = new PostResult(-2, "抽奖失败~");
                    }
                }else{
                    postResult = new PostResult(-2, "抽奖失败~~");
                }
            } catch (Exception e) {
                e.printStackTrace();
                postResult = new PostResult(-1, "抽奖异常~");
            }
        }else{
            postResult = new PostResult(-6, "请前往扫码页面重新扫描！");
        }
        return postResult;
    }

    /**
     * 中奖数据
     */
    @RequestMapping(value = "getNum", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getNum() {
        try {
            Map<String, Object> data = new HashMap<>();
            Long notNum = RedisUtil.scard(KEY1, null);
            Long drawNum = RedisUtil.llen(KEY2, null);
            data.put("notNum", notNum);
            data.put("drawNum", drawNum);
            return data;
        }catch (Exception e){
            log.error("中奖用户", e);
        }
        return null;
    }

    /**
     * 重置抽奖信息
     */
    @RequestMapping(value = "reset", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> reset() {
        try {
            RedisUtil.del(KEY7, null);
        }catch (Exception e){
            log.error("重置抽奖信息", e);
        }
        return null;
    }

    /**
     * 操作信息
     */
    @RequestMapping(value = "operateInfo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> operateInfo() {
        try {
            Map<String, Object> data = new HashMap<>();
            Map<String, String> joinInfo = RedisUtil.hgetAll(KEY3, null);

            Set<String> keys = RedisUtil.keys(KEY9 + "*", null);
            List<List<String>> drawLists = new ArrayList<>();
            for (String key : keys){
                List<String> drawList = RedisUtil.lrange(key, null);
                drawLists.add(drawList);
            }

            data.put("joinInfo", joinInfo);
            data.put("drawLists", drawLists);
            return data;
        }catch (Exception e){
            log.error("操作信息", e);
        }
        return null;
    }

    /**
     * 回滚中奖数据
     */
    @RequestMapping(value = "rollback")
    @ResponseBody
    public Integer rollback() {
        try {
            RedisUtil.del(KEY1, null);
            RedisUtil.del(KEY2, null);
            RedisUtil.del(KEY3, null);
            RedisUtil.del(KEY4, null);
            RedisUtil.batchDel(KEY5 + "*", null);
            RedisUtil.del(KEY6, null);
            RedisUtil.del(KEY7, null);
            RedisUtil.del(KEY8, null);
            RedisUtil.batchDel(KEY9 + "*", null);
            return 1;
        } catch (Exception e) {
            log.error("回滚中奖数据", e);
        }
        return null;
    }

}
