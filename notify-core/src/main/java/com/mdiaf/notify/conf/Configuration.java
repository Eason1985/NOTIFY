package com.mdiaf.notify.conf;

import com.mdiaf.notify.sender.DefaultReturnListener;
import com.mdiaf.notify.sender.IConfirmListener;
import com.mdiaf.notify.sender.IReturnListener;

/**
 * Created by Eason on 15/11/18.
 */
public class Configuration {

    private IReturnListener returnListener;
    private IConfirmListener confirmListener;
    private int maxResend;
    private long timerDelay;
    private String url;
    private String mode;//default is local
    /**
     * resend per time by unit of second .
     */
    private final static int DEFAULT_RESEND_PERIOD = 30;

    private final static int DEFAULT_MAX_RESEND = 3;


    /**
     * make it small when you in test.
     */
    private final static int TIMER_DELAY = 3*60*1000;

    public final static int SENDER_TIMER_PERIOD = 30*1000;
    public final static int RECEIVED_TIMER_PERIOD = 30*1000;

    public int getResendPeriod() {
        return DEFAULT_RESEND_PERIOD;
    }

    public int getMaxResend() {
        if (maxResend > 0) {
            return maxResend;
        }
        return DEFAULT_MAX_RESEND;
    }

    public void setMaxResend(int maxResend) {
        this.maxResend = maxResend;
    }

    public IConfirmListener getConfirmListener() {
        return confirmListener;
    }

    public void setConfirmListener(IConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public IReturnListener getReturnListener() {
        if (returnListener == null) {
            return new DefaultReturnListener();
        }
        return returnListener;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setReturnListener(IReturnListener returnListener) {
        this.returnListener = returnListener;
    }

    public void setTimerDelay(long timerDelay) {
        this.timerDelay = timerDelay;
    }

    public long getTimerDelay() {
        if (timerDelay > 0) {
            return timerDelay;
        }
        return TIMER_DELAY;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
