package org.haobtc.wallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.thirdgoddess.tnt.viewpager_adapter.ViewPagerFragmentStateAdapter;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.ApplicationObserver;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.adapter.UnbackupKeyAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.bean.MainSweepcodeBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.bean.UpdateInfo;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.FixWalletNameEvent;
import org.haobtc.wallet.event.MainpageWalletEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.fragment.mainwheel.AddViewFragment;
import org.haobtc.wallet.fragment.mainwheel.CheckHideWalletFragment;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.executorService;


public class MainActivity extends BaseActivity implements View.OnClickListener, OnRefreshListener, OnButtonClickListener, OnDownloadListener {

    @BindView(R.id.recl_un_backup)
    RecyclerView reclUnBackup;
    private ViewPager viewPager;
    SharedPreferences sharedPreferences;
    public static Python py;
    private RecyclerView recy_data;
    //remeber first back time
    private long firstTime = 0;
    private TextView tetNone;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private String date;
    private boolean is_mine;
    private JSONArray jsonArray;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private List<Fragment> fragmentList;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private String strNames;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<AddressEvent> walletnameList;
    private String strType;
    private int scrollPos = 0;//scrollPos --> recyclerview position != The last one || second to last
    PyObject getWalletsListInfo = null;
    private DownloadManager manager;
    private SharedPreferences.Editor edit;
    private List<HardwareFeatures> deviceValue;
    private MyDialog myDialog;
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    public static boolean isBacked;

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(MainActivity.this);
        //Eventbus register
        EventBus.getDefault().register(this);
        sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = sharedPreferences.edit();

        //FIRST_RUN,if frist run
        String firstRun = "is_first_run";
        edit.putBoolean(firstRun, true);
        edit.apply();
        init();
    }

    private void init() {
        boolean userAgreement = sharedPreferences.getBoolean("user_agreement", false);
        if (!userAgreement) {
            //User agreement dialog
            userAgreementDialog();
        }
        rxPermissions = new RxPermissions(this);
        ImageView imageViewSweep = findViewById(R.id.img_sweep);
        TextView btnAddmoney = findViewById(R.id.tet_Addmoney);
        viewPager = findViewById(R.id.viewPager);
        recy_data = findViewById(R.id.recy_data);
        ImageView imageViewSetting = findViewById(R.id.img_setting);
        TextView textView = findViewById(R.id.textView_more);
        tetNone = findViewById(R.id.tet_None);
        refreshLayout = findViewById(R.id.smart_RefreshLayout);
        imageViewSweep.setOnClickListener(this);
        imageViewSetting.setOnClickListener(this);
        textView.setOnClickListener(this);
        btnAddmoney.setOnClickListener(this);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(this);

        //wallet name and balance list
        walletnameList = new ArrayList<>();
    }

    @Override
    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
        fragmentList = new ArrayList<>();
        //Binder Adapter
        trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
        recy_data.setAdapter(trsactionlistAdapter);
        //Rolling Wallet
        mWheelplanting();
        if (ApplicationObserver.tryUpdate) {
            ApplicationObserver.tryUpdate = false;
            getUpdateInfo();
        }

    }

    private void mWheelplanting() {
        walletnameList.clear();
        fragmentList.clear();
        //wallet list
        if (Daemon.commands == null) {
            finishAndRemoveTask();
            System.exit(0);
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //wallet list
                try {
                    getWalletsListInfo = Daemon.commands.callAttr("list_wallets");
                } catch (Exception e) {
                    e.printStackTrace();
                    addwalletFragment();
                    return;
                }
                mainhandler.sendEmptyMessage(1);
            }
        });
    }

    private void showWalletList() {
        if (getWalletsListInfo != null && getWalletsListInfo.size() != 0) {
            String toStrings = getWalletsListInfo.toString();
            Log.i("mWheelplanting", "toStrings: " + toStrings);
            if (toStrings.length() != 2) {
                com.alibaba.fastjson.JSONArray jsons = com.alibaba.fastjson.JSONObject.parseArray(toStrings);
                for (int i = 0; i < jsons.size(); i++) {
                    Map jsonToMap = (Map) jsons.get(i);
                    Set keySets = jsonToMap.keySet();
                    Iterator ki = keySets.iterator();
                    AddressEvent addressEvent = new AddressEvent();
                    while (ki.hasNext()) {
                        //get key
                        String key = (String) ki.next();
                        String value = jsonToMap.get(key).toString();
                        addressEvent.setName(key);
                        addressEvent.setType(value);
                        walletnameList.add(addressEvent);
                    }
                }
                if (walletnameList != null && walletnameList.size() != 0) {
                    strNames = walletnameList.get(0).getName();
                    strType = walletnameList.get(0).getType();
                    for (int i = 0; i < walletnameList.size(); i++) {
                        String name = walletnameList.get(i).getName();
                        String walletType = walletnameList.get(i).getType();
                        if (i == 0) {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType, true));
                        } else {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType));
                        }
                    }
                    fragmentList.add(new AddViewFragment());
                    fragmentList.add(new CheckHideWalletFragment());
                    viewPager.setOffscreenPageLimit(4);
                    viewPager.setPageMargin(40);
                    viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));

                } else {
                    addwalletFragment();
                }
            } else {
                addwalletFragment();
            }
        }
        //scroll
        viewPagerScroll(walletnameList.size());
    }

    //no wallet show fragment
    private void addwalletFragment() {
        reclUnBackup.setVisibility(View.GONE);
        fragmentList.add(new AddViewFragment());
        fragmentList.add(new CheckHideWalletFragment());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageMargin(40);
        viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
        //trsaction list data
        tetNone.setText(getString(R.string.no_records));
        tetNone.setVisibility(View.VISIBLE);
        recy_data.setVisibility(View.GONE);
    }

    //viewPagerScroll
    private void viewPagerScroll(int size) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                scrollPos = position;
                if (position == (fragmentList.size() - 1) || position == (fragmentList.size() - 2)) {
                    refreshLayout.setEnableRefresh(false);
                    reclUnBackup.setVisibility(View.GONE);
                    if (position == (fragmentList.size() - 1)) {
                        tetNone.setText(getString(R.string.hide_wallet_tips));
                        tetNone.setVisibility(View.VISIBLE);
                        recy_data.setVisibility(View.GONE);
                    } else {
                        tetNone.setText(getString(R.string.no_records));
                        tetNone.setVisibility(View.VISIBLE);
                        recy_data.setVisibility(View.GONE);
                    }

                } else {
                    refreshLayout.setEnableRefresh(true);
                    strNames = walletnameList.get(position).getName();
                    strType = walletnameList.get(position).getType();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //refresh only wallet
                            ((WheelViewpagerFragment) fragmentList.get(position)).refreshList();
                        }
                    }, 500);
                    if (!"standard".equals(strType)) {
                        //get unBackupKey
                        getunBackupKey();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void downMainListdata() {
        maintrsactionlistEvents.clear();
        PyObject getHistoryTx = null;
        try {
            //get transaction json
            getHistoryTx = Daemon.commands.callAttr("get_all_tx_list");
        } catch (Exception e) {
            e.printStackTrace();
            mToast(getString(R.string.switch_server));
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
//            Log.i("downMainListdata", "downMaina===: " + e.getMessage());
            return;
        }
        //get transaction list
        if (getHistoryTx != null) {
            tetNone.setVisibility(View.GONE);
            recy_data.setVisibility(View.VISIBLE);
            String strHistory = getHistoryTx.toString();
            // Log.i("strHistory", "onPage----: " + strHistory);
            refreshLayout.finishRefresh();
            if (strHistory.length() == 2) {
                tetNone.setText(getString(R.string.no_records));
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            } else {
                //show trsaction list
                showTrsactionlist(strHistory);
            }
        } else {
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
        }
        trsactionlistAdapter.notifyDataSetChanged();

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        maintrsactionlistEvents.clear();
        try {
            jsonArray = new JSONArray(strHistory);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MaintrsactionlistEvent maintrsactionlistEvent = new MaintrsactionlistEvent();
                String type = jsonObject.getString("type");
                String txHash = jsonObject.getString("tx_hash");
                String amount = jsonObject.getString("amount");
                //false ->get   true ->push
                is_mine = jsonObject.getBoolean("is_mine");
                date = jsonObject.getString("date");
                String txStatus = jsonObject.getString("tx_status");
                if ("history".equals(type)) {
                    String confirmations = jsonObject.getString("confirmations");
                    //add attribute
                    maintrsactionlistEvent.setTxHash(txHash);
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setMine(is_mine);
                    maintrsactionlistEvent.setConfirmations(confirmations);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTxStatus(txStatus);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                } else {
                    String invoiceId = jsonObject.getString("invoice_id");//delete use
                    //add attribute
                    maintrsactionlistEvent.setTxHash(txHash);
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setMine(is_mine);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTxStatus(txStatus);
                    maintrsactionlistEvent.setInvoiceId(invoiceId);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                }
            }
            trsactionlistAdapter.notifyDataSetChanged();
            trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                private String txHash1;
                private boolean status = false;

                @SingleClick
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    String typeDele = maintrsactionlistEvents.get(position).getType();
                    switch (view.getId()) {
                        case R.id.lin_Item:
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(position);
                                txHash1 = jsonObject.getString("tx_hash");
                                is_mine = jsonObject.getBoolean("is_mine");
                                date = jsonObject.getString("date");
                                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                                if ("tx".equals(typeDele)) {
                                    String txOnclick = jsonObject.getString("tx");
                                    intent.putExtra("keyValue", "B");
                                    intent.putExtra("tx_hash", txHash1);
                                    intent.putExtra("is_mine", is_mine);
                                    intent.putExtra("strwalletType", strType);
                                    intent.putExtra("listType", typeDele);
                                    intent.putExtra("dataTime", date);
                                    intent.putExtra("txCreatTrsaction", txOnclick);
                                    startActivity(intent);

                                } else {
                                    intent.putExtra("tx_hash", txHash1);
                                    intent.putExtra("is_mine", is_mine);
                                    intent.putExtra("dataTime", date);
                                    intent.putExtra("strwalletType", strType);
                                    intent.putExtra("keyValue", "B");
                                    intent.putExtra("listType", typeDele);
                                    startActivity(intent);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case R.id.txt_delete:
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(position);
                                txHash1 = jsonObject.getString("tx_hash");
                                Log.i("onItemChildClick", "onItemCh==== " + txHash1);
                                status = Daemon.commands.callAttr("get_remove_flag", txHash1).toBoolean();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (status) {
                                try {
                                    Daemon.commands.callAttr("remove_local_tx", txHash1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (status) {
                                    try {
                                        Daemon.commands.callAttr("remove_local_tx", txHash1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mToast(getString(R.string.delete_fail));
                                        return;
                                    }
                                    maintrsactionlistEvents.remove(position);
                                }
                            }

                            break;
                        default:
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getunBackupKey() {
        ArrayList<String> unBackupKeyList = new ArrayList<>();
        List<HardwareFeatures> deviceValue = new ArrayList<>();
        isBacked = true;
        SharedPreferences devices = getSharedPreferences("devices", MODE_PRIVATE);
        Map<String, ?> devicesAll = devices.getAll();
        //key
        for (Object info : devicesAll.values()) {
            HardwareFeatures hardwareFeatures = HardwareFeatures.objectFromData((String) info);
            if (hardwareFeatures.isNeedsBackup()) {
                deviceValue.add(hardwareFeatures);
            }
        }
        if (!deviceValue.isEmpty()) {
            try {
                String strDeviceId = Daemon.commands.callAttr("get_device_info").toString().replaceAll("\"", "");
                if (!Strings.isNullOrEmpty(strDeviceId)) {
                    for (HardwareFeatures entity : deviceValue) {
                        if (strDeviceId.equals(entity.getDeviceId())) {
                            unBackupKeyList.add(Optional.ofNullable(entity.getLabel()).orElse(entity.getBleName()));
                        }
                    }
                }
                reclUnBackup.setLayoutManager(new LinearLayoutManager(this));
                if (!unBackupKeyList.isEmpty()) {
                    isBacked = false;
                    reclUnBackup.setVisibility(View.VISIBLE);
                    UnbackupKeyAdapter unbackupKeyAdapter = new UnbackupKeyAdapter(unBackupKeyList);
                    reclUnBackup.setAdapter(unbackupKeyAdapter);
                    unbackupKeyAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                        if (view.getId() == R.id.test_go_to_backup) {
                            CommunicationModeSelector.backupTip = true;
                            Intent intent = new Intent(MainActivity.this, BackupRecoveryActivity.class);
                            intent.putExtra("home_un_backup", "home_un_backup");
                            startActivity(intent);
                        }
                    });
                } else {
                    reclUnBackup.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                reclUnBackup.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            reclUnBackup.setVisibility(View.GONE);
        }

    }

    /**
     * set white background and black text
     */
    @Override
    public void mInitState() {
        ImmersionBar.with(this).keyboardEnable(false).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.button_bk_ddake).init();
    }

    @SingleClick(value = 1000)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_sweep:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //If you have already authorized it, you can directly jump to the QR code scanning interface
                                Intent intent2 = new Intent(this, CaptureActivity.class);
                                startActivityForResult(intent2, REQUEST_CODE);
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.photopersion, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.img_setting:
                Intent intent1 = new Intent(this, SettingActivity.class);
                startActivity(intent1);
                break;
            case R.id.textView_more:
                Intent intent2 = new Intent(this, TransactionRecordsActivity.class);
                intent2.putExtra("strwalletType", strType);
                startActivity(intent2);
                break;
            case R.id.tet_Addmoney:
                Intent intent6 = new Intent(MainActivity.this, CreateWalletActivity.class);
                startActivity(intent6);
                break;
            default:
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        switch (msgVote) {
            case "11":
                //Rolling Wallet
                mWheelplanting();
                break;
            case "22":
                if (scrollPos != (fragmentList.size() - 1) && scrollPos != (fragmentList.size() - 2)) {//scrollPos --> recyclerview position != The last one || second to last
                    maintrsactionlistEvents.clear();
                    //transaction list data
                    downMainListdata();
                    trsactionlistAdapter.notifyDataSetChanged();
                }
                break;
            case "33":
                tetNone.setText(getString(R.string.no_records));
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
                break;
            case "load_wallet_finish":
                if (!"standard".equals(strType)) {
                    //get unBackupKey
                    getunBackupKey();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + msgVote);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (!TextUtils.isEmpty(msgVote) && msgVote.length() != 2 && msgVote.contains("{")) {
            //Rolling Wallet
            if (fragmentList != null && fragmentList.size() > scrollPos) {
                if (hasWindowFocus()) {
                    Optional.ofNullable(fragmentList.get(scrollPos)).ifPresent(fragment -> ((WheelViewpagerFragment) fragment).setValue(msgVote));
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void event(MainpageWalletEvent updataHint) {
        String status = updataHint.getStatus();
        if ("22".equals(status)) {
            int walletPos = updataHint.getPos();
            viewPager.setCurrentItem(walletPos);
        }
    }

    private void getUpdateInfo() {
        // version_testnet.json version_regtest.json
        String appId = BuildConfig.APPLICATION_ID;
        String urlPrefix = "https://key.bixin.com/";
        String url = "";
        if (appId.endsWith("mainnet")) {
            url = urlPrefix + "version.json";
        } else if (appId.endsWith("testnet")) {
            url = urlPrefix + "version_testnet.json";
        } else if (appId.endsWith("regnet")) {
            url = urlPrefix + "version_regtest.json";
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        Log.d("Main", "正在检查更新信息");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                Log.e("Main", "获取更新信息失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
                String locate = preferences.getString("language", "");
                String info = response.body().string();
                UpdateInfo updateInfo = UpdateInfo.objectFromData(info);
                String oldInfo = preferences.getString("upgrade_info", null);
                Optional.ofNullable(oldInfo).ifPresent((s) -> {
                    UpdateInfo old = UpdateInfo.objectFromData(s);
                    if (!old.getStm32().getUrl().equals(updateInfo.getStm32().getUrl())) {
                        updateInfo.getStm32().setNeedUpload(true);
                    }
                    if (!old.getNrf().getUrl().equals(updateInfo.getNrf().getUrl())) {
                        updateInfo.getNrf().setNeedUpload(true);
                    }
                });
                preferences.edit().putString("upgrade_info", updateInfo.toString()).apply();
                String url = updateInfo.getAPK().getUrl();
                String versionName = updateInfo.getAPK().getVersionName();
                int versionCode = updateInfo.getAPK().getVersionCode();
                String size = updateInfo.getAPK().getSize().replace("M", "");
                String description = "English".equals(locate) ? updateInfo.getAPK().getChangelogEn() : updateInfo.getAPK().getChangelogCn();
                runOnUiThread(() -> attemptUpdate(url, versionName, versionCode, size, description));
            }
        });
    }

    private void attemptUpdate(String uri, String versionName, int versionCode, String
            size, String description) {
        String url = "https://key.bixin.com/" + uri;
        UpdateConfiguration configuration = new UpdateConfiguration()
                .setEnableLog(true)
                //.setHttpManager()
                .setJumpInstallPage(true)
                .setDialogButtonTextColor(Color.WHITE)
                .setDialogButtonColor(getColor(R.color.button_bk))
                .setDialogImage(R.drawable.update)
                .setShowNotification(true)
                .setShowBgdToast(true)
                .setForcedUpgrade(false)
                .setButtonClickListener(this)
                .setOnDownloadListener(this);

        manager = DownloadManager.getInstance(this);
        manager.setApkName("BixinKEY.apk")
                .setApkUrl(url)
                .setSmallIcon(R.drawable.app_icon)
                .setShowNewerToast(false)
                .setConfiguration(configuration)
                .setApkVersionCode(versionCode)
                .setApkVersionName(versionName)
                .setApkSize(size)
                .setApkDescription(description)
                .download();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.release();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
//                Log.i("PyObject", "parse_qr-----:  " + data);
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                //bitcoin:mhZ5dTc91TxttEvFJifBNPNqwLAD5CxhYF
                if (!TextUtils.isEmpty(content)) {
                    PyObject parseQr;
                    try {
                        parseQr = Daemon.commands.callAttr("parse_pr", content);

                    } catch (Exception e) {
                        e.printStackTrace();
                        mToast(getString(R.string.address_wrong));
//                        Log.i("PyObject", "parse_qr+++++++:  " + e.getMessage());
                        return;
                    }
                    if (parseQr != null) {
                        String strParse = parseQr.toString();
                        Log.i("PyObject", "parse_qr:  " + strParse);
                        try {
                            JSONObject jsonObject = new JSONObject(strParse);
                            int type = jsonObject.getInt("type");
                            Gson gson = new Gson();
                            if (type == 1) {
                                MainSweepcodeBean mainSweepcodeBean = gson.fromJson(strParse, MainSweepcodeBean.class);
                                MainSweepcodeBean.DataBean listData = mainSweepcodeBean.getData();
                                String address = listData.getAddress();
                                String sendAmount = listData.getAmount();
                                String message = listData.getMessage();
                                //address  -->  intent  send  activity
                                Intent intent = new Intent(MainActivity.this, SendOne2OneMainPageActivity.class);
                                intent.putExtra("sendAdress", address);
                                intent.putExtra("sendamount", sendAmount);
                                intent.putExtra("sendmessage", message);
                                intent.putExtra("wallet_name", strNames);
                                startActivity(intent);
                            } else if (type == 2) {
                                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                                intent.putExtra("strParse", strParse);
                                intent.putExtra("keyValue", "B");
                                intent.putExtra("listType", "scan");
                                intent.putExtra("strwalletType", strType);
                                startActivity(intent);
                            } else {
                                mToast(getString(R.string.address_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mToast(getString(R.string.address_wrong));
                        }
                    }
                }
            }
        }
    }

    private void userAgreementDialog() {
        View view1 = LayoutInflater.from(this).inflate(R.layout.user_agreement, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        view1.findViewById(R.id.btn_agree).setOnClickListener(v -> {
            edit.putBoolean("user_agreement", true);
            edit.apply();
            alertDialog.dismiss();
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * onclick dowble exit
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(MainActivity.this, R.string.dowbke_to_exit, Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fixName(FixWalletNameEvent event) {
        //Rolling Wallet
        mWheelplanting();
    }


    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //trsaction list data
        downMainListdata();
    }

    @SuppressLint("HandlerLeak")
    Handler mainhandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                showWalletList();
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        getunBackupKey();
    }

    @Override
    public void onButtonClick(int id) {

    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {

    }

    @Override
    public void done(File apk) {
        manager.release();
    }

    @Override
    public void cancel() {
        manager.release();
    }

    @Override
    public void error(Exception e) {
        manager.release();
    }
}