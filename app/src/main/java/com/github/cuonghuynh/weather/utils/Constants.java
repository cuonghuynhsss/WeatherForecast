package com.github.cuonghuynh.weather.utils;

public class Constants {
  public static final Integer NAVIGATION_HOME_ID = 1;
  public static final Integer NAVIGATION_CHAT_ID = 2;
  public static final Integer NAVIGATION_MAP_ID = 3;
  public static final Integer NAVIGATION_SETTING_ID = 4;
  public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
  public static final String UNITS = "metric";
  public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
  public static final String[] DAYS_OF_WEEK = {
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday"
  };
  public static final String[] MONTH_NAME = {
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December"
  };

  public static final String[] DAYS_OF_WEEK_VIET_NAM = {
      "Chủ Nhật",
      "Thứ 2",
      "Thứ 3",
      "Thứ 4",
      "Thứ 5",
      "Thứ 6",
      "Thứ 7"
  };
  public static final String[] MONTH_NAME_VIET_NAM = {
      "Tháng 1",
      "Tháng 2",
      "Tháng 3",
      "Tháng 4",
      "Tháng 5",
      "Tháng 6",
      "Tháng 7",
      "Tháng 8",
      "Tháng 9",
      "Tháng 10",
      "Tháng 11",
      "Tháng 12"
  };

  public static final String[] WEATHER_STATUS = {
      "Thunderstorm",
      "Drizzle",
      "Rain",
      "Snow",
      "Atmosphere",
      "Clear",
      "Few Clouds",
      "Broken Clouds",
      "Cloud"
  };

  public static final String[] WEATHER_STATUS_VIET_NAM = {
      "Dông",
      "Mưa phùn",
      "Mưa",
      "Tuyết",
      "Có gió",
      "Thông thoáng",
      "Ít mây",
      "Mây rải rác",
      "Có mây"

  };


  public static final String CITY_INFO = "city-info";

  public static final long TIME_TO_PASS = 6 * 600000;

  public static final String LAST_STORED_CURRENT = "last-stored-current";
  public static final String LAST_STORED_MULTIPLE_DAYS = "last-stored-multiple-days";
  public static final String OPEN_WEATHER_MAP_WEBSITE = "https://home.openweathermap.org/api_keys";

  public static final String API_KEY = "c0d290eeee9dd399b017a6d2ba64be7e";
  public static final String LANGUAGE = "language";
  public static final String DARK_THEME = "dark-theme";
  public static final String FIVE_DAY_WEATHER_ITEM = "five-day-weather-item";
}
