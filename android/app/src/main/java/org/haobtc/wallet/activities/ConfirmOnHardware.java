package org.haobtc.wallet.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.HardwareAdapter;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.event.SendMoreAddressEvent;
import org.haobtc.wallet.event.SignFailedEvent;
import org.haobtc.wallet.event.SignResultEvent;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfirmOnHardware extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "org.haobtc.wallet.activities.ConfirmOnHardware";
    @BindView(R.id.linBitcoin)
    LinearLayout linBitcoin;
    @BindView(R.id.testConfirmMsg)
    TextView testConfirmMsg;
    private Dialog dialog;
    private View view;
    private ImageView imageViewCancel;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_payAddress)
    TextView tetPayAddress;
    @BindView(R.id.tet_feeNum)
    TextView tetFeeNum;
    @BindView(R.id.recl_Msg)
    RecyclerView reclMsg;
    private TextView signSuccess;

    public int getLayoutId() {
        return R.layout.confirm_on_hardware;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        findViewById(R.id.confirm_on_hardware).setOnClickListener(this);
        findViewById(R.id.img_back).setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initData() {
        ArrayList<SendMoreAddressEvent> addressEventList = new ArrayList<>();
        Bundle bundle = getIntent().getBundleExtra("outputs");
        if (bundle != null) {
            String payAddress = bundle.getString("pay_address");
            String fee = bundle.getString("fee");
            ArrayList<GetnewcreatTrsactionListBean.OutputAddrBean> outputs = (ArrayList<GetnewcreatTrsactionListBean.OutputAddrBean>) bundle.getSerializable("output");
            for (GetnewcreatTrsactionListBean.OutputAddrBean output : outputs) {
                SendMoreAddressEvent sendMoreAddressEvent = new SendMoreAddressEvent();
                String addr = output.getAddr();
                String amount = output.getAmount();
                sendMoreAddressEvent.setInputAddress(addr);
                sendMoreAddressEvent.setInputAmount(amount);
                addressEventList.add(sendMoreAddressEvent);
            }
            Log.i("addressEventList", "-----: " + addressEventList);
            tetPayAddress.setText(payAddress);
            tetFeeNum.setText(fee);
            HardwareAdapter hardwareAdapter = new HardwareAdapter(addressEventList);
            reclMsg.setAdapter(hardwareAdapter);
        } else {
            linBitcoin.setVisibility(View.GONE);
            testConfirmMsg.setText(getString(R.string.confirm_hardware_msg));
        }
    }

    private void showPopupSignProcessing() {
        view = LayoutInflater.from(this).inflate(R.layout.touch_process_popupwindow, null);
        imageViewCancel = view.findViewById(R.id.cancel_touch);
        signSuccess = view.findViewById(R.id.sign_success);
        imageViewCancel.setOnClickListener(this);
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.AnimBottom);
        dialog.show();
    }

    private void showPopupSignFailed() {
        view = LayoutInflater.from(this).inflate(R.layout.signature_fail_popupwindow, null);
        imageViewCancel = view.findViewById(R.id.cancel_sign_fail);
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(view);
        imageViewCancel.setOnClickListener(this);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.AnimBottom);
        dialog.show();
    }

    private void showPopupSignTimeout() {
        view = LayoutInflater.from(this).inflate(R.layout.signature_timeout_popupwindow, null);
        Button button = view.findViewById(R.id.sign_again);
        imageViewCancel = view.findViewById(R.id.cancel_sign_timeout);
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(view);
        imageViewCancel.setOnClickListener(this);
        button.setOnClickListener(this);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.AnimBottom);
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignSuccessful(SignResultEvent resultEvent) {
        if (dialog == null) {
            showPopupSignProcessing();
        }
        Drawable drawableStart = getDrawable(R.drawable.chenggong);
        Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
        signSuccess.setCompoundDrawables(drawableStart, null, null, null);
        String signedRaw = resultEvent.getSignedRaw();
            if (!TextUtils.isEmpty(signedRaw)) {
                Intent intent1 = new Intent(this, TransactionDetailsActivity.class);
                intent1.putExtra(TouchHardwareActivity.FROM, TAG);
                intent1.putExtra("signed_raw_tx", signedRaw);
                startActivity(intent1);
                dialog.dismiss();
                finish();
            }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignFailed(SignFailedEvent failedEvent) {
        if (dialog != null) {
            dialog.dismiss();
        }
        showPopupSignFailed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_again:
                dialog.dismiss();
                finish();
                break;
            case R.id.confirm_on_hardware:
                showPopupSignProcessing();
                break;
            case R.id.img_back:
                finish();
                break;
            default:
                dialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}