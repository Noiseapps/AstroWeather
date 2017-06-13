package pl.lodz.p.astroweather;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {
    public static final String KEY_UNIT = "unit";
    private final Context context;
    private SharedPreferences preferences;

    public SharedPrefHelper(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    public String getUnit() {
        return preferences.getString(KEY_UNIT, "c");
    }

    public void setUnit(String unit) {
        preferences.edit().putString(KEY_UNIT, unit).apply();
    }

    public int getRefreshTime() {
        return preferences.getInt("refresh", -1);
    }

    public void setRefreshTime(int refreshTime) {
        preferences.edit().putInt("refresh", refreshTime).apply();
    }
}
