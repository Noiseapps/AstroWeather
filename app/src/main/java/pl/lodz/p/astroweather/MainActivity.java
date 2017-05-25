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
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_FREQUENCY = "Frequency";

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
            Log.d("Ticker", "tick");
            astroDateTime = Utils.getCurrentAstroDateTime();
            MainActivity.this.timeValue.setText(Utils.formatAstroDateToString(astroDateTime));
            if (tickerHandler != null) {
                tickerHandler.postDelayed(this, SECOND_DELAY);
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
        } else {
            userLatitude = -1001;
            userLongitude = -1001;
        }

        timeValue = (TextView) findViewById(R.id.timeValue);
        userLocation = (TextView) findViewById(R.id.locationValue);

        sunContainer = (FrameLayout) findViewById(R.id.sunFragmentContainer);
        moonContainer = (FrameLayout) findViewById(R.id.moonFragmentContainer);
        viewPager = (ViewPager) findViewById(R.id.fragmentsPager);

        this.configureFragments();
        astroDateTime = Utils.getCurrentAstroDateTime();

        tickerHandler = new Handler();
        tickerHandler.postDelayed(timeTicker, SECOND_DELAY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LATITUDE, userLatitude);
        outState.putDouble(KEY_LONGITUDE, userLongitude);
        outState.putInt(KEY_FREQUENCY, updateFrequency);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.setUserLocation(userLatitude, userLongitude);
    }

    public void setUserLocation(double latitude, double longitude) {
        userLatitude = latitude;
        userLongitude = longitude;
        astroCalculator = new AstroCalculator(astroDateTime, new AstroCalculator.Location(latitude, longitude));
        userLocation.setText(String.format(Locale.getDefault(), "Szer.: %f, Dł.: %f", latitude, longitude));
        updateFragments();
    }

    public void setUpdateFrequency(int updateFrequencyMinutes) {
//        todo alarm
    }

    public void updateFragments() {
        if(userLatitude < 0 || userLongitude < -180) {
            setLocation();
            return;
        }

        if(updateFrequency < 0) {
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
        final EditText longitudeInput = (EditText) dialogBody.findViewById(R.id.longitude);
        final EditText latitudeInput = (EditText) dialogBody.findViewById(R.id.latitude);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(R.string.setUserLocation).
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
                        double longitude = Double.parseDouble(longitudeInput.getText().toString());
                        double latitude = Double.parseDouble(latitudeInput.getText().toString());

                        boolean isValid = true;
                        if(Math.abs(longitude) > 180) {
                            longitudeInput.setError(getString(R.string.invalidLongitudeValue));
                            isValid = false;
                        }

                        if(latitude < 0 || latitude > 90) {
                            latitudeInput.setError(getString(R.string.invalidLatitudeValue));
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
                        int frequencyMinutes = Integer.parseInt(frequencyInput.getText().toString());
                        setUpdateFrequency(frequencyMinutes);
                        alert.dismiss();
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
        sunFragment = new SunFragment();
        moonFragment = new MoonFragment();

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
