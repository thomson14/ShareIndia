package com.example.shareindia.dialog;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.example.shareindia.R;
import com.example.shareindia.database.AccessDatabase;
import com.example.shareindia.object.NetworkDevice;
import com.example.shareindia.ui.UIConnectionUtils;
import com.example.shareindia.ui.callback.NetworkDeviceSelectedListener;
import com.example.shareindia.util.NetworkDeviceLoader;

/**
 * created by: veli
 * date: 4/4/19 7:27 PM
 */
public class ManualIpAddressConnectionDialog extends AbstractSingleTextInputDialog
{
    private NetworkDeviceLoader.OnDeviceRegisteredListener mSelfListener = new NetworkDeviceLoader
            .OnDeviceRegisteredListener()
    {
        @Override
        public void onDeviceRegistered(AccessDatabase database, NetworkDevice device, NetworkDevice.Connection connection)
        {
            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            if (mListener != null)
                mListener.onNetworkDeviceSelected(device, connection);
        }
    };

    private AlertDialog mDialog;
    private NetworkDeviceSelectedListener mListener;

    public ManualIpAddressConnectionDialog(final Activity activity, final UIConnectionUtils utils,
                                           NetworkDeviceSelectedListener listener)
    {
        super(activity);

        mListener = listener;

        setTitle(R.string.butn_enterIpAddress);

        setOnProceedClickListener(R.string.butn_connect, new OnProceedClickListener()
        {
            @Override
            public boolean onProceedClick(AlertDialog dialog)
            {
                doTask(activity, utils);
                return false;
            }
        });
    }

    private void doTask(final Activity activity, final UIConnectionUtils utils)
    {

        final String ipAddress = getEditText().getText().toString();

        if (ipAddress.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
            utils.makeAcquaintance(activity, null, ipAddress, -1, mSelfListener);
        } else
            getEditText().setError(getContext().getString(R.string.mesg_errorNotAnIpAddress));
    }

    @Override
    public AlertDialog show()
    {
        return mDialog = super.show();
    }
}
