package com.mdiaf.notify.sender;

/**
 * Created by Eason on 15/11/18.
 */
public class Configuration {

    private ReturnListener returnListener;

    private ConfirmListener confirmListener;

    public ConfirmListener getConfirmListener() {
        return confirmListener;
    }

    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public ReturnListener getReturnListener() {
        return returnListener;
    }

    public void setReturnListener(ReturnListener returnListener) {
        this.returnListener = returnListener;
    }
}
