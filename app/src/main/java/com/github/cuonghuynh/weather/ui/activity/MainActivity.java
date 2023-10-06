package com.github.cuonghuynh.weather.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.cuonghuynh.weather.R;
import com.github.cuonghuynh.weather.databinding.ActivityMainBinding;
import com.github.cuonghuynh.weather.model.CityInfo;
import com.github.cuonghuynh.weather.model.common.WeatherItem;
import com.github.cuonghuynh.weather.model.currentweather.CurrentWeatherResponse;
import com.github.cuonghuynh.weather.model.daysweather.ListItem;
import com.github.cuonghuynh.weather.model.daysweather.MultipleDaysWeatherResponse;
import com.github.cuonghuynh.weather.model.db.CurrentWeather;
import com.github.cuonghuynh.weather.model.db.FiveDayWeather;
import com.github.cuonghuynh.weather.model.db.ItemHourlyDB;
import com.github.cuonghuynh.weather.model.db.MultipleDaysWeather;
import com.github.cuonghuynh.weather.model.fivedayweather.FiveDayResponse;
import com.github.cuonghuynh.weather.model.fivedayweather.ItemHourly;
import com.github.cuonghuynh.weather.service.ApiService;
import com.github.cuonghuynh.weather.ui.fragment.AboutFragment;
import com.github.cuonghuynh.weather.ui.fragment.MapFragment;
import com.github.cuonghuynh.weather.ui.fragment.SearchFragment;
import com.github.cuonghuynh.weather.ui.fragment.SettingFragment;
import com.github.cuonghuynh.weather.utils.ApiClient;
import com.github.cuonghuynh.weather.utils.AppUtil;
import com.github.cuonghuynh.weather.utils.Constants;
import com.github.cuonghuynh.weather.utils.DbUtil;
import com.github.cuonghuynh.weather.utils.MyApplication;
import com.github.cuonghuynh.weather.utils.SnackbarUtil;
import com.github.cuonghuynh.weather.utils.TextViewFactory;
import com.github.cuonghuynh.weather.viewmodel.WeatherViewModel;
import com.github.pwittchen.prefser.library.rx2.Prefser;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscriptionList;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class MainActivity extends BaseActivity {

    private FastAdapter<FiveDayWeather> mFastAdapter;
    private ItemAdapter<FiveDayWeather> mItemAdapter;
    private FastAdapter<MultipleDaysWeather> mFastAdapterMultipleDays;
    private ItemAdapter<MultipleDaysWeather> mItemAdapterMultipleDays;
    private Box<MultipleDaysWeather> multipleDaysWeatherBox;
    private CompositeDisposable disposable = new CompositeDisposable();
    private String defaultLang = "en";
    private List<FiveDayWeather> fiveDayWeathers;
    private ApiService apiService;
    private FiveDayWeather todayFiveDayWeather;
    private Prefser prefser;
    private Box<CurrentWeather> currentWeatherBox;
    private Box<FiveDayWeather> fiveDayWeatherBox;
    private Box<ItemHourlyDB> itemHourlyDBBox;
    private DataSubscriptionList subscriptions = new DataSubscriptionList();
    private boolean isLoad = false;
    private CityInfo cityInfo;
    private String apiKey;
    private Typeface typeface;
    private ActivityMainBinding binding;
    private int[] colors;
    private int[] colorsAlpha;
    private WeatherViewModel weatherViewModel;
    private Date currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        initViewModel();
        initObserve();
        setContentView(binding.getRoot());
        setSupportActionBar(binding.contentMainLayout.toolbar);
        initSearchView();
        initValues();
        setupTextSwitchers();
        initRecyclerView();
        initRecyclerViewMultipleDays();
        showStoredCurrentWeather();
        showStoredFiveDayWeather();
        showStoredMultipleDaysWeather();
        checkLastUpdate();
        checkTimePass();
    }

    private void initViewModel() {
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        weatherViewModel.setNavigationIdSelected(Constants.NAVIGATION_HOME_ID);
        weatherViewModel.getNavigationIdSelected().observe(this, navCurrent -> {
            switch (navCurrent) {
                case 1:
                    binding.btnHome.setImageResource(R.drawable.ic_home_light);
                    binding.btnChat.setImageResource(R.drawable.chatbot__1_);
                    binding.btnMap.setImageResource(R.drawable.ic_map);
                    binding.btnSetting.setImageResource(R.drawable.ic_setting);
                    return;
                case 2:
                    binding.btnHome.setImageResource(R.drawable.ic_home);
                    binding.btnChat.setImageResource(R.drawable.ic_chatbot_live);
                    binding.btnMap.setImageResource(R.drawable.ic_map);
                    binding.btnSetting.setImageResource(R.drawable.ic_setting);
                    return;
                case 3:
                    binding.btnHome.setImageResource(R.drawable.ic_home);
                    binding.btnChat.setImageResource(R.drawable.chatbot__1_);
                    binding.btnMap.setImageResource(R.drawable.ic_map_light_);
                    binding.btnSetting.setImageResource(R.drawable.ic_setting);
                    return;
                case 4:
                    binding.btnHome.setImageResource(R.drawable.ic_home);
                    binding.btnChat.setImageResource(R.drawable.chatbot__1_);
                    binding.btnMap.setImageResource(R.drawable.ic_map);
                    binding.btnSetting.setImageResource(R.drawable.ic_setting_light);
                    return;
                default:
                    return;

            }
        });
    }

    private void initObserve() {

    }

    private void initSearchView() {
        binding.contentMainLayout.searchView.setVoiceSearch(false);
        binding.contentMainLayout.searchView.setHint(getString(R.string.search_label));
        binding.contentMainLayout.searchView.setCursorDrawable(R.drawable.custom_curosr);
        binding.contentMainLayout.searchView.setEllipsize(true);
        binding.contentMainLayout.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                requestWeather(query, true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        binding.contentMainLayout.searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.contentMainLayout.searchView.showSearch();
                binding.contentMainLayout.searchView.requestFocus();
            }
        });

    }

    private void initValues() {
        colors = getResources().getIntArray(R.array.mdcolor_500);
        colorsAlpha = getResources().getIntArray(R.array.mdcolor_500_alpha);
        prefser = new Prefser(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        BoxStore boxStore = MyApplication.getBoxStore();
        currentWeatherBox = boxStore.boxFor(CurrentWeather.class);
        fiveDayWeatherBox = boxStore.boxFor(FiveDayWeather.class);
        itemHourlyDBBox = boxStore.boxFor(ItemHourlyDB.class);
        multipleDaysWeatherBox = boxStore.boxFor(MultipleDaysWeather.class);
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
                if (cityInfo != null) {
                    long lastStored = prefser.get(Constants.LAST_STORED_CURRENT, Long.class, 0L);
                    if (AppUtil.isTimePass(lastStored)) {
                        requestWeather(cityInfo.getName(), false);
                    } else {
                        binding.swipeContainer.setRefreshing(false);
                    }
                } else {
                    binding.swipeContainer.setRefreshing(false);
                }
            }

        });
//    binding.bar.setNavigationOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        showAboutFragment();
//      }
//    });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Vazir.ttf");
//    binding.nextDaysButton.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        AppUtil.showFragment(new MultipleDaysFragment(), getSupportFragmentManager(), true);
//      }
//    });
        binding.contentMainLayout.todayMaterialCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (todayFiveDayWeather != null) {
                    Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
                    intent.putExtra(Constants.FIVE_DAY_WEATHER_ITEM, todayFiveDayWeather);
                    startActivity(intent);
                }
            }
        });

        binding.btnHome.setOnClickListener(v -> {
            if (!Objects.requireNonNull(weatherViewModel.getNavigationIdSelected().getValue()).equals(Constants.NAVIGATION_HOME_ID)) {
                weatherViewModel.setNavigationIdSelected(Constants.NAVIGATION_HOME_ID);
                binding.contentMainLayout.getRoot().setVisibility(View.VISIBLE);
            }
        });
        binding.btnChat.setOnClickListener(v -> {
            if (!Objects.requireNonNull(weatherViewModel.getNavigationIdSelected().getValue()).equals(Constants.NAVIGATION_CHAT_ID)) {
                weatherViewModel.setNavigationIdSelected(Constants.NAVIGATION_CHAT_ID);
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                SearchFragment searchFragment = new SearchFragment();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                transaction.replace(R.id.frame_nav, searchFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                binding.contentMainLayout.getRoot().setVisibility(View.GONE);
            }

        });
        binding.btnSetting.setOnClickListener(v -> {
            if (!Objects.requireNonNull(weatherViewModel.getNavigationIdSelected().getValue()).equals(Constants.NAVIGATION_SETTING_ID)) {
                weatherViewModel.setNavigationIdSelected(Constants.NAVIGATION_SETTING_ID);
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                SettingFragment settingFragment = new SettingFragment();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                transaction.replace(R.id.frame_nav, settingFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                binding.contentMainLayout.getRoot().setVisibility(View.GONE);
            }
        });
        binding.btnMap.setOnClickListener(v -> {
            if (!Objects.requireNonNull(weatherViewModel.getNavigationIdSelected().getValue()).equals(Constants.NAVIGATION_MAP_ID)) {
                weatherViewModel.setNavigationIdSelected(Constants.NAVIGATION_MAP_ID);
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                MapFragment mapFragment = new MapFragment();
                transaction.replace(R.id.frame_nav, mapFragment);
                transaction.addToBackStack(null);
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                transaction.commit();
                binding.contentMainLayout.getRoot().setVisibility(View.GONE);
            }
        });
    }

    private void setupTextSwitchers() {
        binding.contentMainLayout.tempTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.TempTextView, true, typeface));
        binding.contentMainLayout.tempTextView.setInAnimation(MainActivity.this, R.anim.slide_in_right);
        binding.contentMainLayout.tempTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_left);
        binding.contentMainLayout.descriptionTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.DescriptionTextView, true, typeface));
        binding.contentMainLayout.descriptionTextView.setInAnimation(MainActivity.this, R.anim.slide_in_right);
        binding.contentMainLayout.descriptionTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_left);
        binding.contentMainLayout.humidityTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.HumidityTextView, false, typeface));
        binding.contentMainLayout.humidityTextView.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
        binding.contentMainLayout.humidityTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
        binding.contentMainLayout.windTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.WindSpeedTextView, false, typeface));
        binding.contentMainLayout.windTextView.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
        binding.contentMainLayout.windTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
        binding.contentMainLayout.tvLocation.setFactory(new TextViewFactory(MainActivity.this, R.style.TvLocation, false, typeface));
        binding.contentMainLayout.tvLocation.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
        binding.contentMainLayout.tvLocation.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
        binding.contentMainLayout.dateMaxMixTemp.setFactory(new TextViewFactory(MainActivity.this, R.style.dateMaxMixTemp, false, typeface));
        binding.contentMainLayout.dateMaxMixTemp.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
        binding.contentMainLayout.dateMaxMixTemp.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.contentMainLayout.recyclerView.setLayoutManager(layoutManager);
        mItemAdapter = new ItemAdapter<>();
        mFastAdapter = FastAdapter.with(mItemAdapter);
        binding.contentMainLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.contentMainLayout.recyclerView.setAdapter(mFastAdapter);
        binding.contentMainLayout.recyclerView.setFocusable(false);
        mFastAdapter.withOnClickListener(new OnClickListener<FiveDayWeather>() {
            @Override
            public boolean onClick(@Nullable View v, @NonNull IAdapter<FiveDayWeather> adapter, @NonNull FiveDayWeather item, int position) {
                Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
                intent.putExtra(Constants.FIVE_DAY_WEATHER_ITEM, item);
                startActivity(intent);
                return true;
            }
        });
    }

    private void initRecyclerViewMultipleDays() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.contentMainLayout.recyclerViewToday.setLayoutManager(layoutManager);
        mItemAdapterMultipleDays = new ItemAdapter<>();
        mFastAdapterMultipleDays = FastAdapter.with(mItemAdapterMultipleDays);
        binding.contentMainLayout.recyclerViewToday.setItemAnimator(new DefaultItemAnimator());
        binding.contentMainLayout.recyclerViewToday.setAdapter(mFastAdapterMultipleDays);
    }

    private void showStoredMultipleDaysWeather() {
        Query<MultipleDaysWeather> query = DbUtil.getMultipleDaysWeatherQuery(multipleDaysWeatherBox);
        query.subscribe().on(AndroidScheduler.mainThread())
                .observer(new DataObserver<List<MultipleDaysWeather>>() {
                    @Override
                    public void onData(@NonNull List<MultipleDaysWeather> data) {
                        if (data.size() > 0) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    data.remove(0);
                                    mItemAdapterMultipleDays.clear();
                                    mItemAdapterMultipleDays.add(data);
                                }
                            }, 500);
                        }
                    }
                });
    }

    private void showStoredCurrentWeather() {
        Query<CurrentWeather> query = DbUtil.getCurrentWeatherQuery(currentWeatherBox);
        query.subscribe(subscriptions).on(AndroidScheduler.mainThread())
                .observer(new DataObserver<List<CurrentWeather>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onData(@NonNull List<CurrentWeather> data) {
                        if (data.size() > 0) {
                            hideEmptyLayout();
                            Date d = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd MMM E", Locale.getDefault());
                            String formattedDate = df.format(d);

                            CurrentWeather currentWeather = data.get(0);
                            if (isLoad) {
                                binding.contentMainLayout.tempTextView.setText(String.format(Locale.getDefault(), "%.0f°C", currentWeather.getTemp()));
                                binding.contentMainLayout.descriptionTextView.setText(AppUtil.getWeatherStatus(currentWeather.getWeatherId(), AppUtil.isRTL(MainActivity.this)));
                                binding.contentMainLayout.humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", currentWeather.getHumidity()));
                                binding.contentMainLayout.windTextView.setText(String.format(Locale.getDefault(), getResources().getString(R.string.wind_unit_label), currentWeather.getWindSpeed()));
                                binding.contentMainLayout.tvLocation.setText(weatherViewModel.getCityInfoCurrent().getValue().getName());
                                binding.contentMainLayout.dateMaxMixTemp.setText(formattedDate);
                            } else {
                                binding.contentMainLayout.tempTextView.setCurrentText(String.format(Locale.getDefault(), "%.0f°C", currentWeather.getTemp()));
                                binding.contentMainLayout.descriptionTextView.setCurrentText(AppUtil.getWeatherStatus(currentWeather.getWeatherId(), AppUtil.isRTL(MainActivity.this)));
                                binding.contentMainLayout.humidityTextView.setCurrentText(String.format(Locale.getDefault(), "%d%%", currentWeather.getHumidity()));
                                binding.contentMainLayout.windTextView.setCurrentText(String.format(Locale.getDefault(), getResources().getString(R.string.wind_unit_label), currentWeather.getWindSpeed()));
                                binding.contentMainLayout.tvLocation.setCurrentText(weatherViewModel.getCityInfoCurrent().getValue().getName());
                                binding.contentMainLayout.dateMaxMixTemp.setCurrentText(formattedDate);
                            }
                            binding.contentMainLayout.animationView.setAnimation(AppUtil.getWeatherAnimation(currentWeather.getWeatherId()));
                            binding.contentMainLayout.animationView.playAnimation();
                        }
                    }
                });
    }

    private void showStoredFiveDayWeather() {
        Query<FiveDayWeather> query = DbUtil.getFiveDayWeatherQuery(fiveDayWeatherBox);
        query.subscribe(subscriptions).on(AndroidScheduler.mainThread())
                .observer(new DataObserver<List<FiveDayWeather>>() {
                    @Override
                    public void onData(@NonNull List<FiveDayWeather> data) {
                        if (data.size() > 0) {
                            todayFiveDayWeather = data.remove(0);
                            mItemAdapter.clear();
                            mItemAdapter.add(data);
                        }
                    }
                });
    }

    private void checkLastUpdate() {
        cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
        if (cityInfo != null) {
            weatherViewModel.setCityInfoCurrent(cityInfo);
            //binding.contentMainLayout.cityNameTextView.setText(String.format("%s, %s", cityInfo.getName(), cityInfo.getCountry()));
            if (prefser.contains(Constants.LAST_STORED_CURRENT)) {
                long lastStored = prefser.get(Constants.LAST_STORED_CURRENT, Long.class, 0L);
                if (AppUtil.isTimePass(lastStored)) {
                    requestWeather(cityInfo.getName(), false);
                }
            } else {
                requestWeather(cityInfo.getName(), false);
            }
        } else {
            showEmptyLayout();
        }

    }

    private void checkTimePass() {
        apiKey = getResources().getString(R.string.open_weather_map_api);
        if (prefser.contains(Constants.LAST_STORED_MULTIPLE_DAYS)) {
            requestWeather();
        } else {
            checkCityInfoExist();
        }
    }

    private void requestWeather() {
        long lastUpdate = prefser.get(Constants.LAST_STORED_MULTIPLE_DAYS, Long.class, 0L);
        if (AppUtil.isTimePass(lastUpdate)) {
            checkCityInfoExist();
        } else {
            binding.swipeContainer.setRefreshing(false);
        }
    }

    private void checkCityInfoExist() {
        CityInfo cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
        if (cityInfo != null) {
            if (AppUtil.isNetworkConnected()) {
                requestWeathers(cityInfo.getName());
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
                binding.swipeContainer.setRefreshing(false);
            }
        }
    }

    private void requestWeathers(String cityName) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        disposable.add(
                apiService.getMultipleDaysWeather(
                                cityName, Constants.UNITS, defaultLang, 16, apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<MultipleDaysWeatherResponse>() {
                            @Override
                            public void onSuccess(MultipleDaysWeatherResponse response) {
                                handleMultipleDaysResponse(response);
                                binding.swipeContainer.setRefreshing(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.swipeContainer.setRefreshing(false);
                                Log.e("MainActivity", "onError: " + e.getMessage());
                            }
                        })
        );
    }

    private void handleMultipleDaysResponse(MultipleDaysWeatherResponse response) {
        multipleDaysWeatherBox.removeAll();
        List<ListItem> listItems = response.getList();
        for (ListItem listItem : listItems) {
            MultipleDaysWeather multipleDaysWeather = new MultipleDaysWeather();
            multipleDaysWeather.setDt(listItem.getDt());
            multipleDaysWeather.setMaxTemp(listItem.getTemp().getMax());
            multipleDaysWeather.setMinTemp(listItem.getTemp().getMin());
            multipleDaysWeather.setWeatherId(listItem.getWeather().get(0).getId());
            multipleDaysWeatherBox.put(multipleDaysWeather);
        }
        prefser.put(Constants.LAST_STORED_MULTIPLE_DAYS, System.currentTimeMillis());
    }

    private void requestWeather(String cityName, boolean isSearch) {
        if (AppUtil.isNetworkConnected()) {
            getCurrentWeather(cityName, isSearch);
            getCurrentWeatherForLatLon(21.0282, 105.8542);
            getFiveDaysWeather(cityName);
        } else {
            SnackbarUtil
                    .with(binding.swipeContainer)
                    .setMessage(getString(R.string.no_internet_message))
                    .setDuration(SnackbarUtil.LENGTH_LONG)
                    .showError();
            binding.swipeContainer.setRefreshing(false);
        }
    }

    private void getCurrentWeather(String cityName, boolean isSearch) {
        apiKey = getResources().getString(R.string.open_weather_map_api);
        disposable.add(
                apiService.getCurrentWeather(
                                cityName, Constants.UNITS, defaultLang, apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<CurrentWeatherResponse>() {
                            @Override
                            public void onSuccess(CurrentWeatherResponse currentWeatherResponse) {
                                isLoad = true;
                                storeCurrentWeather(currentWeatherResponse);
                                storeCityInfo(currentWeatherResponse);
                                binding.swipeContainer.setRefreshing(false);
                                if (isSearch) {
                                    prefser.remove(Constants.LAST_STORED_MULTIPLE_DAYS);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.swipeContainer.setRefreshing(false);
                                try {
                                    HttpException error = (HttpException) e;
                                    handleErrorCode(error);
                                } catch (Exception exception) {
                                    e.printStackTrace();
                                }
                            }
                        })

        );
    }

    private void getCurrentWeatherForLatLon(double lat, double lon) {
        apiKey = getResources().getString(R.string.open_weather_map_api);
        disposable.add(
                apiService.getCurrentWeatherForLatLon(
                                lat, lon, apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<CurrentWeatherResponse>() {
                            @Override
                            public void onSuccess(CurrentWeatherResponse currentWeatherResponse) {
                                isLoad = true;
//                        storeCurrentWeather(currentWeatherResponse);
//                        storeCityInfo(currentWeatherResponse);
                                Log.d("aaa", currentWeatherResponse.getBase());
                                Log.d("aaa", currentWeatherResponse.getName());
                                for (WeatherItem item : currentWeatherResponse.getWeather())
                                    Log.d("aaa_item", item.getDescription());
                                binding.swipeContainer.setRefreshing(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.swipeContainer.setRefreshing(false);
                                try {
                                    HttpException error = (HttpException) e;
                                    handleErrorCode(error);
                                } catch (Exception exception) {
                                    e.printStackTrace();
                                }
                            }
                        })

        );
    }


    private void handleErrorCode(HttpException error) {
        if (error.code() == 404) {
            SnackbarUtil
                    .with(binding.swipeContainer)
                    .setMessage(getString(R.string.no_city_found_message))
                    .setDuration(SnackbarUtil.LENGTH_INDEFINITE)
                    .setAction(getResources().getString(R.string.search_label), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //binding.toolbarLayout.searchView.showSearch();
                        }
                    })
                    .showWarning();

        } else if (error.code() == 401) {
            SnackbarUtil
                    .with(binding.swipeContainer)
                    .setMessage(getString(R.string.invalid_api_key_message))
                    .setDuration(SnackbarUtil.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.ok_label), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    })
                    .showError();

        } else {
            SnackbarUtil
                    .with(binding.swipeContainer)
                    .setMessage(getString(R.string.network_exception_message))
                    .setDuration(SnackbarUtil.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.retry_label), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cityInfo != null) {
                                requestWeather(cityInfo.getName(), false);
                            } else {
                                //binding.toolbarLayout.searchView.showSearch();
                            }
                        }
                    })
                    .showWarning();
        }
    }

    private void showEmptyLayout() {
        Glide.with(MainActivity.this).load(R.drawable.no_city).into(binding.contentEmptyLayout.noCityImageView);
        binding.contentEmptyLayout.emptyLayout.setVisibility(View.VISIBLE);
        binding.contentMainLayout.nestedScrollView.setVisibility(View.GONE);
    }

    private void hideEmptyLayout() {
        binding.contentEmptyLayout.emptyLayout.setVisibility(View.GONE);
        binding.contentMainLayout.nestedScrollView.setVisibility(View.VISIBLE);
    }


    private void storeCurrentWeather(CurrentWeatherResponse response) {
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setTemp(response.getMain().getTemp());
        currentWeather.setHumidity(response.getMain().getHumidity());
        currentWeather.setDescription(response.getWeather().get(0).getDescription());
        currentWeather.setMain(response.getWeather().get(0).getMain());
        currentWeather.setWeatherId(response.getWeather().get(0).getId());
        currentWeather.setWindDeg(response.getWind().getDeg());
        currentWeather.setWindSpeed(response.getWind().getSpeed());
        currentWeather.setStoreTimestamp(System.currentTimeMillis());
        prefser.put(Constants.LAST_STORED_CURRENT, System.currentTimeMillis());
        if (!currentWeatherBox.isEmpty()) {
            currentWeatherBox.removeAll();
            currentWeatherBox.put(currentWeather);
        } else {
            currentWeatherBox.put(currentWeather);
        }
    }

    private void storeCityInfo(CurrentWeatherResponse response) {
        CityInfo cityInfo = new CityInfo();
        cityInfo.setCountry(response.getSys().getCountry());
        cityInfo.setId(response.getId());
        cityInfo.setName(response.getName());
        prefser.put(Constants.CITY_INFO, cityInfo);
        weatherViewModel.setCityInfoCurrent(cityInfo);
        //binding.toolbarLayout.cityNameTextView.setText(String.format("%s, %s", cityInfo.getName(), cityInfo.getCountry()));
    }

    private void getFiveDaysWeather(String cityName) {
        disposable.add(
                apiService.getMultipleDaysWeather(
                                cityName, Constants.UNITS, defaultLang, 5, apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<MultipleDaysWeatherResponse>() {
                            @Override
                            public void onSuccess(MultipleDaysWeatherResponse response) {
                                handleFiveDayResponse(response, cityName);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
    }

    private void handleFiveDayResponse(MultipleDaysWeatherResponse response, String cityName) {
        fiveDayWeathers = new ArrayList<>();
        List<ListItem> list = response.getList();
        int day = 0;
        for (ListItem item : list) {
            int color = colors[day];
            int colorAlpha = colorsAlpha[day];
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            Calendar newCalendar = AppUtil.addDays(calendar, day);
            FiveDayWeather fiveDayWeather = new FiveDayWeather();
            fiveDayWeather.setWeatherId(item.getWeather().get(0).getId());
            fiveDayWeather.setDt(item.getDt());
            fiveDayWeather.setMaxTemp(item.getTemp().getMax());
            fiveDayWeather.setMinTemp(item.getTemp().getMin());
            fiveDayWeather.setTemp(item.getTemp().getDay());
            fiveDayWeather.setColor(color);
            fiveDayWeather.setColorAlpha(colorAlpha);
            fiveDayWeather.setTimestampStart(AppUtil.getStartOfDayTimestamp(newCalendar));
            fiveDayWeather.setTimestampEnd(AppUtil.getEndOfDayTimestamp(newCalendar));
            fiveDayWeathers.add(fiveDayWeather);
            day++;
        }
        getFiveDaysHourlyWeather(cityName);
    }

    private void getFiveDaysHourlyWeather(String cityName) {
        disposable.add(
                apiService.getFiveDaysWeather(
                                cityName, Constants.UNITS, defaultLang, apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<FiveDayResponse>() {
                            @Override
                            public void onSuccess(FiveDayResponse response) {
                                handleFiveDayHourlyResponse(response);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        })

        );
    }

    private void handleFiveDayHourlyResponse(FiveDayResponse response) {
        if (!fiveDayWeatherBox.isEmpty()) {
            fiveDayWeatherBox.removeAll();
        }
        if (!itemHourlyDBBox.isEmpty()) {
            itemHourlyDBBox.removeAll();
        }
        for (FiveDayWeather fiveDayWeather : fiveDayWeathers) {
            long fiveDayWeatherId = fiveDayWeatherBox.put(fiveDayWeather);
            ArrayList<ItemHourly> listItemHourlies = new ArrayList<>(response.getList());
            for (ItemHourly itemHourly : listItemHourlies) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                calendar.setTimeInMillis(itemHourly.getDt() * 1000L);
                if (calendar.getTimeInMillis()
                        <= fiveDayWeather.getTimestampEnd()
                        && calendar.getTimeInMillis()
                        > fiveDayWeather.getTimestampStart()) {
                    ItemHourlyDB itemHourlyDB = new ItemHourlyDB();
                    itemHourlyDB.setDt(itemHourly.getDt());
                    itemHourlyDB.setFiveDayWeatherId(fiveDayWeatherId);
                    itemHourlyDB.setTemp(itemHourly.getMain().getTemp());
                    itemHourlyDB.setWeatherCode(itemHourly.getWeather().get(0).getId());
                    itemHourlyDBBox.put(itemHourlyDB);
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        binding.contentMainLayout.searchView.setMenuItem(item);
        return true;
    }

    public void showAboutFragment() {
        AppUtil.showFragment(new AboutFragment(), getSupportFragmentManager(), true);
    }

    @Override
    public void onBackPressed() {
        if (binding.contentMainLayout.searchView.isSearchOpen()) {
            binding.contentMainLayout.searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
