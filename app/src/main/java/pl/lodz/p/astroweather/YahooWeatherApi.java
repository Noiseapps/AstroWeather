package pl.lodz.p.astroweather;

import pl.lodz.p.astroweather.models.BaseResponse;
import pl.lodz.p.astroweather.models.WoeidResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YahooWeatherApi {

    @GET("yql?format=json")
    Call<BaseResponse<WoeidResponse>> getWoeid(@Query("q") String query);

    @GET("yql?format=json")
    Call<Void> getWeather(@Query("q") String query);
}
