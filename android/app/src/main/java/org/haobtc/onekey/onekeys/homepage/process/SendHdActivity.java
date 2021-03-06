package org.haobtc.onekey.onekeys.homepage.process;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.asynctask.BusinessAsyncTask;
import org.haobtc.onekey.bean.CurrentFeeDetails;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.bean.TemporaryTxInfo;
import org.haobtc.onekey.bean.TransactionInfoBean;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.event.ButtonRequestConfirmedEvent;
import org.haobtc.onekey.event.ButtonRequestEvent;
import org.haobtc.onekey.event.ChangePinEvent;
import org.haobtc.onekey.event.CustomizeFeeRateEvent;
import org.haobtc.onekey.event.ExitEvent;
import org.haobtc.onekey.event.GetFeeEvent;
import org.haobtc.onekey.event.GotPassEvent;
import org.haobtc.onekey.event.SecondEvent;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.ui.activity.SoftPassActivity;
import org.haobtc.onekey.ui.activity.VerifyPinActivity;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.dialog.CustomizeFeeDialog;
import org.haobtc.onekey.ui.dialog.TransactionConfirmDialog;
import org.haobtc.onekey.ui.dialog.UnBackupTipDialog;
import org.haobtc.onekey.ui.widget.PointLengthFilter;
import org.haobtc.onekey.utils.ClipboardUtils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import dr.android.utils.LogUtil;

import static org.haobtc.onekey.constant.Constant.CURRENT_CURRENCY_GRAPHIC_SYMBOL;
import static org.haobtc.onekey.constant.Constant.CURRENT_SELECTED_WALLET_TYPE;
import static org.haobtc.onekey.constant.Constant.WALLET_BALANCE;

/**
 * @author liyan
 */
public class SendHdActivity extends BaseActivity implements BusinessAsyncTask.Helper {

    static final int RECOMMENDED_FEE_RATE = 0;
    static final int SLOW_FEE_RATE = 1;
    static final int FAST_FEE_RATE = 2;
    static final int CUSTOMIZE_FEE_RATE = 3;
    private static final int DEFAULT_TX_SIZE = 220;
    @BindView(R.id.edit_amount)
    EditText editAmount;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.checkbox_slow)
    CheckBox checkboxSlow;
    @BindView(R.id.view_slow)
    View viewSlow;
    @BindView(R.id.checkbox_recommend)
    CheckBox checkboxRecommend;
    @BindView(R.id.view_recommend)
    View viewRecommend;
    @BindView(R.id.checkbox_fast)
    CheckBox checkboxFast;
    @BindView(R.id.view_fast)
    View viewFast;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.edit_receiver_address)
    EditText editReceiverAddress;
    @BindView(R.id.paste_address)
    TextView pasteAddress;
    @BindView(R.id.switch_coin_type)
    TextView switchCoinType;
    @BindView(R.id.text_max_amount)
    TextView textMaxAmount;
    @BindView(R.id.text_balance)
    TextView textBalance;
    @BindView(R.id.text_customize_fee_rate)
    TextView textCustomizeFeeRate;
    @BindView(R.id.text_fee_in_btc_0)
    TextView textFeeInBtc0;
    @BindView(R.id.text_fee_in_cash_0)
    TextView textFeeInCash0;
    @BindView(R.id.text_spend_time_0)
    TextView textSpendTime0;
    @BindView(R.id.linear_slow)
    RelativeLayout linearSlow;
    @BindView(R.id.text_fee_in_btc_1)
    TextView textFeeInBtc1;
    @BindView(R.id.text_fee_in_cash_1)
    TextView textFeeInCash1;
    @BindView(R.id.text_spend_time_1)
    TextView textSpendTime1;
    @BindView(R.id.linear_recommend)
    RelativeLayout linearRecommend;
    @BindView(R.id.text_fee_in_btc_2)
    TextView textFeeInBtc2;
    @BindView(R.id.text_fee_in_cash_2)
    TextView textFeeInCash2;
    @BindView(R.id.text_spend_time_2)
    TextView textSpendTime2;
    @BindView(R.id.linear_fast)
    RelativeLayout linearFast;
    @BindView(R.id.linear_rate_selector)
    LinearLayout linearRateSelector;
    @BindView(R.id.checkbox_custom)
    CheckBox checkboxCustom;
    @BindView(R.id.text_fee_customize_in_btc)
    TextView textFeeCustomizeInBtc;
    @BindView(R.id.text_fee_customize_in_cash)
    TextView textFeeCustomizeInCash;
    @BindView(R.id.text_customize_spend_time)
    TextView textCustomizeSpendTime;
    @BindView(R.id.text_rollback)
    TextView textRollback;
    @BindView(R.id.linear_customize)
    LinearLayout linearCustomize;
    private int screenHeight;
    private boolean mIsSoftKeyboardShowing;
    private SharedPreferences preferences;
    private String hdWalletName;
    private String baseUnit;
    private String currencySymbols;
    private String showWalletType;
    private String signedTx;
    private String rawTx;
    private TransactionConfirmDialog confirmDialog;
    private CurrentFeeDetails currentFeeDetails;
    private String currentTempTransaction;
    private String tempFastTransaction;
    private String tempSlowTransaction;
    private String tempRecommendTransaction;
    private int transactionSize;
    private double currentFeeRate;
    private double previousFeeRate;
    private BigDecimal minAmount;
    private BigDecimal decimalBalance;
    private int scale;
    private CustomizeFeeDialog feeDialog;
    private boolean addressInvalid;
    private String amount;
    private boolean isFeeValid;
    private String amounts;
    private int customSize;
    private boolean isCustom;
    private boolean isSetBig;
    private String balance;
    private BigDecimal maxAmount;
    // 保存当前的选中位置，默认是推荐
    private int selectFlag = 0;
    private boolean isResume;

    /**
     * init
     */
    @Override
    public void init() {
        hdWalletName = getIntent().getStringExtra("hdWalletName");
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        balance = getIntent().getStringExtra(WALLET_BALANCE);
        if (!Strings.isNullOrEmpty(balance)) {
            decimalBalance = BigDecimal.valueOf(Double.parseDouble(balance));
        }
        showWalletType = preferences.getString(CURRENT_SELECTED_WALLET_TYPE, "");
        baseUnit = preferences.getString("base_unit", "");
        currencySymbols = preferences.getString(CURRENT_CURRENCY_GRAPHIC_SYMBOL, "¥");
        getDefaultFee();
        setMinAmount();
        editAmount.setFilters(new InputFilter[]{new PointLengthFilter(8, new PointLengthFilter.onMaxListener() {
            @Override
            public void onMax (int maxNum) {
                showToast(R.string.accuracy_num);
            }
        })});
        String addressScan = getIntent().getStringExtra("addressScan");
        if (!TextUtils.isEmpty(addressScan)) {
            editReceiverAddress.setText(addressScan);
        } else {
            //whether backup
            try {
                boolean isBackup = PyEnv.hasBackup(getActivity());
                if (!isBackup) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.UN_BACKUP_TIP, getString(R.string.receive_unbackup_tip));
                    UnBackupTipDialog unBackupTipDialog = new UnBackupTipDialog();
                    unBackupTipDialog.setArguments(bundle);
                    unBackupTipDialog.show(getSupportFragmentManager(), "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        textBalance.setText(String.format("%s%s", balance, preferences.getString("base_unit", "")));
        registerLayoutChangeListener();
    }

    /***
     * init layout
     * @return
     */
    @Override
    public int getContentViewId() {
        return R.layout.activity_send_hd;
    }

    private void setMinAmount() {
        switch (baseUnit) {
            case Constant.BTC_UNIT_BTC:
                minAmount = BigDecimal.valueOf(0.00000546);
                scale = 8;
                break;
            case Constant.BTC_UNIT_M_BTC:
                minAmount = BigDecimal.valueOf(0.00546);
                scale = 5;
                break;
            case Constant.BTC_UNIT_M_BITS:
                minAmount = BigDecimal.valueOf(5.46);
                scale = 2;
                break;
        }
    }

    @OnClick({R.id.img_back, R.id.switch_coin_type, R.id.text_max_amount, R.id.text_customize_fee_rate, R.id.linear_slow, R.id.linear_recommend, R.id.linear_fast, R.id.text_rollback, R.id.btn_next, R.id.paste_address})
    @SingleClick
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.switch_coin_type:
                // not support
                break;
            case R.id.text_max_amount:
                if (Strings.isNullOrEmpty(editReceiverAddress.getText().toString())) {
                    showToast(R.string.input_number);
                } else {
                    isSetBig = true;
                    // 点击最大之后也需要刷新当前
                    calculateMaxSpendableAmount();
                }
                break;
            case R.id.text_customize_fee_rate:
                if (currentFeeDetails.getSlow() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putDouble(Constant.CUSTOMIZE_FEE_RATE_MIN, currentFeeDetails.getSlow().getFeerate());
                    bundle.putDouble(Constant.CUSTOMIZE_FEE_RATE_MAX, currentFeeDetails.getFast().getFeerate() * 20);
                    bundle.putInt(Constant.TAG_TX_SIZE, transactionSize);
                    feeDialog = new CustomizeFeeDialog();
                    feeDialog.setArguments(bundle);
                    feeDialog.show(getSupportFragmentManager(), "customize_fee");
                }
                break;
            case R.id.linear_slow:
                viewSlow.setVisibility(View.VISIBLE);
                viewRecommend.setVisibility(View.GONE);
                viewFast.setVisibility(View.GONE);
                checkboxSlow.setVisibility(View.VISIBLE);
                checkboxRecommend.setVisibility(View.GONE);
                checkboxFast.setVisibility(View.GONE);
                currentFeeRate = currentFeeDetails.getSlow().getFeerate();
                currentTempTransaction = tempSlowTransaction;
                selectFlag = SLOW_FEE_RATE;
                calculateMaxSpendableAmount();
                break;
            case R.id.linear_recommend:
                viewSlow.setVisibility(View.GONE);
                viewRecommend.setVisibility(View.VISIBLE);
                viewFast.setVisibility(View.GONE);
                checkboxSlow.setVisibility(View.GONE);
                checkboxRecommend.setVisibility(View.VISIBLE);
                checkboxFast.setVisibility(View.GONE);
                currentFeeRate = currentFeeDetails.getNormal().getFeerate();
                currentTempTransaction = tempRecommendTransaction;
                selectFlag = RECOMMENDED_FEE_RATE;
                calculateMaxSpendableAmount();
                break;
            case R.id.linear_fast:
                viewSlow.setVisibility(View.GONE);
                viewRecommend.setVisibility(View.GONE);
                viewFast.setVisibility(View.VISIBLE);
                checkboxSlow.setVisibility(View.GONE);
                checkboxRecommend.setVisibility(View.GONE);
                checkboxFast.setVisibility(View.VISIBLE);
                currentFeeRate = currentFeeDetails.getFast().getFeerate();
                currentTempTransaction = tempFastTransaction;
                selectFlag = FAST_FEE_RATE;
                calculateMaxSpendableAmount();
                break;
            case R.id.text_rollback:
                turnOffCustomize();
                break;
            case R.id.paste_address:
                editReceiverAddress.setText(ClipboardUtils.pasteText(this));
                getAddressIsValid();
                if (!mIsSoftKeyboardShowing) {
                    keyBoardHideRefresh();
                }
                break;
            case R.id.btn_next:
                send();
                break;
            default:
                break;
        }
    }

    /**
     * 取消自定义费率
     */
    private void turnOffCustomize() {
        isCustom = false;
        linearRateSelector.setVisibility(View.VISIBLE);
        linearCustomize.setVisibility(View.GONE);
        currentFeeRate = previousFeeRate;
        calculateMaxSpendableAmount();
    }

    /**
     * 判断地址和金额是否正确填写完毕
     */
    private boolean sendReady() {
        if (Strings.isNullOrEmpty(editReceiverAddress.getText().toString())) {
            showToast(R.string.input_number);
            return false;
        }
        if (Strings.isNullOrEmpty(editAmount.getText().toString())) {
            showToast(R.string.input_out_number);
            return false;
        }
        if (Double.parseDouble(amount) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取费率详情
     */
    private void getDefaultFee() {
        try {
            PyResponse<String> response = PyEnv.getFeeInfo();
            String errors = response.getErrors();
            if (Strings.isNullOrEmpty(errors)) {
                currentFeeDetails = CurrentFeeDetails.objectFromDate(response.getResult());
                transactionSize = currentFeeDetails.getFast().getSize();
                LogUtil.d(" 获取详情-->" + JSON.toJSONString(currentFeeDetails));
                initFeeSelectorStatus();
            } else {
                showToast(R.string.get_fee_info_failed);
            }
            currentFeeRate = currentFeeDetails.getFast().getFeerate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化三种等级手续费的默认视图
     */
    private void initFeeSelectorStatus() {
        try {
            textSpendTime0.setText(String.format("%s%s%s", getString(R.string.about_), currentFeeDetails == null ? 0 : currentFeeDetails.getSlow().getTime(), getString(R.string.minute)));
            customSize = currentFeeDetails.getSlow().getSize();
            textFeeInBtc0.setText(String.format(Locale.ENGLISH, "%s %s", currentFeeDetails.getSlow().getFee(), baseUnit));
            PyResponse<String> response0 = PyEnv.exchange(currentFeeDetails.getSlow().getFee());
            String errors0 = response0.getErrors();
            if (Strings.isNullOrEmpty(errors0)) {
                textFeeInCash0.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response0.getResult()));
            } else {
                showToast(errors0);
            }
            textSpendTime1.setText(String.format("%s%s%s", getString(R.string.about_), currentFeeDetails == null ? 0 : currentFeeDetails.getNormal().getTime(), getString(R.string.minute)));
            textFeeInBtc1.setText(String.format(Locale.ENGLISH, "%s %s", currentFeeDetails.getNormal().getFee(), baseUnit));
            PyResponse<String> response1 = PyEnv.exchange(currentFeeDetails.getNormal().getFee());
            String errors1 = response1.getErrors();
            if (Strings.isNullOrEmpty(errors1)) {
                textFeeInCash1.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response1.getResult()));
            } else {
//                showToast(errors0);
            }
            textSpendTime2.setText(String.format("%s%s%s", getString(R.string.about_), currentFeeDetails == null ? 0 : currentFeeDetails.getFast().getTime(), getString(R.string.minute)));
            textFeeInBtc2.setText(String.format(Locale.ENGLISH, "%s %s", currentFeeDetails.getFast().getFee(), baseUnit));
            PyResponse<String> response2 = PyEnv.exchange(currentFeeDetails.getFast().getFee());
            String errors2 = response2.getErrors();
            if (Strings.isNullOrEmpty(errors2)) {
                textFeeInCash2.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response2.getResult()));
            } else {
//                showToast(errors0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算最大可用余额
     */

    private void calculateMaxSpendableAmount () {
        PyResponse<TemporaryTxInfo> pyResponse = PyEnv.getFeeByFeeRate(editReceiverAddress.getText().toString(), "!", String.valueOf(currentFeeRate));

        if (Strings.isNullOrEmpty(pyResponse.getErrors())) {
            TemporaryTxInfo temporaryTxInfo = pyResponse.getResult();
            maxAmount = BigDecimal.valueOf(Double.parseDouble(balance)).subtract(BigDecimal.valueOf(temporaryTxInfo.getFee()));
            if (isSetBig) {
                editAmount.setText(maxAmount.toPlainString());
            } else {
                // 不能大于最大值，
                if (!Strings.isNullOrEmpty(amount) && Double.parseDouble(amount) > 0) {
                    BigDecimal decimal = BigDecimal.valueOf(Double.parseDouble(amount));
                    if (decimal.compareTo(maxAmount) >= 0) {
                        editAmount.setText(maxAmount.toPlainString());
                    }
                }
            }
            refreshFeeView();
            editAmount.setFocusable(true);
        }
    }


    /**
     * 发送交易
     */
    private void send() {
        PyResponse<String> response = PyEnv.makeTx(currentTempTransaction);
        String errors = response.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            // sign
            rawTx = response.getResult();
            if (Constant.WALLET_TYPE_HARDWARE.equals(showWalletType)) {
                hardwareSign(rawTx);
            } else {
                sendConfirmDialog(rawTx);
            }
        } else {
            showToast(errors);
        }
    }

    @Subscribe
    public void onGotSoftPass(GotPassEvent event) {
        softSign(event.getPassword());
    }

    /**
     * 软件签名交易
     */
    private void softSign(String password) {
        PyResponse<TransactionInfoBean> pyResponse = PyEnv.signTx(rawTx, password);
        String errorMsg = pyResponse.getErrors();
        if (Strings.isNullOrEmpty(errorMsg)) {
            broadcastTx(pyResponse.getResult().getTx());
        } else {
            showToast(errorMsg);
        }
    }

    /**
     * 广播交易
     */
    private void broadcastTx(String signedTx) {
        PyResponse<Void> response = PyEnv.broadcast(signedTx);
        String errors = response.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            Intent intent = new Intent(SendHdActivity.this, TransactionCompletion.class);
            intent.putExtra("txDetail", signedTx);
            intent.putExtra("amounts", amounts);
            startActivity(intent);
            finish();
        } else {
            showToast(errors);
        }
    }

    /**
     * 弹出交易确认框
     */
    private void sendConfirmDialog(String rawTx) {
        PyResponse<String> response = PyEnv.analysisRawTx(rawTx);
        String errors = response.getErrors();
        if (!Strings.isNullOrEmpty(errors)) {
            showToast(errors);
//            showToast(R.string.transaction_parse_error);
            return;
        }
        TransactionInfoBean info = TransactionInfoBean.objectFromData(response.getResult());
        // set see view
        String sender = info.getInputAddr().get(0).getAddress();
        String receiver = info.getOutputAddr().get(0).getAddr();
        amounts = info.getAmount();
        String fee = info.getFee();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.TRANSACTION_SENDER, sender);
        bundle.putString(Constant.TRANSACTION_RECEIVER, receiver);
        bundle.putString(Constant.TRANSACTION_AMOUNT, amounts);
        bundle.putString(Constant.TRANSACTION_FEE, fee);
        bundle.putString(Constant.WALLET_LABEL, hdWalletName);
        bundle.putInt(Constant.WALLET_TYPE, Constant.WALLET_TYPE_HARDWARE.equals(showWalletType) ?
                Constant.WALLET_TYPE_HARDWARE_PERSONAL : Constant.WALLET_TYPE_SOFTWARE);
        confirmDialog = new TransactionConfirmDialog();
        confirmDialog.setArguments(bundle);
        confirmDialog.show(getSupportFragmentManager(), "confirm");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfirm(ButtonRequestConfirmedEvent event) {
        if (Constant.WALLET_TYPE_HARDWARE.equals(showWalletType)) {
            TransactionInfoBean info = TransactionInfoBean.objectFromData(signedTx);
            broadcastTx(info.getTx());
        } else { // TODO: 2020/12/23
            // 获取主密码
            startActivity(new Intent(this, SoftPassActivity.class));
        }
    }

    private boolean getFee(String feeRate, int type) {
        PyResponse<TemporaryTxInfo> pyResponse = PyEnv.getFeeByFeeRate(editReceiverAddress.getText().toString(), isSetBig ? "!" : amount, feeRate);
        LogUtil.d("请求：", "=========" + amount + "--->" + isSetBig);
        String errors = pyResponse.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            TemporaryTxInfo temporaryTxInfo = pyResponse.getResult();
            String fee = BigDecimal.valueOf(temporaryTxInfo.getFee()).toPlainString();
            int time = temporaryTxInfo.getTime();
            String temp = temporaryTxInfo.getTx();
            transactionSize = temporaryTxInfo.getSize();
            switch (type) {
                case RECOMMENDED_FEE_RATE:
                    textSpendTime1.setText(String.format("%s%s%s", getString(R.string.about_), time + "", getString(R.string.minute)));
                    textFeeInBtc1.setText(String.format(Locale.ENGLISH, "%s %s", fee, baseUnit));
                    PyResponse<String> response1 = PyEnv.exchange(fee);
                    String errors1 = response1.getErrors();
                    if (Strings.isNullOrEmpty(errors1)) {
                        textFeeInCash1.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response1.getResult()));
                    } else {
                        showToast(errors1);
                        return false;
                    }
                    tempRecommendTransaction = temp;
                    if (selectFlag == RECOMMENDED_FEE_RATE) {
                        currentTempTransaction = temp;
                    }
                    return true;
                case SLOW_FEE_RATE:
                    textSpendTime0.setText(String.format("%s%s%s", getString(R.string.about_), time + "", getString(R.string.minute)));
                    textFeeInBtc0.setText(String.format(Locale.ENGLISH, "%s %s", fee, baseUnit));
                    PyResponse<String> response0 = PyEnv.exchange(fee);
                    String errors0 = response0.getErrors();
                    if (Strings.isNullOrEmpty(errors0)) {
                        textFeeInCash0.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response0.getResult()));
                    } else {
//                        showToast(errors0);
                        return false;
                    }
                    if (selectFlag == SLOW_FEE_RATE) {
                        currentTempTransaction = temp;
                    }
                    tempSlowTransaction = temp;
                    return true;
                case FAST_FEE_RATE:
                    textSpendTime2.setText(String.format("%s%s%s", getString(R.string.about_), time + "", getString(R.string.minute)));
                    textFeeInBtc2.setText(String.format(Locale.ENGLISH, "%s %s", fee, baseUnit));
                    PyResponse<String> response2 = PyEnv.exchange(fee);
                    String errors2 = response2.getErrors();
                    if (Strings.isNullOrEmpty(errors2)) {
                        textFeeInCash2.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response2.getResult()));
                    } else {
//                        showToast(errors2);
                        return false;
                    }
                    if (selectFlag == FAST_FEE_RATE) {
                        currentTempTransaction = temp;
                    }
                    tempFastTransaction = temp;
                    return true;
                case CUSTOMIZE_FEE_RATE:
                    textCustomizeSpendTime.setText(String.format("%s%s%s", getString(R.string.about_), time + "", getString(R.string.minute)));
                    textFeeCustomizeInBtc.setText(String.format(Locale.ENGLISH, "%s %s", fee, baseUnit));
                    if (feeDialog.isVisible()) {
                        feeDialog.getTextTime().setText(String.format("%s %s", time, getString(R.string.minute)));
                        feeDialog.getTextFeeInBtc().setText(String.format(Locale.ENGLISH, "%s %s", fee, baseUnit));
                    }
                    PyResponse<String> response3 = PyEnv.exchange(fee);
                    String errors3 = response3.getErrors();
                    if (Strings.isNullOrEmpty(errors3)) {
                        String string = String.format(Locale.ENGLISH, "%s %s", currencySymbols, response3.getResult());
                        textFeeCustomizeInCash.setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response3.getResult()));
                        if (feeDialog.isVisible()) {
                            feeDialog.getTextFeeInCash().setText(String.format(Locale.ENGLISH, "%s %s", currencySymbols, response3.getResult()));
                        }
                    } else {
//                        showToast(errors3);
                        return false;
                    }
                    currentTempTransaction = temp;
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }


    /**
     * 改变发送按钮状态
     */
    private void changeButton() {
        if (addressInvalid && isFeeValid) {
            btnNext.setEnabled(true);
        } else {
            btnNext.setEnabled(false);
        }
    }

    /**
     * 自定义费率确认响应
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomizeFee(CustomizeFeeRateEvent event) {
        isCustom = true;
        linearRateSelector.setVisibility(View.GONE);
        linearCustomize.setVisibility(View.VISIBLE);
        previousFeeRate = currentFeeRate;
        currentFeeRate = Double.parseDouble(event.getFeeRate());
        textFeeCustomizeInBtc.setText(String.format(Locale.ENGLISH, "%s %s", event.getFee(), preferences.getString("base_unit", "")));
        textFeeCustomizeInCash.setText(String.format(Locale.ENGLISH, "%s %s", preferences.getString(CURRENT_CURRENCY_GRAPHIC_SYMBOL, "¥"), event.getCash()));
        textCustomizeSpendTime.setText(String.format("%s%s%s", getString(R.string.about_), event.getTime(), getString(R.string.minute)));
//        previousTempTransaction = currentTempTransaction;
//        currentTempTransaction = tempCustomizeTransaction;
    }

    /**
     * 自定义费率监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onGetFee(GetFeeEvent event) {
        String feeRate = event.getFeeRate();
        isFeeValid = getFee(feeRate, CUSTOMIZE_FEE_RATE);
    }

    /**
     * 注册全局视图监听器
     */
    private void registerLayoutChangeListener() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenHeight = metric.heightPixels;
        mIsSoftKeyboardShowing = false;
        //Determine the size of window visible area
        //If the difference between screen height and window visible area height is greater than 1 / 3 of the whole screen height, it means that the soft keyboard is in display, otherwise, the soft keyboard is hidden.
        // If the status of the soft keyboard was previously displayed, it is now closed, or it was previously closed, it is now displayed, it means that the status of the soft keyboard has changed
        ViewTreeObserver.OnGlobalLayoutListener mLayoutChangeListener = () -> {
            //Determine the size of window visible area
            Rect r = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            //If the difference between screen height and window visible area height is greater than 1 / 3 of the whole screen height, it means that the soft keyboard is in display, otherwise, the soft keyboard is hidden.
            int heightDifference = screenHeight - (r.bottom - r.top);
            boolean isKeyboardShowing = heightDifference > screenHeight / 3;
            // If the status of the soft keyboard was previously displayed, it is now closed, or it was previously closed, it is now displayed, it means that the status of the soft keyboard has changed
            if ((mIsSoftKeyboardShowing && !isKeyboardShowing) || (!mIsSoftKeyboardShowing && isKeyboardShowing)) {
                mIsSoftKeyboardShowing = isKeyboardShowing;
                if (!mIsSoftKeyboardShowing && isResume) {
                    keyBoardHideRefresh();
                }
            }
        };
        // Register layout change monitoring
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mLayoutChangeListener);
    }

    private void keyBoardHideRefresh() {
        if (editAmount.getText().toString().endsWith(".")) {
            amount = editAmount.getText().toString().substring(0, editAmount.getText().toString().length() - 1).trim();
        } else {
            amount = editAmount.getText().toString().trim();
        }
        if (Strings.isNullOrEmpty(editReceiverAddress.getText().toString().trim())) {
            showToast(R.string.input_address);
            btnNext.setEnabled(false);
            return;
        } else {
            // 收起键盘地址认为 focus，所以再次校验地址正确性
            getAddressIsValid();
        }
        if (Strings.isNullOrEmpty(editAmount.getText().toString().trim())) {
            showToast(R.string.inoutnum);
            btnNext.setEnabled(false);
            return;
        } else {
            if (!Strings.isNullOrEmpty(amount) && Double.parseDouble(amount) >= 0) {
                BigDecimal decimal = BigDecimal.valueOf(Double.parseDouble(amount));
                if (decimal.compareTo(minAmount) <= 0) {
                    String min = String.format(Locale.ENGLISH, "%s", minAmount.stripTrailingZeros().toPlainString());
                    editAmount.setText(min);
                    editAmount.setSelection(min.length());
                } else {
                    if (addressInvalid) {
                        calculateMaxSpendableAmount();
                    }
                }
            }
        }
        refreshFeeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResume = false;
    }

    private void getAddressIsValid() {
        String address = editReceiverAddress.getText().toString();
        if (!Strings.isNullOrEmpty(address)) {
            addressInvalid = PyEnv.verifyAddress(address);
            if (!addressInvalid) {
                editReceiverAddress.setText("");
                showToast(R.string.invalid_address);
            } else {
                calculateMaxSpendableAmount();
            }
        }
    }

    /**
     * 交易金额实时监听
     */
    @OnTextChanged(value = R.id.edit_amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChangeAmount(CharSequence sequence) {
        amount = sequence.toString();
        if (!String.valueOf(maxAmount).equals(amount)) {
            isSetBig = false;
        }
        // 金额以点开头
        if (amount.startsWith(".")) {
            editAmount.setText("");
            return;
        }
    }

    @OnFocusChange(value = R.id.edit_receiver_address)
    public void onFocusChanged(boolean focused) {
        if (!focused) {
            getAddressIsValid();
        }
    }

    @OnFocusChange(value = R.id.edit_amount)
    public void onEditAmountFocusChange(boolean focused) {
        if (!focused) {
            if (!Strings.isNullOrEmpty(amount) && Double.parseDouble(amount) > 0) {
                BigDecimal decimal = BigDecimal.valueOf(Double.parseDouble(amount));
                if (decimal.compareTo(minAmount) < 0) {
                    String min = String.format(Locale.ENGLISH, "%s", minAmount.stripTrailingZeros().toPlainString());
                    editAmount.setText(min);
                    editAmount.setSelection(min.length());
                } else if (decimal.compareTo(decimalBalance) >= 0) {
                    if (addressInvalid) {
                        calculateMaxSpendableAmount();
                    }
                }
            }
        }
    }

    /**
     * 获取三种不同费率对应的临时交易
     */
    private void refreshFeeView() {
        isFeeValid = isCanRefresh();
        if (isFeeValid) {
            if (!isCustom) {
                refreshOther();
            }
            changeButton();
        }
    }

    private boolean isCanRefresh() {
        boolean success;
        if (isCustom) {
            return getFee(String.valueOf(currentFeeRate), CUSTOMIZE_FEE_RATE);
        } else {
            if (currentFeeDetails != null) {
                double fast = currentFeeDetails.getFast().getFeerate();
                return getFee(Double.toString(fast), FAST_FEE_RATE);
            }
        }
        return false;
    }

    private void refreshOther() {
        Optional.ofNullable(currentFeeDetails).ifPresent((currentFeeDetails1 -> {
            if (sendReady()) {
                synchronized (SendHdActivity.class) {
                    double normal = currentFeeDetails1.getNormal().getFeerate();
                    getFee(Double.toString(normal), RECOMMENDED_FEE_RATE);
                    double slow = currentFeeDetails1.getSlow().getFeerate();
                    getFee(Double.toString(slow), SLOW_FEE_RATE);
                }
            }
        }));
    }

    /**
     * 硬件签名方法
     */
    private void hardwareSign(String rawTx) {
        new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.SIGN_TX, rawTx, MyApplication.getInstance().getDeviceWay());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangePin(ChangePinEvent event) {
        // 回写PIN码
        PyEnv.setPin(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        switch (event.getType()) {
            case PyConstant.PIN_CURRENT:
                Intent intent = new Intent(this, VerifyPinActivity.class);
                startActivity(intent);
                break;
            case PyConstant.BUTTON_REQUEST_7:
                break;
            case PyConstant.BUTTON_REQUEST_8:
                EventBus.getDefault().post(new ExitEvent());
                sendConfirmDialog(rawTx);
                break;
            default:
        }
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onException(Exception e) {
        showToast(e.getMessage());
    }

    @Override
    public void onResult(String s) {
        if (!Strings.isNullOrEmpty(s)) {
            signedTx = s;
            if (confirmDialog != null) {
                confirmDialog.getBtnConfirmPay().setEnabled(true);
            }
        } else {
            finish();
        }
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void currentMethod(String methodName) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean needEvents() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if ("finish".equals(msgVote)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
