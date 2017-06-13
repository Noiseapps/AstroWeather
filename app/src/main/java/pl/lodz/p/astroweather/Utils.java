package pl.lodz.p.astroweather;

import com.astrocalculator.AstroDateTime;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    public static final int MILLIS_IN_AN_HOUR = 3600000;

    /**
     * Converts AstroDateTime object to format 'yyyy-mm-dd HH:MM:SS'
     */
    public static String formatAstroDateToString(AstroDateTime dateTime) {
//        %02d - liczba całkowita, 2 miejsca, dodajemy 0 na początku, jeśli mniej niż 10
        return String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:%02d",
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDay(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond());
    }

    /**
     * Converts AstroDateTime object to format 'HH:MM'
     */
    public static String formatAstroDateToStringTimeOnly(AstroDateTime dateTime) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                dateTime.getHour(),
                dateTime.getMinute());
    }

    /**
     * Converts AstroDateTime object to format 'yyyy-mm-dd'
     */
    public static String formatAstroDateToStringDateOnly(AstroDateTime dateTime) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDay());
    }

    /**
     * Returns AstroDateTime object initialized with current time
     *
     * @return
     */
    public static AstroDateTime getCurrentAstroDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        return new AstroDateTime(calendar.get(Calendar.YEAR),
//                +1 bo miesiące Java liczy od 0
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.getTimeZone().getRawOffset() / MILLIS_IN_AN_HOUR,
                calendar.getTimeZone().useDaylightTime());
    }

    public static YahooWeatherApi getWeatherApi() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().
                readTimeout(5, TimeUnit.SECONDS).
                addInterceptor(loggingInterceptor).build();
        return new Retrofit.Builder().
                client(client).
                baseUrl("https://query.yahooapis.com/v1/public/").
                addConverterFactory(GsonConverterFactory.create()).
                build().create(YahooWeatherApi.class);
    }
}
