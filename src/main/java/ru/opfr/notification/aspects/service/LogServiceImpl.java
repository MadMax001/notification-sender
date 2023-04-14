package ru.opfr.notification.aspects.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogServiceImpl implements LogService{
    @Override
    public void info(String info) {
        log.info(info);
    }

    @Override
    public void error(String info) {
        log.error(info);
    }

    @Override
    public void error(String message, Throwable exception) {
        log.error(message, exception);
    }
}
