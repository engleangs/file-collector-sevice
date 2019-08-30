package com.asiacell.filemonitor.service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
@Service
public class EmailServiceImpl implements EmailService {
    @Value("${email.host}")
    private String host;
    @Value("${email.password}")
    private String password;
    @Value("${email.user}")
    private String username;
    @Value("${email.port}")
    private String port;

    private Properties getEmailProperties(){
        Properties properties = new Properties();
        properties.put("mail.smtp.host",host);
        properties.put("mail.smtp.port",port);
        if( isAuthRequired()) {
            properties.put("mail.smtp.auth","true");
        }
        return properties;
    }
    boolean isAuthRequired() {
        return null != username && !"".equals(username);
    }
    Authenticator authenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }
    @Override
    public void send(String from, String to, String cc, String subject, String content) throws MessagingException {
        Properties mailProperties = getEmailProperties();
        Session session =  isAuthRequired() ? Session.getDefaultInstance(mailProperties, authenticator()) : Session.getDefaultInstance(mailProperties);
        MimeMessage message = new MimeMessage( session);
        message.setFrom( new InternetAddress( from));
        message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
        message.setSubject( subject);
        message.setContent( content , "text/html");
        if( null!= cc && !"".equals(cc)) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        }
        Transport.send( message);

    }
}
