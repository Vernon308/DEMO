package com.vernon.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtils {

    public static void renderJson(HttpServletResponse response, String s) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        try {
            if (StringUtils.isBlank(s)) {
                s = "";
            }
            response.getWriter().write(s);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            if (!"class org.apache.catalina.connector.ClientAbortException".equals(e.getClass().toString()))
                e.printStackTrace();

        }
    }
}
