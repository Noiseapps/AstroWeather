package pl.lodz.p.astroweather;

import android.content.Context;

import pl.lodz.p.astroweather.models.BaseResponse;
import pl.lodz.p.astroweather.models.WoeidResponse;
import retrofit2.Callback;

public class RetrofitHelper {

    public static final RetrofitHelper INSTANCE = new RetrofitHelper();

    private final YahooWeatherApi api;
    private RetrofitHelper() {
        api = Utils.getWeatherApi();
    }

    public void getWoeid(String query, Callback<BaseResponse<WoeidResponse>> callback) {
        final String yqlQuery = "select name,country.content,woeid,centroid.longitude,centroid.latitude from geo.places(1) where text= \"" + query + "\"";
        api.getWoeid(yqlQuery).enqueue(callback);
    }

    public void readWeatherData(Context context, int woeid, Callback<Void> callback) {
        final String unit = new SharedPrefHelper(context).getUnit();
        final String yqlQuery = "select * from geo.places(1) where woeid = " + woeid + "and u=\"" + unit + "\"";
        api.getWeather(yqlQuery).enqueue(callback);
    }
}
