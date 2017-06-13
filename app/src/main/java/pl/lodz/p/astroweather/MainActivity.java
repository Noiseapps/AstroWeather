package pl.lodz.p.astroweather;

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

import java.util.Locale;

import io.realm.RealmResults;
import pl.lodz.p.astroweather.models.BaseResponse;
import pl.lodz.p.astroweather.models.Centroid;
import pl.lodz.p.astroweather.models.Place;
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

    private double userLongitude = -1001;
    private double userLatitude = -1001;
    private Handler tickerHandler;
    private int updateFrequency = -1;
    private Place selectedPlace;
    private AstroDateTime astroDateTime;
    private AstroCalculator astroCalculator;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;
    private FragmentsAdapter pagerAdapter;
    private TextView timeValue;
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
    private Runnable dataRefreshTicker = new Runnable() {
        @Override
        public void run() {
            updateFragments();
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, updateFrequency * MINUTE_DELAY);
            }
        }
    };
    private FrameLayout sunContainer;
    private FrameLayout moonContainer;
    private ViewPager viewPager;

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
        } else {
            userLatitude = -1001;
            userLongitude = -1001;
        }

        timeValue = (TextView) findViewById(R.id.timeValue);
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
    protected void onStart() {
        super.onStart();
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
            case R.id.menuUpdateFrequency:
                updateFrequency();
                break;
        }
        return true;
    }

    private void setLocation() {
        final RealmResults<Place> placeList = Place.getAll();
        final String[] places = new String[placeList.size()];
        for (int i = 0; i < placeList.size(); i++) {
            places[i] = placeList.get(i).toString();
        }
        new AlertDialog.Builder(this).
                setTitle(R.string.setUserLocation).
                setItems(places, new DialogInterface.OnClickListener() {
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
                                        final Place place = body.getQuery().getResults().getPlace();
                                        if (place.getName() == null) {
                                            errorView.setVisibility(View.VISIBLE);
                                            errorView.setText(getString(R.string.failedToFindLocation));
                                        } else {
                                            place.save();
                                            selectPlace(place);
                                            alert.dismiss();
                                            Toast.makeText(MainActivity.this, R.string.newPlaceAdded, Toast.LENGTH_LONG).show();
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
        View dialogBody = LayoutInflater.from(this).inflate(R.layout.dialog_update_frequency, null, false);
        final EditText frequencyInput = (EditText) dialogBody.findViewById(R.id.frequencyUpdate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(R.string.update_frequency).
                setMessage(R.string.setUpdateFrequency).
                setCancelable(false).
                setPositiveButton(R.string.save, null).
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
                        if (!frequencyInput.getText().toString().isEmpty()) {
                            int frequencyMinutes = Integer.parseInt(frequencyInput.getText().toString());
                            if (frequencyMinutes > 0) {
                                setUpdateFrequency(frequencyMinutes);
                                alert.dismiss();
                            } else {
                                frequencyInput.setError(getString(R.string.field_invalid));
                            }
                        } else {
                            frequencyInput.setError(getString(R.string.field_required));
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

    private void configureFragments() {
        if (sunFragment == null) {
            sunFragment = new SunFragment();
        }
        if (moonFragment == null) {
            moonFragment = new MoonFragment();
        }

        if (sunContainer != null && moonContainer != null) {
//            widok tabletu
            getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.sunFragmentContainer, sunFragment).
                    replace(R.id.moonFragmentContainer, moonFragment).
                    commitAllowingStateLoss();
        } else {
//             widok telefonu
            pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), sunFragment, moonFragment);
            viewPager.setAdapter(pagerAdapter);
        }
    }

    private static class FragmentsAdapter extends FragmentStatePagerAdapter {
        private SunFragment sunFragment;
        private MoonFragment moonFragment;

        FragmentsAdapter(FragmentManager fm, SunFragment sunFragment, MoonFragment moonFragment) {
            super(fm);
            this.sunFragment = sunFragment;
            this.moonFragment = moonFragment;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return sunFragment;
            }
            return moonFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
