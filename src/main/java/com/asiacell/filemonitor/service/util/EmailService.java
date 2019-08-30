package com.asiacell.filemonitor.service.util;

import javax.mail.MessagingException;

public interface EmailService {
    void send(String from, String to, String cc, String subject, String content) throws MessagingException;
}
