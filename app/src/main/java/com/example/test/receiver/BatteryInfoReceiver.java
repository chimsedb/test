package com.example.test.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.example.test.viewmodel.MainViewModel;

import static com.example.test.utils.DiffUtils.EXTRA_BATTERY_PERCENT;

public class BatteryInfoReceiver extends BroadcastReceiver {
    private MainViewModel viewModel;

    public BatteryInfoReceiver(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        intent.putExtra(EXTRA_BATTERY_PERCENT, level);
        if (viewModel != null) {
            viewModel.txtBattery.postValue(level + "%");
        }
    }
}
