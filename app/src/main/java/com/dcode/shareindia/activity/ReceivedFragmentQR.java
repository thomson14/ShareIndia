package com.dcode.shareindia.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;

import com.dcode.shareindia.R;
import com.dcode.shareindia.app.Activity;
import com.dcode.shareindia.database.AccessDatabase;
import com.dcode.shareindia.dialog.ManualIpAddressConnectionDialog;
import com.dcode.shareindia.fragment.BarcodeConnectFragment;
import com.dcode.shareindia.fragment.HotspotManagerFragment;
import com.dcode.shareindia.fragment.NetworkDeviceListFragment;
import com.dcode.shareindia.fragment.NetworkManagerFragment;
import com.dcode.shareindia.object.NetworkDevice;
import com.dcode.shareindia.service.CommunicationService;
import com.dcode.shareindia.ui.UIConnectionUtils;
import com.dcode.shareindia.ui.UITask;
import com.dcode.shareindia.ui.callback.NetworkDeviceSelectedListener;
import com.dcode.shareindia.ui.callback.TitleSupport;
import com.dcode.shareindia.ui.help.ConnectionSetUpAssistant;
import com.dcode.shareindia.util.AppUtils;
import com.dcode.shareindia.util.ConnectionUtils;
import com.dcode.shareindia.util.NetworkDeviceLoader;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

public class ReceivedFragmentQR
        extends Activity
        implements SnackbarSupport
{
    public static final String ACTION_CHANGE_FRAGMENT = "com.genonbeta.intent.action.CONNECTION_MANAGER_CHANGE_FRAGMENT";
    public static final String EXTRA_FRAGMENT_ENUM = "extraFragmentEnum";
    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";

    private final IntentFilter mFilter = new IntentFilter();
    private HotspotManagerFragment mHotspotManagerFragment;
    private BarcodeConnectFragment mBarcodeConnectFragment;
    private NetworkManagerFragment mNetworkManagerFragment;
    private NetworkDeviceListFragment mDeviceListFragment;
    private OptionsFragment mOptionsFragment;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolbarLayout;
    private ProgressBar mProgressBar;
    private String mTitleProvided;
    private RequestType mRequestType = RequestType.RETURN_RESULT;

    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener()
    {
        @Override
        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection)
        {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                setResult(RESULT_OK, new Intent()
                        .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
                        .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));

                finish();
            } else {
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(ReceivedFragmentQR.this);
                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, ReceivedFragmentQR.this);

                UITask uiTask = new UITask()
                {
                    @Override
                    public void updateTaskStarted(Interrupter interrupter)
                    {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void updateTaskStopped()
                    {
                        mProgressBar.setVisibility(View.GONE);
                    }
                };

                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener()
                {
                    @Override
                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection)
                    {
                        createSnackbar(R.string.mesg_completing).show();
                    }
                };

                uiConnectionUtils.makeAcquaintance(ReceivedFragmentQR.this, uiTask,
                        connection.ipAddress, -1, registeredListener);
            }

            return true;
        }

        @Override
        public boolean isListenerEffective()
        {
            return true;
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
                    && intent.hasExtra(EXTRA_FRAGMENT_ENUM)) {
                String fragmentEnum = intent.getStringExtra(EXTRA_FRAGMENT_ENUM);

                try {
                    AvailableFragment value = AvailableFragment.valueOf(fragmentEnum);

                    if (AvailableFragment.EnterIpAddress.equals(value))
                        showEnterIpAddressDialog();
                    else
                        setFragment(value);
                } catch (Exception e) {
                    // do nothing
                }
            } else if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                if (CommunicationService.ACTION_DEVICE_ACQUAINTANCE.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_DEVICE_ID)
                        && intent.hasExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME)) {
                    NetworkDevice device = new NetworkDevice(intent.getStringExtra(CommunicationService.EXTRA_DEVICE_ID));
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, intent.getStringExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME));

                    try {
                        AppUtils.getDatabase(ReceivedFragmentQR.this).reconstruct(device);
                        AppUtils.getDatabase(ReceivedFragmentQR.this).reconstruct(connection);

                        mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (mRequestType.equals(RequestType.MAKE_ACQUAINTANCE)) {
                if (CommunicationService.ACTION_INCOMING_TRANSFER_READY.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_GROUP_ID)) {
                    ViewTransferActivity.startInstance(ReceivedFragmentQR.this,
                            intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1));
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.fragment_received_q_r);

        FragmentFactory factory = getSupportFragmentManager().getFragmentFactory();
        //Toolbar toolbar = findViewById(R.id.toolbar_RD);
        mAppBarLayout = findViewById(R.id.app_bar_RD);
        mProgressBar = findViewById(R.id.activity_connection_establishing_progress_bar_RD);
        mToolbarLayout = findViewById(R.id.toolbar_layout_RD);
        mOptionsFragment = (OptionsFragment) factory.instantiate(getClassLoader(), OptionsFragment.class.getName(), null);
        mBarcodeConnectFragment = (BarcodeConnectFragment) factory.instantiate(getClassLoader(), BarcodeConnectFragment.class.getName(), null);
        mHotspotManagerFragment = (HotspotManagerFragment) factory.instantiate(getClassLoader(), HotspotManagerFragment.class.getName(), null);
        mNetworkManagerFragment = (NetworkManagerFragment) factory.instantiate(getClassLoader(), NetworkManagerFragment.class.getName(), null);
        mDeviceListFragment = (NetworkDeviceListFragment) factory.instantiate(getClassLoader(), NetworkDeviceListFragment.class.getName(), null);

        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
        mFilter.addAction(CommunicationService.ACTION_DEVICE_ACQUAINTANCE);
        mFilter.addAction(CommunicationService.ACTION_INCOMING_TRANSFER_READY);

        //setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_REQUEST_TYPE))
                try {
                    mRequestType = RequestType.valueOf(getIntent().getStringExtra(EXTRA_REQUEST_TYPE));
                } catch (Exception e) {
                    // do nothing
                }

            if (getIntent().hasExtra(EXTRA_ACTIVITY_SUBTITLE))
                mTitleProvided = getIntent().getStringExtra(EXTRA_ACTIVITY_SUBTITLE);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkFragment();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed()
    {
        if (getShowingFragment() instanceof OptionsFragment)
            super.onBackPressed();
        else
            setFragment(AvailableFragment.Options);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    public void applyViewChanges(Fragment fragment, String mTitleProvided)
    {
        boolean isOptions = fragment instanceof OptionsFragment;

        if (fragment instanceof DeviceSelectionSupport)
            ((DeviceSelectionSupport) fragment).setDeviceSelectedListener(mDeviceSelectionListener);

        if (getSupportActionBar() != null) {
            CharSequence titleCurrent = fragment instanceof TitleSupport
                    ? ((TitleSupport) fragment).getTitle(ReceivedFragmentQR.this)
                    : getString(R.string.text_connectDevices);

            if (isOptions)
                mToolbarLayout.setTitle(mTitleProvided != null ? mTitleProvided : titleCurrent);
            else
                mToolbarLayout.setTitle(titleCurrent);
        }

        mAppBarLayout.setExpanded(isOptions, true);
    }

    private void checkFragment()
    {
        Fragment currentFragment = getShowingFragment();

        if (currentFragment == null)
            setFragment(AvailableFragment.Options);
        else
            applyViewChanges(currentFragment, mTitleProvided);
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects)
    {
        return Snackbar.make(findViewById(R.id.activity_connection_establishing_content_view_RD), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

    @IdRes
    public AvailableFragment getShowingFragmentId()
    {
        Fragment fragment = getShowingFragment();

        if (fragment instanceof BarcodeConnectFragment)
            return AvailableFragment.ScanQrCode;
        else if (fragment instanceof HotspotManagerFragment)
            return AvailableFragment.CreateHotspot;
        else if (fragment instanceof NetworkManagerFragment)
            return AvailableFragment.UseExistingNetwork;
        else if (fragment instanceof NetworkDeviceListFragment)
            return AvailableFragment.UseKnownDevice;

        // Probably OptionsFragment
        return AvailableFragment.Options;
    }

    @Nullable
    public Fragment getShowingFragment()
    {
        return getSupportFragmentManager().findFragmentById(R.id.activity_connection_establishing_content_view_RD);
    }

    public void setFragment(AvailableFragment fragment)
    {
        @Nullable
        Fragment activeFragment = getShowingFragment();
        Fragment fragmentCandidate = null;

        switch (fragment) {
            case ScanQrCode:
                //fragmentCandidate = mBarcodeConnectFragment;
                if (mOptionsFragment.isAdded())
                    mOptionsFragment.startCodeScanner();
                return;
            case CreateHotspot:
                fragmentCandidate = mHotspotManagerFragment;
                break;
            case UseExistingNetwork:
                fragmentCandidate = mNetworkManagerFragment;
                break;
            case UseKnownDevice:
                fragmentCandidate = mDeviceListFragment;
                break;
            default:
                fragmentCandidate = mOptionsFragment;
        }

        if (activeFragment == null || fragmentCandidate != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (activeFragment != null)
                transaction.remove(activeFragment);

            if (activeFragment != null && fragmentCandidate instanceof OptionsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.activity_connection_establishing_content_view_RD, fragmentCandidate);
            transaction.commit();

            applyViewChanges(fragmentCandidate, mTitleProvided);
        }
    }

    protected void showEnterIpAddressDialog()
    {
        ConnectionUtils connectionUtils = ConnectionUtils.getInstance(this);
        UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, this);
        new ManualIpAddressConnectionDialog(this, uiConnectionUtils, mDeviceSelectionListener).show();
    }

    public enum RequestType
    {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }

    public enum AvailableFragment
    {
        Options,
        UseExistingNetwork,
        UseKnownDevice,
        ScanQrCode,
        CreateHotspot,
        EnterIpAddress
    }

    public interface DeviceSelectionSupport
    {
        void setDeviceSelectedListener(NetworkDeviceSelectedListener listener);
    }

    public static class OptionsFragment
            extends com.genonbeta.android.framework.app.Fragment
            implements DeviceSelectionSupport
    {
        public static final int REQUEST_CHOOSE_DEVICE = 100;

        private NetworkDeviceSelectedListener mListener;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.connection_options_fragment2, container, false);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId()) {
                        case R.id.connection_option_devices_RD:
                            updateFragment(AvailableFragment.UseKnownDevice);
                            break;
                        case R.id.connection_option_hotspot_RD:
                            updateFragment(AvailableFragment.CreateHotspot);
                            break;
                        case R.id.connection_option_network_RD:
                            updateFragment(AvailableFragment.UseExistingNetwork);
                            break;
                        case R.id.connection_option_manual_ip_RD:
                            updateFragment(AvailableFragment.EnterIpAddress);
                            break;
                        case R.id.connection_option_scan_RD:
                            startCodeScanner();
                    }
                }
            };

            view.findViewById(R.id.connection_option_devices_RD).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_hotspot_RD).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_network_RD).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_scan_RD).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_manual_ip_RD).setOnClickListener(listener);

            view.findViewById(R.id.connection_option_guide_RD).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new ConnectionSetUpAssistant(getActivity())
                            .startShowing();
                }
            });

            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CHOOSE_DEVICE)
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        NetworkDevice device = new NetworkDevice(data.getStringExtra(BarcodeScannerActivity.EXTRA_DEVICE_ID));
                        AppUtils.getDatabase(getContext()).reconstruct(device);
                        NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(BarcodeScannerActivity.EXTRA_CONNECTION_ADAPTER));
                        AppUtils.getDatabase(getContext()).reconstruct(connection);

                        if (mListener != null)
                            mListener.onNetworkDeviceSelected(device, connection);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
        }

        private void startCodeScanner()
        {
            startActivityForResult(new Intent(getActivity(), BarcodeScannerActivity.class),
                    REQUEST_CHOOSE_DEVICE);
        }

        public void updateFragment(AvailableFragment fragment)
        {
            if (getContext() != null)
                getContext().sendBroadcast(new Intent(ACTION_CHANGE_FRAGMENT)
                        .putExtra(EXTRA_FRAGMENT_ENUM, fragment.toString()));
        }

        @Override
        public void setDeviceSelectedListener(NetworkDeviceSelectedListener listener)
        {
            mListener = listener;
        }
    }
}
