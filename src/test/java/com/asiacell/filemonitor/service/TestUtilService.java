package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.service.util.UtilService;
import com.asiacell.filemonitor.service.util.UtilServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUtilService  {
    private UtilService utilService;
    @Before
    public void setup(){
        utilService = new UtilServiceImpl();
    }
    @Test
    public void Test_PathForMsisdn(){
        String path  = utilService.pathOfMsisdn("07701105526");
        System.out.println("path :"+path);
    }
}
