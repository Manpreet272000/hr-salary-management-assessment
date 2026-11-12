package com.acme.hr.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Takes the actor from an {@code X-Actor} header when present, otherwise the single HR Manager persona. */
@Component
public class RequestHeaderActor implements CurrentActor {

    static final String HEADER = "X-Actor";
    static final String DEFAULT_ACTOR = "HR Manager";
    static final String SYSTEM_ACTOR = "system";
    private static final int MAX_LENGTH = 120;

    @Override
    public String name() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return SYSTEM_ACTOR;
        }
        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader(HEADER);
        if (header == null || header.isBlank()) {
            return DEFAULT_ACTOR;
        }
        String actor = header.strip();
        return actor.length() > MAX_LENGTH ? actor.substring(0, MAX_LENGTH) : actor;
    }
}
