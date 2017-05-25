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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int SECOND_DELAY = 1000;
    public static final int MINUTE_DELAY = 60 * SECOND_DELAY;
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_FREQUENCY = "Frequency";
    public static final String KEY_SUN_FRAGMENT = "SunFragment";
    public static final String KEY_MOON_FRAGMENT = "MoonFragment";

    private double userLongitude = -1001;
    private double userLatitude = -1001;
    private Handler tickerHandler;
    private int updateFrequency = -1;
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
            if(astroCalculator != null) {
                astroCalculator.setDateTime(astroDateTime);
            }
            MainActivity.this.timeValue.setText(Utils.formatAstroDateToString(astroDateTime));
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, SECOND_DELAY);
            }
        }
    };
    private Runnable dataRefreshTicker = new Runnable() {
        @Override
        public void run() {
            updateFragments();
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, updateFrequency * MINUTE_DELAY);
            }
        }
    };
    private TextView userLocation;

    private FrameLayout sunContainer;
    private FrameLayout moonContainer;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            userLatitude = savedInstanceState.getDouble(KEY_LATITUDE, -1001);
            userLongitude = savedInstanceState.getDouble(KEY_LONGITUDE, -1001);
            updateFrequency = savedInstanceState.getInt(KEY_FREQUENCY, -1);
            moonFragment = (MoonFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_MOON_FRAGMENT);
            sunFragment =  (SunFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_SUN_FRAGMENT);
        } else {
            userLatitude = -1001;
            userLongitude = -1001;
        }

        timeValue = (TextView) findViewById(R.id.timeValue);
        userLocation = (TextView) findViewById(R.id.locationValue);
        userLocation.setText(R.string.not_set);

        sunContainer = (FrameLayout) findViewById(R.id.sunFragmentContainer);
        moonContainer = (FrameLayout) findViewById(R.id.moonFragmentContainer);
        viewPager = (ViewPager) findViewById(R.id.fragmentsPager);

        this.configureFragments();
        astroDateTime = Utils.getCurrentAstroDateTime();

        tickerHandler = new Handler();
        tickerHandler.postDelayed(timeTicker, SECOND_DELAY);
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
        if(latitude >= 0 && latitude <= 90 && Math.abs(longitude) <= 180) {
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
        if(userLatitude < 0 || userLongitude < -180) {
            tickerHandler.removeCallbacks(dataRefreshTicker);
            setLocation();
            return;
        }

        if(updateFrequency < 0) {
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
        View dialogBody = LayoutInflater.from(this).inflate(R.layout.dialog_coordinates, null, false);
        final EditText latitudeInput = (EditText) dialogBody.findViewById(R.id.latitude);
        final EditText longitudeInput = (EditText) dialogBody.findViewById(R.id.longitude);

        longitudeInput.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);

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
                        boolean isValid = true;

                        double longitude = 0;
                        double latitude = 0;

                        if(!longitudeInput.getText().toString().isEmpty()) {
                            try {
                                longitude = Double.parseDouble(longitudeInput.getText().toString());
                                if(Math.abs(longitude) > 180) {
                                    longitudeInput.setError(getString(R.string.invalidLongitudeValue));
                                    isValid = false;
                                }
                            } catch (Exception e) {
                                longitudeInput.setError(getString(R.string.field_invalid));
                                isValid = false;
                            }
                        } else {
                            longitudeInput.setError(getString(R.string.field_required));
                            isValid = false;
                        }

                        if(!latitudeInput.getText().toString().isEmpty()) {
                            try {
                                latitude = Double.parseDouble(latitudeInput.getText().toString());
                                if(latitude < 0 || latitude > 90) {
                                    latitudeInput.setError(getString(R.string.invalidLatitudeValue));
                                    isValid = false;
                                }
                            } catch (Exception e) {
                                latitudeInput.setError(getString(R.string.field_invalid));
                                isValid = false;
                            }
                        } else {
                            latitudeInput.setError(getString(R.string.field_required));
                            isValid = false;
                        }

                        if(isValid) {
                            setUserLocation(latitude, longitude);
                            alert.dismiss();
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
                        if(!frequencyInput.getText().toString().isEmpty()) {
                            int frequencyMinutes = Integer.parseInt(frequencyInput.getText().toString());
                            if(frequencyMinutes > 0) {
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
        if(sunFragment == null ) {
            sunFragment = new SunFragment();
        }
        if(moonFragment == null) {
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
