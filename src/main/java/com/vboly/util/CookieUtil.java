package com.vboly.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    /**
     * 根据name取出cookie的值
     * @param request 请求信息
     * @param name    名称
     */
    public static String getCookieByName(HttpServletRequest request, String name) {
        String value = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    value = cookie.getValue();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * 根据name设置cookie的值
     * @param response 响应信息
     * @param name     名称
     * @param value    数值
     * @param second   缓存时间
     */
    public static String setCookieByName(HttpServletResponse response, String name, String value, Integer second) {
        if (value != null) {
            Cookie cookie = new Cookie(name, value);
            if (second != null) {
                cookie.setMaxAge(second);
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return value;
    }

}