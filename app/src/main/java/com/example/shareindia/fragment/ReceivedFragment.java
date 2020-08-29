//package com.example.shareindia.fragment;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//
//import androidx.annotation.IdRes;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.widget.Toolbar;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentFactory;
//import androidx.fragment.app.FragmentTransaction;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//
//import com.example.shareindia.R;
//import com.example.shareindia.activity.BarcodeScannerActivity;
//import com.example.shareindia.activity.ConnectionManagerActivity;
//import com.example.shareindia.activity.ViewTransferActivity;
//import com.example.shareindia.app.Activity;
//import com.example.shareindia.database.AccessDatabase;
//import com.example.shareindia.dialog.ManualIpAddressConnectionDialog;
//import com.example.shareindia.object.NetworkDevice;
//import com.example.shareindia.service.CommunicationService;
//import com.example.shareindia.ui.UIConnectionUtils;
//import com.example.shareindia.ui.UITask;
//import com.example.shareindia.ui.callback.NetworkDeviceSelectedListener;
//import com.example.shareindia.ui.callback.TitleSupport;
//import com.example.shareindia.ui.help.ConnectionSetUpAssistant;
//import com.example.shareindia.util.AppUtils;
//import com.example.shareindia.util.ConnectionUtils;
//import com.example.shareindia.util.NetworkDeviceLoader;
//import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
//import com.genonbeta.android.framework.util.Interrupter;
//import com.google.android.material.appbar.AppBarLayout;
//import com.google.android.material.appbar.CollapsingToolbarLayout;
//import com.google.android.material.snackbar.Snackbar;
//
//import static android.app.Activity.RESULT_CANCELED;
//import static com.example.shareindia.activity.ConnectionManagerActivity.ACTION_CHANGE_FRAGMENT;
//import static com.example.shareindia.activity.ConnectionManagerActivity.EXTRA_ACTIVITY_SUBTITLE;
//import static com.example.shareindia.activity.ConnectionManagerActivity.EXTRA_FRAGMENT_ENUM;
//
//
//
//public class ReceivedFragment extends Activity
//        implements SnackbarSupport
//{
//
//    public static final String ACTION_CHANGE_FRAGMENT = "com.genonbeta.intent.action.CONNECTION_MANAGER_CHANGE_FRAGMENT";
//    public static final String EXTRA_FRAGMENT_ENUM = "extraFragmentEnum";
//    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
//    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
//    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
//    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
//
//    private final IntentFilter mFilter = new IntentFilter();
//    private HotspotManagerFragment mHotspotManagerFragment;
//    private BarcodeConnectFragment mBarcodeConnectFragment;
//    private NetworkManagerFragment mNetworkManagerFragment;
//    private NetworkDeviceListFragment mDeviceListFragment;
//    private ConnectionManagerActivity.OptionsFragment mOptionsFragment;
//    private AppBarLayout mAppBarLayout;
//    private CollapsingToolbarLayout mToolbarLayout;
//    private ProgressBar mProgressBar;
//    private String mTitleProvided;
//    private ReceivedFragment.RequestType mRequestType = ReceivedFragment.RequestType.RETURN_RESULT;
//    public static final int REQUEST_CODE_CHOOSE_DEVICE = 0;
//
//    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener()
//    {
//        @Override
//        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection)
//        {
//            if (mRequestType.equals(ReceivedFragment.RequestType.RETURN_RESULT)) {
//                setResult(RESULT_OK, new Intent()
//                        .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
//                        .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));
//
//                finish();
//            } else {
//                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(ReceivedFragment.this);
//                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, ReceivedFragment.this);
//
//                UITask uiTask = new UITask()
//                {
//                    @Override
//                    public void updateTaskStarted(Interrupter interrupter)
//                    {
//                        mProgressBar.setVisibility(View.VISIBLE);
//                    }
//
//                    @Override
//                    public void updateTaskStopped()
//                    {
//                        mProgressBar.setVisibility(View.GONE);
//                    }
//                };
//
//                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener()
//                {
//                    @Override
//                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection)
//                    {
//                        createSnackbar(R.string.mesg_completing).show();
//                    }
//                };
//
//                uiConnectionUtils.makeAcquaintance(ReceivedFragment.this, uiTask,
//                        connection.ipAddress, -1, registeredListener);
//            }
//
//            return true;
//        }
//
//        @Override
//        public boolean isListenerEffective()
//        {
//            return true;
//        }
//    };
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
//    {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
//                    && intent.hasExtra(EXTRA_FRAGMENT_ENUM)) {
//                String fragmentEnum = intent.getStringExtra(EXTRA_FRAGMENT_ENUM);
//
//                try {
//                    ConnectionManagerActivity.AvailableFragment value = ConnectionManagerActivity.AvailableFragment.valueOf(fragmentEnum);
//
//                    if (ConnectionManagerActivity.AvailableFragment.EnterIpAddress.equals(value))
//                        showEnterIpAddressDialog();
//                    else
//                        setFragment(value);
//                } catch (Exception e) {
//                    // do nothing
//                }
//            } else if (mRequestType.equals(ConnectionManagerActivity.RequestType.RETURN_RESULT)) {
//                if (CommunicationService.ACTION_DEVICE_ACQUAINTANCE.equals(intent.getAction())
//                        && intent.hasExtra(CommunicationService.EXTRA_DEVICE_ID)
//                        && intent.hasExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME)) {
//                    NetworkDevice device = new NetworkDevice(intent.getStringExtra(CommunicationService.EXTRA_DEVICE_ID));
//                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, intent.getStringExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME));
//
//                    try {
//                        AppUtils.getDatabase(ReceivedFragment.this).reconstruct(device);
//                        AppUtils.getDatabase(ReceivedFragment.this).reconstruct(connection);
//
//                        mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else if (mRequestType.equals(ConnectionManagerActivity.RequestType.MAKE_ACQUAINTANCE)) {
//                if (CommunicationService.ACTION_INCOMING_TRANSFER_READY.equals(intent.getAction())
//                        && intent.hasExtra(CommunicationService.EXTRA_GROUP_ID)) {
//                    ViewTransferActivity.startInstance(ReceivedFragment.this,
//                            intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1));
//                    finish();
//                }
//            }
//        }
//    };
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState)
//    {
//        Log.d("DEBUG1", "onCreate: onCreate");
//        super.onCreate(savedInstanceState);
//
//        setResult(RESULT_CANCELED);
//        setContentView(R.layout.activity_connection_manager);
//
//        FragmentFactory factory = getSupportFragmentManager().getFragmentFactory();
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        mAppBarLayout = findViewById(R.id.app_bar);
//        mProgressBar = findViewById(R.id.activity_connection_establishing_progress_bar);
//        mToolbarLayout = findViewById(R.id.toolbar_layout);
//        mOptionsFragment = (ConnectionManagerActivity.OptionsFragment) factory.instantiate(getClassLoader(), ConnectionManagerActivity.OptionsFragment.class.getName(), null);
//        mBarcodeConnectFragment = (BarcodeConnectFragment) factory.instantiate(getClassLoader(), BarcodeConnectFragment.class.getName(), null);
//        mHotspotManagerFragment = (HotspotManagerFragment) factory.instantiate(getClassLoader(), HotspotManagerFragment.class.getName(), null);
//        mNetworkManagerFragment = (NetworkManagerFragment) factory.instantiate(getClassLoader(), NetworkManagerFragment.class.getName(), null);
//        mDeviceListFragment = (NetworkDeviceListFragment) factory.instantiate(getClassLoader(), NetworkDeviceListFragment.class.getName(), null);
//
//        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
//        mFilter.addAction(CommunicationService.ACTION_DEVICE_ACQUAINTANCE);
//        mFilter.addAction(CommunicationService.ACTION_INCOMING_TRANSFER_READY);
//
//        setSupportActionBar(toolbar);
//
//        if (getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        if (getIntent() != null) {
//            if (getIntent().hasExtra(EXTRA_REQUEST_TYPE))
//                try {
//                    mRequestType = ReceivedFragment.RequestType.valueOf(getIntent().getStringExtra(EXTRA_REQUEST_TYPE));
//                } catch (Exception e) {
//                    // do nothing
//                }
//
//            if (getIntent().hasExtra(EXTRA_ACTIVITY_SUBTITLE))
//                mTitleProvided = getIntent().getStringExtra(EXTRA_ACTIVITY_SUBTITLE);
//        }
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        checkFragment();
//        registerReceiver(mReceiver, mFilter);
//    }
//
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        unregisterReceiver(mReceiver);
//    }
//
//    @Override
//    public void onBackPressed()
//    {
//        if (getShowingFragment() instanceof ConnectionManagerActivity.OptionsFragment)
//            super.onBackPressed();
//        else
//            setFragment(ConnectionManagerActivity.AvailableFragment.Options);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        int id = item.getItemId();
//
//        if (id == android.R.id.home)
//            onBackPressed();
//        else
//            return super.onOptionsItemSelected(item);
//
//        return true;
//    }
//
//    public void applyViewChanges(Fragment fragment, String mTitleProvided)
//    {
//        boolean isOptions = fragment instanceof ConnectionManagerActivity.OptionsFragment;
//
//        if (fragment instanceof ConnectionManagerActivity.DeviceSelectionSupport)
//            ((ConnectionManagerActivity.DeviceSelectionSupport) fragment).setDeviceSelectedListener(mDeviceSelectionListener);
//
//        if (getSupportActionBar() != null) {
//            CharSequence titleCurrent = fragment instanceof TitleSupport
//                    ? ((TitleSupport) fragment).getTitle(ReceivedFragment.this)
//                    : getString(R.string.text_connectDevices);
//
//            if (isOptions)
//                mToolbarLayout.setTitle(mTitleProvided != null ? mTitleProvided : titleCurrent);
//            else
//                mToolbarLayout.setTitle(titleCurrent);
//        }
//
//        mAppBarLayout.setExpanded(isOptions, true);
//    }
//
//    private void checkFragment()
//    {
//        Fragment currentFragment = getShowingFragment();
//
//        if (currentFragment == null)
//            setFragment(ConnectionManagerActivity.AvailableFragment.Options);
//        else
//            applyViewChanges(currentFragment, mTitleProvided);
//    }
//
//    @Override
//    public Snackbar createSnackbar(int resId, Object... objects)
//    {
//        return Snackbar.make(findViewById(R.id.activity_connection_establishing_content_view), getString(resId, objects), Snackbar.LENGTH_LONG);
//    }
//
//    @IdRes
//    public ConnectionManagerActivity.AvailableFragment getShowingFragmentId()
//    {
//        Fragment fragment = getShowingFragment();
//
//        if (fragment instanceof BarcodeConnectFragment)
//            return ConnectionManagerActivity.AvailableFragment.ScanQrCode;
//        else if (fragment instanceof HotspotManagerFragment)
//            return ConnectionManagerActivity.AvailableFragment.CreateHotspot;
//        else if (fragment instanceof NetworkManagerFragment)
//            return ConnectionManagerActivity.AvailableFragment.UseExistingNetwork;
//        else if (fragment instanceof NetworkDeviceListFragment)
//            return ConnectionManagerActivity.AvailableFragment.UseKnownDevice;
//
//        // Probably OptionsFragment
//        return ConnectionManagerActivity.AvailableFragment.Options;
//    }
//
//    @Nullable
//    public Fragment getShowingFragment()
//    {
//        return getSupportFragmentManager().findFragmentById(R.id.activity_connection_establishing_content_view);
//    }
//
//    public void setFragment(ConnectionManagerActivity.AvailableFragment fragment)
//    {
//        @Nullable
//        Fragment activeFragment = getShowingFragment();
//        Fragment fragmentCandidate = null;
//
//        switch (fragment) {
//            case ScanQrCode:
//                //fragmentCandidate = mBarcodeConnectFragment;
//                if (mOptionsFragment.isAdded())
//                    mOptionsFragment.startCodeScanner();
//                return;
//            case CreateHotspot:
//                fragmentCandidate = mHotspotManagerFragment;
//                break;
//            case UseExistingNetwork:
//                fragmentCandidate = mNetworkManagerFragment;
//                break;
//            case UseKnownDevice:
//                fragmentCandidate = mDeviceListFragment;
//                break;
//            default:
//                fragmentCandidate = mOptionsFragment;
//        }
//
//        if (activeFragment == null || fragmentCandidate != activeFragment) {
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//            if (activeFragment != null)
//                transaction.remove(activeFragment);
//
//            if (activeFragment != null && fragmentCandidate instanceof ConnectionManagerActivity.OptionsFragment)
//                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
//            else
//                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
//
//            transaction.add(R.id.activity_connection_establishing_content_view, fragmentCandidate);
//            transaction.commit();
//
//            applyViewChanges(fragmentCandidate, mTitleProvided);
//        }
//    }
//
//    protected void showEnterIpAddressDialog()
//    {
//        ConnectionUtils connectionUtils = ConnectionUtils.getInstance(this);
//        UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, this);
//        new ManualIpAddressConnectionDialog(this, uiConnectionUtils, mDeviceSelectionListener).show();
//    }
//
//    public enum RequestType
//    {
//        RETURN_RESULT,
//        MAKE_ACQUAINTANCE
//    }
//
//    public enum AvailableFragment
//    {
//        Options,
//        UseExistingNetwork,
//        UseKnownDevice,
//        ScanQrCode,
//        CreateHotspot,
//        EnterIpAddress
//    }
//
//    public interface DeviceSelectionSupport
//    {
//        void setDeviceSelectedListener(NetworkDeviceSelectedListener listener);
//    }
//
//    public static class OptionsFragment
//            extends com.genonbeta.android.framework.app.Fragment
//            implements ConnectionManagerActivity.DeviceSelectionSupport
//    {
//        public static final int REQUEST_CHOOSE_DEVICE = 100;
//
//        private NetworkDeviceSelectedListener mListener;
//
//        @Nullable
//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
//        {
//            View view = inflater.inflate(R.layout., container, false);
//
//            View.OnClickListener listener = new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    switch (v.getId()) {
//                        case R.id.connection_option_devices:
//                            updateFragment(ConnectionManagerActivity.AvailableFragment.UseKnownDevice);
//                            break;
//                        case R.id.connection_option_hotspot:
//                            updateFragment(ConnectionManagerActivity.AvailableFragment.CreateHotspot);
//                            break;
//                        case R.id.connection_option_network:
//                            updateFragment(ConnectionManagerActivity.AvailableFragment.UseExistingNetwork);
//                            break;
//                        case R.id.connection_option_manual_ip:
//                            updateFragment(ConnectionManagerActivity.AvailableFragment.EnterIpAddress);
//                            break;
//                        case R.id.connection_option_scan:
//                            startCodeScanner();
//                    }
//                }
//            };
//
//            view.findViewById(R.id.connection_option_devices).setOnClickListener(listener);
//            view.findViewById(R.id.connection_option_hotspot).setOnClickListener(listener);
//            view.findViewById(R.id.connection_option_network).setOnClickListener(listener);
//            view.findViewById(R.id.connection_option_scan).setOnClickListener(listener);
//            view.findViewById(R.id.connection_option_manual_ip).setOnClickListener(listener);
//
//            view.findViewById(R.id.connection_option_guide).setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    new ConnectionSetUpAssistant(getActivity())
//                            .startShowing();
//                }
//            });
//
//            return view;
//        }
//
//        @Override
//        public void onActivityResult(int requestCode, int resultCode, Intent data)
//        {
//            super.onActivityResult(requestCode, resultCode, data);
//
//            if (requestCode == REQUEST_CHOOSE_DEVICE)
//                if (resultCode == RESULT_OK && data != null) {
//                    try {
//                        NetworkDevice device = new NetworkDevice(data.getStringExtra(BarcodeScannerActivity.EXTRA_DEVICE_ID));
//                        AppUtils.getDatabase(getContext()).reconstruct(device);
//                        NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(BarcodeScannerActivity.EXTRA_CONNECTION_ADAPTER));
//                        AppUtils.getDatabase(getContext()).reconstruct(connection);
//
//                        if (mListener != null)
//                            mListener.onNetworkDeviceSelected(device, connection);
//                    } catch (Exception e) {
//                        // do nothing
//                    }
//                }
//        }
//
//        private void startCodeScanner()
//        {
//            startActivityForResult(new Intent(getActivity(), BarcodeScannerActivity.class),
//                    REQUEST_CHOOSE_DEVICE);
//        }
//
//        public void updateFragment(ConnectionManagerActivity.AvailableFragment fragment)
//        {
//            if (getContext() != null)
//                getContext().sendBroadcast(new Intent(ACTION_CHANGE_FRAGMENT)
//                        .putExtra(EXTRA_FRAGMENT_ENUM, fragment.toString()));
//        }
//
//        @Override
//        public void setDeviceSelectedListener(NetworkDeviceSelectedListener listener)
//        {
//            mListener = listener;
//        }
//    }
//
//
//
//}