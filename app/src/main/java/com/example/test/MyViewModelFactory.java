package com.example.test;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.test.adapter.AdapterRCInfoThread;
import com.example.test.retrofit.GetDataService;
import com.example.test.viewmodel.MainViewModel;

public class MyViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private GetDataService service;
    private AdapterRCInfoThread adapter;


    public MyViewModelFactory(Application application, GetDataService service, AdapterRCInfoThread adapter) {
        this.application = application;
        this.service = service;
        this.adapter = adapter;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MainViewModel(application, service,adapter);
    }
}