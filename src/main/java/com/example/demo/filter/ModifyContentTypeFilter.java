package com.example.demo.filter;

import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.buf.MessageBytes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.Field;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ModifyContentTypeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            Field field =servletRequest.getClass().getDeclaredField("request");
            field.setAccessible(true);
            Request request = (Request) field.get(servletRequest);
            MessageBytes contentType = MessageBytes.newInstance();
            contentType.setString("application/octet-stream");
            request.getCoyoteRequest().setContentType(contentType);
            filterChain.doFilter(request, servletResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
