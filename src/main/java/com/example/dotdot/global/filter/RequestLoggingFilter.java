package com.example.dotdot.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

// @Component 어노테이션으로 스프링 빈으로 등록합니다.
@Component
// @Order 어노테이션으로 필터 중 가장 먼저 실행되도록 설정합니다.
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HttpServletRequest로 캐스팅하여 HTTP 관련 정보를 얻습니다.
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // --- 요청 정보를 콘솔에 출력 ---
        System.out.println("--- [Request Log] ---");
        System.out.println("Request URI: " + httpRequest.getRequestURI());
        System.out.println("Request Method: " + httpRequest.getMethod());
        System.out.println("Remote Address: " + httpRequest.getRemoteAddr());

        // 모든 헤더 정보를 출력합니다.
        Collections.list(httpRequest.getHeaderNames()).forEach(headerName ->
                System.out.println(headerName + ": " + httpRequest.getHeader(headerName))
        );
        System.out.println("-----------------------");
        // --- 여기까지 ---

        // 다음 필터로 요청을 전달합니다. 이 코드가 없으면 요청이 멈춥니다.
        chain.doFilter(request, response);
    }
}