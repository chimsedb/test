package com.example.test.viewmodel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.test.R;
import com.example.test.adapter.AdapterRCInfoThread;
import com.example.test.retrofit.GetDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.example.test.utils.DateUtils.convertIntToTimeFormat;
import static com.example.test.utils.DiffUtils.VALUE_1_SECONDS;
import static com.example.test.utils.DiffUtils.VALUE_6_MINUTES;
import static com.example.test.utils.DiffUtils.VALUE_9_MINUTES;
import static com.example.test.utils.DiffUtils.VALUE_EXCEEDS;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getName();

    private final Context context;
    private final GetDataService service;
    private final AdapterRCInfoThread adapter;
    private Location location;
    private final CompositeDisposable composite = new CompositeDisposable();

    public MutableLiveData<String> txtLatLong = new MutableLiveData<>();
    public MutableLiveData<String> txtBattery = new MutableLiveData<>();
    public MutableLiveData<String> txtCount = new MutableLiveData<>();

    public MutableLiveData<String> timeExecute = new MutableLiveData<>();

    private Observable<Long> threadT1;
    private Observable<Long> threadT2;
    private Observable<Response<Void>> threadT3;

    private List<String> listValueForThread3 = new ArrayList<>();
    public Boolean isBatteryCanSet = false;

    public MainViewModel(@NonNull Context context, @NonNull GetDataService service, @NonNull AdapterRCInfoThread adapter) {
        this.context = context;
        this.service = service;
        this.adapter = adapter;
        initData();
        initLocationInfo();
    }

    private void initData() {
        synchronized (listValueForThread3) {
            adapter.setList(listValueForThread3);
        }
        txtLatLong.postValue(context.getString(R.string.txt_latlong, "0"));
        txtBattery.postValue(context.getString(R.string.txt_battery, "0"));
        txtCount.postValue(context.getString(R.string.txt_total_item, "0"));
        timeExecute.postValue("00:00:00");
    }

    public void start() {
        executeTime();
        createThreadT1();
        createThreadT2();
        createThreadT3();

        executeThreadT1();
        executeThreadT2();
    }

    public void stop() {
        txtLatLong.postValue(String.valueOf(0));
        isBatteryCanSet = false;
        txtBattery.postValue(txtBattery.getValue());
        listValueForThread3.clear();
        txtCount.postValue(String.valueOf(listValueForThread3.size()));
        composite.clear();
    }

    private void initLocationInfo() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private Observable<Long> createThreadT1() {
        if (threadT1 == null) {
            threadT1 = Observable.interval(0, VALUE_6_MINUTES, TimeUnit.MILLISECONDS);
        }
        return threadT1;
    }

    private Observable<Long> createThreadT2() {
        if (threadT2 == null) {
            threadT2 = Observable.interval(0, VALUE_9_MINUTES, TimeUnit.MILLISECONDS);
        }
        return threadT2;
    }

    private Observable<Response<Void>> createThreadT3() {
        if (threadT3 == null) {
            threadT3 = service.postAllDataFromThread3();
        }
        return threadT3;
    }

    private synchronized void executeThreadT1() {
        if (threadT1 == null) {
            threadT1 = createThreadT1();
        }
        composite.add(threadT1.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e(TAG, "Thread 1 " + throwable.getMessage()))
                .subscribe(aLong -> {
                    isBatteryCanSet = true;
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    txtLatLong.postValue("(" + longitude + " , " + latitude + ")");
                    listValueForThread3.add(txtLatLong.getValue());
                    adapter.notifyDataSetChanged();
                    executeThreadT3();
                }));
    }

    private synchronized void executeThreadT2() {
        if (threadT2 == null) {
            threadT2 = createThreadT2();
        }
        composite.add(threadT2.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e(TAG, "Thread 2 " + throwable.getMessage()))
                .subscribe(aLong -> {
                    txtBattery.postValue(txtBattery.getValue());
                    listValueForThread3.add(txtBattery.getValue());
                    adapter.notifyDataSetChanged();
                    executeThreadT3();
                }));
    }

    private synchronized void executeThreadT3() {
        if (threadT3 == null) {
            threadT3 = createThreadT3();
        }

        txtCount.postValue(String.valueOf(listValueForThread3.size()));
        if (listValueForThread3.size() > VALUE_EXCEEDS) {
            composite.add(threadT3.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Log.e(TAG, "Thread 3 " + throwable.getMessage()))
                    .subscribe(voidResponse -> Log.d(TAG, voidResponse.code() + "")));
        }

    }

    private void executeTime() {
        AtomicInteger startTime = new AtomicInteger();
        composite.add(Observable.interval(0, VALUE_1_SECONDS, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e(TAG, "Execute Time " + throwable.getMessage()))
                .subscribe(aLong -> {
                    startTime.addAndGet(1);
                    timeExecute.postValue(convertIntToTimeFormat(startTime.get()));
                }));
    }
}
