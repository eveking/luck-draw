package com.vboly.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 系统监听器
 */

@WebListener
public class SystemListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.initSystem(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(sce.getServletContext().getServletContextName() + " destroy");
    }

    /**
     * @author 系统初始化
     * @param sce
     */
    private void initSystem(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        // 保存项目名进Session
        sc.setAttribute("ctx", sc.getContextPath());
    }

}
