package com.mdiaf.notify.sender;


import com.mdiaf.notify.BaseTest;
import com.mdiaf.notify.message.StringMessage;
import org.springframework.beans.factory.annotation.Autowired;

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
        while (true){
            StringMessage message = new StringMessage("hello world!");
            messageSender.send(message, "eason-exchange", "eason_test_messageType");

            Thread.sleep(1000*5);
            System.out.println("aaaa");
        }
    }

    @org.testng.annotations.Test
    public void testExpireSend() throws Exception {

    }
}