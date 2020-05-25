package org.haobtc.wallet.asynctask;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.chaquo.python.Kwarg;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.event.OperationTimeoutEvent;
import org.haobtc.wallet.utils.Daemon;

public class BusinessAsyncTask extends AsyncTask<String, Void, String> {
    private Helper helper;
    private final static String TAG = BusinessAsyncTask.class.getSimpleName();
    public final static String GET_EXTEND_PUBLIC_KEY = "get_xpub_from_hw";
    public final static String GET_EXTEND_PUBLIC_KEY_SINGLE = "get_xpub_from_hw_single";
    public final static String SIGN_TX = "sign_tx";
    public static final String BACK_UP = "backup_wallet";
    public static final String RECOVER = "recovery_wallet";
    public final static String CHANGE_PIN = "reset_pin";
    public final static String WIPE_DEVICE = "wipe_device";
    public static final String SIGN_MESSAGE = "sign_message";
    public static final String INIT_DEVICE = "init";
    public static final String COUNTER_VERIFICATION = "hardware_verify";
    public static final String APPLY_SETTING = "apply_setting";

    public BusinessAsyncTask setHelper(Helper helper) {
        this.helper = helper;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        helper.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        System.out.println(String.format("method==%s===in thread===%d", strings[0], Thread.currentThread().getId()));
        String result = "";
        switch (strings[0]) {
            case GET_EXTEND_PUBLIC_KEY_SINGLE:
            case RECOVER:
            case SIGN_TX:
            case COUNTER_VERIFICATION:
                try {
                    result = Daemon.commands.callAttr(strings[0].endsWith("single") ? GET_EXTEND_PUBLIC_KEY : strings[0], strings[1], strings[2]).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
            case SIGN_MESSAGE:
                try {
                    result = Daemon.commands.callAttr(strings[0], strings[1], strings[2], strings[3]).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
            case GET_EXTEND_PUBLIC_KEY:
            case CHANGE_PIN:
            case WIPE_DEVICE:
            case BACK_UP:
                try {
                    result = Daemon.commands.callAttr(strings[0], strings[1]).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
            case INIT_DEVICE:
                try {
                    result = Daemon.commands.callAttr(strings[0], strings[1], strings[2], strings[3], strings[4]).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
            case APPLY_SETTING:
                try {
                    result = Daemon.commands.callAttr(strings[0], strings[1], strings[2].equals("one") ? new Kwarg("use_ble", true) : new Kwarg("use_ble", false)).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
//            case PAY_APPLY_SETTING:
//                try {
//                    result = Daemon.commands.callAttr(strings[0], strings[1], new Kwarg("fee_pay_money_limit",strings[2]), new Kwarg("fee_pay_times",strings[3]), strings[4].equals("true") ? new Kwarg("fee_pay_pin", true) : new Kwarg("fee_pay_pin", false), strings[5].equals("true") ? new Kwarg("fee_pay_confirm", true) : new Kwarg("fee_pay_confirm", false)).toString();
//                } catch (Exception e) {
//                    cancel(true);
//                    onException(e);
//                }
//                break;
        }
        return result;
    }

    private void onException(Exception e) {
        Log.e(TAG, e.getMessage() == null ? "unknown exception" : e.getMessage());
        if ("BaseException: waiting passphrase timeout".equals(e.getMessage()) || "BaseException: waiting pin timeout".equals(e.getMessage())) {
            EventBus.getDefault().post(new OperationTimeoutEvent());
        } else {
            helper.onException(e);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        helper.onCancelled();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        helper.onResult(s);
    }

    public interface Helper {
        void onPreExecute();

        void onException(Exception e);

        void onResult(String s);

        void onCancelled();
    }

}
