package com.asiacell.filemonitor.service.util;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
@Service
public class UtilServiceImpl implements UtilService {
    @Override
    public String pathOfMsisdn(String msisdn) {
        StringBuilder stringBuilder = new StringBuilder(msisdn.substring(0,5));
        stringBuilder.append("/").append(msisdn.substring(0,7)).append("/").append(msisdn.substring(0,9));
        return stringBuilder.toString();

    }

    @Override
    public String getHostname() {
        InetAddress ip;
        String hostname ="localhost";
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName().toLowerCase();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;

    }

    @Override
    public String guid() {
        return UUID.randomUUID().toString().replace("-","");
    }
}
