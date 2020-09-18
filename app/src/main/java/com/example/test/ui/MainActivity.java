package com.example.test.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.test.adapter.AdapterRCInfoThread;
import com.example.test.viewmodel.MainViewModel;
import com.example.test.MyViewModelFactory;
import com.example.test.R;
import com.example.test.databinding.ActivityMainBinding;
import com.example.test.receiver.BatteryInfoReceiver;
import com.example.test.retrofit.GetDataService;
import com.example.test.retrofit.RetrofitClientInstance;

import static com.example.test.utils.DiffUtils.REQUEST_ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {


    private MainViewModel viewModel;

    private BatteryInfoReceiver batteryInfoReceiver;

    private GetDataService service;

    private AdapterRCInfoThread adapter;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_ACCESS_FINE_LOCATION);
    }

    private void callBackFromViewModel() {
        viewModel.txtLatLong.observe(this, s -> binding.txtLatLong.setText(getString(R.string.txt_latlong,s)));

        viewModel.txtBattery.observe(this, s -> {
            if (viewModel.isBatteryCanSet) {
                binding.txtBattery.setText(getString(R.string.txt_battery, s));
            } else {
                binding.txtBattery.setText(getString(R.string.txt_battery, "0"));
            }
        });

        viewModel.txtCount.observe(this, s -> binding.txtCount.setText(getString(R.string.txt_total_item, s)));

        viewModel.timeExecute.observe(this, s -> binding.txtTimeExecute.setText(s));
    }

    private void initData() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        setUpForRecyclerView();
        viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication(), service,adapter)).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        batteryInfoReceiver = new BatteryInfoReceiver(viewModel);
        registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void setUpForRecyclerView() {
        adapter = new AdapterRCInfoThread();
        binding.rcInfo.setHasFixedSize(true);
        binding.rcInfo.setLayoutManager(new LinearLayoutManager(this));
        binding.rcInfo.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initData();
                    callBackFromViewModel();
                } else {
                    finish();
                }
                return;
            }
        }
    }
}
