package pl.lodz.p.astroweather.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Place extends RealmObject implements Parcelable {

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel source) {
            return new Place(source);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    private String name;
    private String country;
    private String woeid;
    private Centroid centroid;
    @PrimaryKey
    private String id;

    public Place() {
    }

    protected Place(Parcel in) {
        this.name = in.readString();
        this.country = in.readString();
        this.woeid = in.readString();
        this.centroid = in.readParcelable(Centroid.class.getClassLoader());
        this.id = in.readString();
    }

    public static RealmResults<Place> getAll() {
        final RealmQuery<Place> query = Realm.getDefaultInstance().where(Place.class);
        return query.findAll();
    }

    @Override
    public String toString() {
        return name + ", " + country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWoeid() {
        return woeid;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public Centroid getCentroid() {
        return centroid;
    }

    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
    }

    public void save() {
        id = UUID.randomUUID().toString();
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(this);
        realm.commitTransaction();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.country);
        dest.writeString(this.woeid);
        dest.writeParcelable(this.centroid, flags);
        dest.writeString(this.id);
    }
}
