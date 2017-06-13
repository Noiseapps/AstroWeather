package pl.lodz.p.astroweather;

import android.app.Application;

import io.realm.Realm;

public class WeatherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
