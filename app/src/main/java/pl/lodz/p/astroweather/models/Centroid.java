package pl.lodz.p.astroweather.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Centroid extends RealmObject implements Parcelable {

    public static final Parcelable.Creator<Centroid> CREATOR = new Parcelable.Creator<Centroid>() {
        @Override
        public Centroid createFromParcel(Parcel source) {
            return new Centroid(source);
        }

        @Override
        public Centroid[] newArray(int size) {
            return new Centroid[size];
        }
    };
    @PrimaryKey
    private String id;
    private String longitude;
    private String latitude;

    public Centroid() {
        id = UUID.randomUUID().toString();
    }

    public Centroid(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected Centroid(Parcel in) {
        this.longitude = in.readString();
        this.latitude = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return Double.parseDouble(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return Double.parseDouble(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.longitude);
        dest.writeString(this.latitude);
    }
}
