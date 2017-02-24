package org.hspconsortium.platform.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ServiceActivator
    public String handleException(Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error("Error in messaging system", e);
        }
        return "http_servlet_response:" + HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
