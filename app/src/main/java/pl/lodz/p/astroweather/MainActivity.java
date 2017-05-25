package pl.lodz.p.astroweather;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

public class MainActivity extends AppCompatActivity {

    public static final int SECOND_DELAY = 1000;
    private Runnable timeTicker = new Runnable() {
        @Override
        public void run() {
            MainActivity.this.timeValue.setText(MainActivity.this.astroDateTime.toString());
            if(tickerHandler != null) {
                tickerHandler.postDelayed(this, SECOND_DELAY);
            }

            sunFragment.update(astroCalculator);
            moonFragment.update(astroCalculator);
        }
    };
    private Handler tickerHandler;

    private AstroDateTime astroDateTime;
    private AstroCalculator astroCalculator;

    private SunFragment sunFragment;
    private MoonFragment moonFragment;

    private FragmentsAdapter pagerAdapter;

    private TextView timeValue;
    private TextView userLocation;

    private FrameLayout sunContainer;
    private FrameLayout moonContainer;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeValue = (TextView) findViewById(R.id.timeValue);
        userLocation = (TextView) findViewById(R.id.locationValue);

        sunContainer = (FrameLayout) findViewById(R.id.sunFragmentContainer);
        moonContainer = (FrameLayout) findViewById(R.id.moonFragmentContainer);
        viewPager = (ViewPager) findViewById(R.id.fragmentsPager);

        astroDateTime = new AstroDateTime();
        astroCalculator = new AstroCalculator(astroDateTime, new AstroCalculator.Location(51, 19));

        tickerHandler = new Handler();
        tickerHandler.postDelayed(timeTicker, SECOND_DELAY);

        this.configureFragments();
    }

    private void configureFragments() {
        sunFragment = new SunFragment();
        moonFragment = new MoonFragment();

        if(sunContainer != null && moonContainer != null) {
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
            if(position == 0) {
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
