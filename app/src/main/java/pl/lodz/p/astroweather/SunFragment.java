package pl.lodz.p.astroweather;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;

import java.util.Locale;

public class SunFragment extends Fragment {
    private TextView riseTime;
    private TextView riseAzimuth;
    private TextView setTime;
    private TextView setAzimuth;
    private TextView civilTimeDusk;
    private TextView civilTimeDawn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sun, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        riseTime = (TextView) view.findViewById(R.id.riseTime);
        riseAzimuth = (TextView) view.findViewById(R.id.riseAzimuth);
        setTime = (TextView) view.findViewById(R.id.setTime);
        setAzimuth = (TextView) view.findViewById(R.id.setAzimuth);
        civilTimeDusk = (TextView) view.findViewById(R.id.duskTime);
        civilTimeDawn = (TextView) view.findViewById(R.id.dawnTime);
        super.onViewCreated(view, savedInstanceState);
    }

    public void update(AstroCalculator astroCalculator) {
        if(riseTime == null) {
            return;
        }
        AstroCalculator.SunInfo sunInfo = astroCalculator.getSunInfo();
        riseTime.setText(Utils.formatAstroDateToStringTimeOnly(sunInfo.getSunrise()));
        setTime.setText(Utils.formatAstroDateToStringTimeOnly(sunInfo.getSunset()));
        riseAzimuth.setText(String.format(Locale.getDefault(), "%f deg", sunInfo.getAzimuthRise()));
        setAzimuth.setText(String.format(Locale.getDefault(), "%f deg", sunInfo.getAzimuthSet()));
        civilTimeDusk.setText(Utils.formatAstroDateToStringTimeOnly(sunInfo.getTwilightEvening()));
        civilTimeDawn.setText(Utils.formatAstroDateToStringTimeOnly(sunInfo.getTwilightMorning()));
    }
}
