package com.mdiaf.notify.store;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.message.ObjectMessage;
import org.testng.Assert;

import java.util.List;

/**
 * Created by Eason on 15/11/16.
 */
public class MessageStoreTest {

    private IMessageStore messageStore;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        messageStore = MessageStoreManager.getOrCreate(configuration, "test");
        messageStore.deleteByUniqueId("1");
    }

    @org.testng.annotations.Test
    public void testSave() throws Exception {
        String msg = "hello world";
        IMessage message = new ObjectMessage(msg);
        message.getHeader().setUniqueId("1");
        messageStore.saveOrUpdate(message);
        messageStore.saveOrUpdate(message);

        List<IMessage> list = messageStore.findMomentBefore(0);
        Assert.assertEquals(list.size(), 1);
        IMessage message1 = list.get(0);
        Assert.assertEquals(message1.getBody().toString(), msg);

        messageStore.deleteByUniqueId(message.getHeader().getUniqueId());
        List<IMessage> list1 = messageStore.findMomentBefore(0);
        Assert.assertEquals(list1.size(), 0);
    }

    @org.testng.annotations.Test
    public void testDelete() throws Exception {

    }

    @org.testng.annotations.Test
    public void testFindALL() throws Exception {

    }

    @org.testng.annotations.Test
    public void testGetTableName() throws Exception {

    }

}