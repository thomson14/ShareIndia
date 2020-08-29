package com.example.shareindia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.shareindia.R;
import com.example.shareindia.activity.ConnectionManagerActivity;
import com.example.shareindia.activity.ContentSharingActivity;
import com.example.shareindia.app.Activity;


public class MainSendRecieveFragment extends Fragment implements Activity.OnBackPressedListener{


    public MainSendRecieveFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View viewSend = view.findViewById(R.id.sendLayoutButton);
        View viewReceive = view.findViewById(R.id.receiveLayoutButton);
        View viewHistory = view.findViewById(R.id.historyLayoutButton);

        viewSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getContext(), ContentSharingActivity.class));
            }
        });

        viewReceive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ConnectionManagerActivity.class)
                        .putExtra(ConnectionManagerActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                        .putExtra(ConnectionManagerActivity.EXTRA_REQUEST_TYPE, ConnectionManagerActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            }
        });

        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.transfer_group_list_main, new TransferGroupListFragment(), "NewFragmentTag");
                ft.commit();
                ft.addToBackStack(null);

            }
        });


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_send_recieve, container, false);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
