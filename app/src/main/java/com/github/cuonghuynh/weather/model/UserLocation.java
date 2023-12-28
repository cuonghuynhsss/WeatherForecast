package com.github.cuonghuynh.weather.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
//implement in future
public class UserLocation implements Parcelable{

    private User user;
    private int geo_point;
    private  Date timestamp;

    public UserLocation(User user, int geo_point, Date timestamp) {
        this.user = user;
        this.geo_point = geo_point;
        this.timestamp = timestamp;
    }

    public UserLocation() {

    }

    protected UserLocation(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(int geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + geo_point +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
    }
}
