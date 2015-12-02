package com.mdiaf.notify.sender;


import com.mdiaf.notify.BaseTest;
import com.mdiaf.notify.listener.TestMessage;
import com.mdiaf.notify.message.ObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by Eason on 15/10/4.
 */
public class RabbitMessageSenderTest extends BaseTest{

    @Autowired
    private IMessageSender messageSender ;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {

    }

    @org.testng.annotations.Test
    public void testSend() throws Exception {
        int i = 0;
        while (true){
            ObjectMessage message = new ObjectMessage(new TestMessage(new Date() , i++));
            messageSender.expireSend(message, "eason-exchange", "eason_type_expire", 1000);
            messageSender.send(message, "eason-exchange", "eason_type_expire1");
            Thread.sleep(1000 * 10);
        }
    }

    @org.testng.annotations.Test
    public void testExpireSend() throws Exception {

    }

}