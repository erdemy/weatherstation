package com.kiteclub.weather.config;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ConnectionAliveFilter implements Filter {

    private static final String CONNECTION = "Connection";
    private static final String CLOSE = "close";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader(CONNECTION, CLOSE);
        chain.doFilter(request, response);
    }
}