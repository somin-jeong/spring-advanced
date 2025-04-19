package org.example.expert.global.admiin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class AdminLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(AdminLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(AdminLogging)")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 요청 정보
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String url = request.getRequestURI();
        Long userId = (Long) request.getAttribute("userId");
        String timestamp = LocalDateTime.now().toString();
        logger.info("[AdminLoggingAspect] 사용자 ID: {}, 시간: {}, URL: {}", userId, timestamp, url);

        // 요청 본문
        Object[] args = joinPoint.getArgs();
        String requestBody = Arrays.stream(args)
                .map(arg -> {
                    try {
                        return objectMapper.writeValueAsString(arg);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.joining(", "));
        logger.info("[AdminLoggingAspect] 요청 본문: {}", requestBody);

        // 실제 메서드 실행
        Object result = joinPoint.proceed();
        logger.info("[AdminLoggingAspect] ==== API 메서드 실행 ====");

        // 응답 본문
        String responseBody = objectMapper.writeValueAsString(result);
        logger.info("[AdminLoggingAspect] 응답 본문: {}", responseBody);

        return result;
    }

}
