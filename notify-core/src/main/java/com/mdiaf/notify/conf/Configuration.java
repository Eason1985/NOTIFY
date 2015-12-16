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
    private int maxResend = DEFAULT_MAX_RESEND;
    private String url;
    /**
     * resend per time by second unit.
     */
    private final static int DEFAULT_RESEND_PERIOD = 30;
    private final static int DEFAULT_MAX_RESEND = 3;



    public final static int TIMER_DELAY = 3*60*1000;
    public final static int SENDER_TIMER_PERIOD = 60*1000;
    public final static int RECEIVED_TIMER_PERIOD = 60*1000;

    public int getResendPeriod() {
        return DEFAULT_RESEND_PERIOD;
    }

    public int getMaxResend() {
        return maxResend;
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
}
