package pl.lodz.p.astroweather;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Locale;

import io.realm.RealmResults;
import pl.lodz.p.astroweather.fragments.MoonFragment;
import pl.lodz.p.astroweather.fragments.SunFragment;
import pl.lodz.p.astroweather.fragments.WeatherBasicDataFragment;
import pl.lodz.p.astroweather.fragments.WeatherForecastFragment;
import pl.lodz.p.astroweather.models.BaseResponse;
import pl.lodz.p.astroweather.models.Centroid;
import pl.lodz.p.astroweather.models.Place;
import pl.lodz.p.astroweather.models.Query;
import pl.lodz.p.astroweather.models.WeatherResponse;
import pl.lodz.p.astroweather.models.WoeidResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int SECOND_DELAY = 1000;
    public static final int MINUTE_DELAY = 60 * SECOND_DELAY;
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_PLACE = "Place";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_FREQUENCY = "Frequency";
    public static final String KEY_SUN_FRAGMENT = "SunFragment";
    public static final String KEY_MOON_FRAGMENT = "MoonFragment";
    public static final String KEY_BASIC_DATA = "BasicDataFragment";
    public static final String KEY_ADDITIONAL_DATA = "AdditionalDataFragment";
    public static final String KEY_FORECAST = "ForecastFragment";

    private double userLongitude = -1001;
    private double userLatitude = -1001;
    private Handler tickerHandler;
    private int updateFrequency = -1;
    private Place selectedPlace;
    private AstroDateTime astroDateTime;
    private AstroCalculator astroCalculator;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;
    private WeatherBasicDataFragment basicDataFragment;
    //    private WeatherAdditionalDataFragment additionalDataFragment;
    private WeatherForecastFragment forecastFragment;
    private FragmentsAdapter pagerAdapter;
    private TextView timeValue;
    private TextView currentDataUnavailable;
    private Runnable timeTicker = new Runnable() {
        @Override
        public void run() {
            astroDateTime = Utils.getCurrentAstroDateTime();
            if (astroCalculator != null) {
                astroCalculator.setDateTime(astroDateTime);
            }
            MainActivity.this.timeValue.setText(Utils.formatAstroDateToString(astroDateTime));
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, SECOND_DELAY);
            }
        }
    };
    private TextView userLocation;
    private TextView locationName;
    private FrameLayout sunContainer;
    private FrameLayout moonContainer;
    private ViewPager viewPager;
    private boolean isReadingData;
    private AlertDialog frequencyAlert;
    private Runnable dataRefreshTicker = new Runnable() {
        @Override
        public void run() {
            updateFragments();
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, updateFrequency * MINUTE_DELAY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            userLatitude = savedInstanceState.getDouble(KEY_LATITUDE, -1001);
            selectedPlace = savedInstanceState.getParcelable(KEY_PLACE);
            userLongitude = savedInstanceState.getDouble(KEY_LONGITUDE, -1001);
            updateFrequency = savedInstanceState.getInt(KEY_FREQUENCY, -1);
            moonFragment = (MoonFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_MOON_FRAGMENT);
            sunFragment = (SunFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_SUN_FRAGMENT);
            basicDataFragment = (WeatherBasicDataFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_BASIC_DATA);
//            additionalDataFragment = (WeatherAdditionalDataFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_ADDITIONAL_DATA);
            forecastFragment = (WeatherForecastFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_FORECAST);
        } else {
            userLatitude = -1001;
            userLongitude = -1001;
        }

        timeValue = (TextView) findViewById(R.id.timeValue);
        currentDataUnavailable = (TextView) findViewById(R.id.currentDataUnavailable);
        locationName = (TextView) findViewById(R.id.locationName);
        userLocation = (TextView) findViewById(R.id.locationValue);
        userLocation.setText(R.string.not_set);

        sunContainer = (FrameLayout) findViewById(R.id.sunFragmentContainer);
        moonContainer = (FrameLayout) findViewById(R.id.moonFragmentContainer);
        viewPager = (ViewPager) findViewById(R.id.fragmentsPager);

        this.configureFragments();
        astroDateTime = Utils.getCurrentAstroDateTime();

        tickerHandler = new Handler();
        tickerHandler.postDelayed(timeTicker, SECOND_DELAY);

        updateFrequency = new SharedPrefHelper(this).getRefreshTime();
        if (selectedPlace != null) {
            selectPlace(selectedPlace);
        } else {
            final RealmResults<Place> places = Place.getAll();
            if (places.size() > 0) {
                selectPlace(places.first());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setUserLocation(userLatitude, userLongitude);
    }

    @Override
    protected void onDestroy() {
        tickerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LATITUDE, userLatitude);
        outState.putDouble(KEY_LONGITUDE, userLongitude);
        outState.putInt(KEY_FREQUENCY, updateFrequency);
        getSupportFragmentManager().putFragment(outState, KEY_MOON_FRAGMENT, moonFragment);
        getSupportFragmentManager().putFragment(outState, KEY_SUN_FRAGMENT, sunFragment);
        getSupportFragmentManager().putFragment(outState, KEY_BASIC_DATA, basicDataFragment);
//        getSupportFragmentManager().putFragment(outState, KEY_ADDITIONAL_DATA, additionalDataFragment);
        getSupportFragmentManager().putFragment(outState, KEY_FORECAST, forecastFragment);
        super.onSaveInstanceState(outState);
    }

    public void setUserLocation(double latitude, double longitude) {
        tickerHandler.removeCallbacks(dataRefreshTicker);
        userLatitude = latitude;
        userLongitude = longitude;
        if (latitude >= 0 && latitude <= 90 && Math.abs(longitude) <= 180) {
            astroCalculator = new AstroCalculator(astroDateTime, new AstroCalculator.Location(latitude, longitude));
            userLocation.setText(String.format(Locale.getDefault(), "Szer.: %f, \nDł.: %f", latitude, longitude));
        }
        updateFragments();
    }

    public void setUpdateFrequency(int updateFrequencyMinutes) {
        tickerHandler.removeCallbacks(dataRefreshTicker);
        updateFrequency = updateFrequencyMinutes;
        tickerHandler.post(dataRefreshTicker);
        updateFragments();
    }

    public void updateFragments() {
        if (userLatitude < 0 || userLongitude < -180) {
            tickerHandler.removeCallbacks(dataRefreshTicker);
            addLocation();
            return;
        }

        if (updateFrequency < 0) {
            tickerHandler.removeCallbacks(dataRefreshTicker);
            updateFrequency();
            return;
        }
        sunFragment.update(astroCalculator);
        moonFragment.update(astroCalculator);
        refreshDataForSelectedPlace();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAddLocation:
                addLocation();
                break;
            case R.id.menuSetLocation:
                setLocation();
                break;
            case R.id.menuSwitchUnit:
                setUnit();
                break;
            case R.id.menuRefresh:
                refreshDataForSelectedPlace();
                break;
            case R.id.menuUpdateFrequency:
                updateFrequency();
                break;
        }
        return true;
    }

    private void setUnit() {
        final SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(MainActivity.this);
        final String[] units = {"Celsius", "Fahrenheit"};
        int selectedItem = 0;
        if (sharedPrefHelper.getUnit().equalsIgnoreCase("f")) {
            selectedItem = 1;
        }
        new AlertDialog.Builder(this).
                setTitle(R.string.setUserLocation).
                setSingleChoiceItems(units, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPrefHelper.setUnit(units[which].substring(0, 1).toLowerCase());
                        dialog.dismiss();
                        refreshDataForSelectedPlace();
                    }
                }).
                setCancelable(false).
                setNegativeButton(R.string.cancel, null).show();
    }

    private void setLocation() {
        final RealmResults<Place> placeList = Place.getAll();
        final String[] places = new String[placeList.size()];
        int selectedItem = 0;
        for (int i = 0; i < placeList.size(); i++) {
            places[i] = placeList.get(i).toString();
            if (selectedPlace != null && placeList.get(i).getId().equalsIgnoreCase(selectedPlace.getId())) {
                selectedItem = i;
            }
        }
        new AlertDialog.Builder(this).
                setTitle(R.string.setUserLocation).
                setSingleChoiceItems(places, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectPlace(placeList.get(which));
                        dialog.dismiss();
                    }
                }).
                setCancelable(false).
                setNegativeButton(R.string.cancel, null).show();
    }

    private void selectPlace(Place place) {
        selectedPlace = place;
        final Centroid centroid = place.getCentroid();
        setUserLocation(centroid.getLatitude(), centroid.getLongitude());
        locationName.setText(place.toString());
    }

    private void addLocation() {
        View dialogBody = LayoutInflater.from(this).inflate(R.layout.dialog_coordinates, null, false);
        final EditText latitudeInput = (EditText) dialogBody.findViewById(R.id.placeId);
        final ProgressBar progressBar = (ProgressBar) dialogBody.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        final TextView errorView = (TextView) dialogBody.findViewById(R.id.errorLabel);
        errorView.setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(R.string.setUserLocation).
                setPositiveButton(R.string.save, null).
                setCancelable(false).
                setNegativeButton(R.string.cancel, null).
                setView(dialogBody);
        final AlertDialog alert = builder.create();


//        ustawiamy on show, żeby alert nie zamykał się zaraz po kliknięciu na button
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String query = latitudeInput.getText().toString();
                        if (!query.isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);
                            RetrofitHelper.INSTANCE.getWoeid(query, new Callback<BaseResponse<WoeidResponse>>() {
                                @Override
                                public void onResponse(Call<BaseResponse<WoeidResponse>> call, Response<BaseResponse<WoeidResponse>> response) {
                                    progressBar.setVisibility(View.GONE);
                                    final BaseResponse<WoeidResponse> body = response.body();
                                    if (body != null) {
                                        final WoeidResponse woeidResponse = body.getQuery().getResults();
                                        if (woeidResponse == null || woeidResponse.getPlace().getName() == null) {
                                            errorView.setVisibility(View.VISIBLE);
                                            errorView.setText(getString(R.string.failedToFindLocation));
                                        } else {
                                            final Place place = woeidResponse.getPlace();
                                            final Place placeWithWoeid = Place.getPlaceWithWoeid(woeidResponse.getPlace().getWoeid());
                                            if (placeWithWoeid == null) {
                                                place.save();
                                                selectPlace(place);
                                                alert.dismiss();
                                                Toast.makeText(MainActivity.this, R.string.newPlaceAdded, Toast.LENGTH_LONG).show();
                                            } else {
                                                errorView.setVisibility(View.VISIBLE);
                                                errorView.setText(R.string.location_already_added);
                                            }
                                        }
                                    } else {
                                        errorView.setVisibility(View.VISIBLE);
                                        errorView.setText(getString(R.string.requestFailed));
                                    }
                                }

                                @Override
                                public void onFailure(Call<BaseResponse<WoeidResponse>> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    errorView.setVisibility(View.VISIBLE);
                                    errorView.setText(getString(R.string.requestFailed));
                                }
                            });
                        } else {
                            latitudeInput.setError(getString(R.string.field_required));
                        }
                    }
                });

                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });
            }
        });

        alert.show();

    }

    private void updateFrequency() {
        if (frequencyAlert != null) return;
        View dialogBody = LayoutInflater.from(this).inflate(R.layout.dialog_update_frequency, null, false);
        final EditText frequencyInput = (EditText) dialogBody.findViewById(R.id.frequencyUpdate);
        if (updateFrequency > 0) {
            frequencyInput.setText(String.format(Locale.getDefault(), "%d", updateFrequency));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(R.string.update_frequency).
                setMessage(R.string.setUpdateFrequency).
                setCancelable(false).
                setPositiveButton(R.string.save, null).
                setNegativeButton(R.string.cancel, null).
                setView(dialogBody);
        frequencyAlert = builder.create();
        frequencyAlert.setCanceledOnTouchOutside(false);

//        ustawiamy on show, żeby alert nie zamykał się zaraz po kliknięciu na button
        frequencyAlert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                frequencyAlert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!frequencyInput.getText().toString().isEmpty()) {
                            int frequencyMinutes = Integer.parseInt(frequencyInput.getText().toString());
                            if (frequencyMinutes > 0) {
                                frequencyAlert.dismiss();
                                frequencyAlert = null;
                                new SharedPrefHelper(MainActivity.this).setRefreshTime(frequencyMinutes);
                                setUpdateFrequency(frequencyMinutes);
                            } else {
                                frequencyInput.setError(getString(R.string.field_invalid));
                            }
                        } else {
                            frequencyInput.setError(getString(R.string.field_required));
                        }
                    }
                });

                frequencyAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        frequencyAlert = null;
                        frequencyAlert.dismiss();
                    }
                });
            }
        });

        frequencyAlert.show();
    }

    private void refreshDataForSelectedPlace() {
        if (isReadingData) return;
        isReadingData = true;
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.downloading_data));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        RetrofitHelper.INSTANCE.readWeatherData(this, selectedPlace.getWoeid(), new Callback<BaseResponse<WeatherResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<WeatherResponse>> call, Response<BaseResponse<WeatherResponse>> response) {
                isReadingData = false;
                dialog.dismiss();
                updateWeather(response.body().getQuery());
            }

            @Override
            public void onFailure(Call<BaseResponse<WeatherResponse>> call, Throwable t) {
                isReadingData = false;
                dialog.dismiss();
                final Query<WeatherResponse> weatherResponse = readFromFile();
                if (weatherResponse != null) {
                    currentDataUnavailable.setText(R.string.failed_to_read_current_data);
                    updateWeatherFragments(weatherResponse, false);
                } else {
                    currentDataUnavailable.setVisibility(View.VISIBLE);
                    currentDataUnavailable.setText(R.string.no_cached_data);
                }
            }
        });
    }

    private void updateWeatherFragments(Query<WeatherResponse> weatherResponse, boolean isFreshData) {
        currentDataUnavailable.setVisibility(isFreshData ? View.GONE : View.VISIBLE);
        basicDataFragment.update(weatherResponse);
//        additionalDataFragment.update(weatherResponse);
        forecastFragment.update(weatherResponse);
    }

    private void updateWeather(Query<WeatherResponse> response) {
        writeDataToFile(new Gson().toJson(response), selectedPlace.getId());
        updateWeatherFragments(response, true);
    }

    private Query<WeatherResponse> readFromFile() {
        FileInputStream inputStream = null;
        try {
            final File file = new File(getFilesDir(), selectedPlace.getId());
            inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            final Type type = new TypeToken<Query<WeatherResponse>>() {
            }.getType();
            return new Gson().fromJson(sb.toString(), type);
        } catch (Exception ex) {
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeDataToFile(String data, String fileName) {
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(getFilesDir(), fileName);
            fop = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = data.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureFragments() {
        if (sunFragment == null) {
            sunFragment = new SunFragment();
        }
        if (moonFragment == null) {
            moonFragment = new MoonFragment();
        }
        if (basicDataFragment == null) {
            basicDataFragment = new WeatherBasicDataFragment();
        }
//        if (additionalDataFragment == null) {
//            additionalDataFragment = new WeatherAdditionalDataFragment();
//        }
        if (forecastFragment == null) {
            forecastFragment = new WeatherForecastFragment();
        }

        if (sunContainer != null && moonContainer != null) {
//            widok tabletu
            getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.sunFragmentContainer, sunFragment).
                    replace(R.id.moonFragmentContainer, moonFragment).
                    replace(R.id.basicDataContainer, basicDataFragment).
                    replace(R.id.forecastContainer, forecastFragment).
//                    replace(R.id.moonFragmentContainer, moonFragment).
        commitAllowingStateLoss();
        } else {
//             widok telefonu
            final Fragment[] fragments = {sunFragment, moonFragment, basicDataFragment/*, additionalDataFragment*/, forecastFragment};
            pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), fragments);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(fragments.length);
        }
    }

    private static class FragmentsAdapter extends FragmentStatePagerAdapter {
        private Fragment[] fragments;

        FragmentsAdapter(FragmentManager fm, Fragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}
