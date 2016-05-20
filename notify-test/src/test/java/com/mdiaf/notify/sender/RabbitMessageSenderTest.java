package com.mdiaf.notify.sender;


import com.mdiaf.notify.BaseTest;
import com.mdiaf.notify.listener.TestMessage;
import com.mdiaf.notify.message.ObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Eason on 15/10/4.
 */
public class RabbitMessageSenderTest extends BaseTest {

    @Autowired
    private IMessageSender messageSender;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {

    }

    @org.testng.annotations.Test
    public void testSend() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread t = new Thread(new MsgTest(), "msg+" + i);
            t.start();
            threads.add(t);
        }

        for (Thread t : threads) {
            t.join();
        }
//        Thread t1 = new Thread(new MsgTest(), "msg-t1");
//        Thread t2 = new Thread(new MsgTest(), "msg-t2");
//        Thread t3 = new Thread(new MsgTest(), "msg-t3");
//
//
//        t1.start();
//        t2.start();
//        t3.start();
//
//        try
//        {
//            t1.join();
//            t2.join();
//            t3.join();
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
    }

    @org.testng.annotations.Test
    public void testExpireSend() throws Exception {

    }

    public class MsgTest implements Runnable {

        @Override
        public void run() {
            int i = 0;
            while (true) {
                ObjectMessage message = new ObjectMessage(new TestMessage(new Date(), i++));
//                System.err.println("send expire:"+new Date());
                try {
                    messageSender.send(message, "eason-exchange", "eason_type_expire");
//                    Thread.sleep(1000 * 5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            messageSender.send(message, "eason-exchange", "eason_type_expire1");

            }
        }
    }

}