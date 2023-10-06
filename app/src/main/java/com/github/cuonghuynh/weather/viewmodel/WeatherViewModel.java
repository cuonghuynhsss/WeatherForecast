package com.github.cuonghuynh.weather.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.cuonghuynh.weather.model.CityInfo;

public class WeatherViewModel extends ViewModel {

    public LiveData<Integer> getNavigationIdSelected() {
        return navigationIdSelected;
    }

    public void setNavigationIdSelected(Integer navigationIdSelected) {
        this.navigationIdSelected.setValue(navigationIdSelected);
    }

    public LiveData<CityInfo> getCityInfoCurrent() {
        return cityInfo;
    }

    public void setCityInfoCurrent(CityInfo cityInfo) {
        this.cityInfo.setValue(cityInfo);
    }

    private MutableLiveData<Integer> navigationIdSelected = new MutableLiveData<>();
    private MutableLiveData<CityInfo> cityInfo = new MutableLiveData<>();
}
