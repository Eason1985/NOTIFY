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

    /**
     * resend per time by second unit.
     */
    private final static int DEFAULT_RESEND_PERIOD = 60;

    private final static int DEFAULT_MAX_RESEND = 3;
    private int maxResend = DEFAULT_MAX_RESEND;

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

    public void setReturnListener(IReturnListener returnListener) {
        this.returnListener = returnListener;
    }
}
