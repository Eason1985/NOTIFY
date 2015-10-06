package com.mdiaf.notify.listener;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Eason on 15/10/5.
 */
public class TestMessage implements Serializable{

    private static final long serialVersionUID = 4952424319921000141L;
    private Date time;

    public TestMessage(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }
}
