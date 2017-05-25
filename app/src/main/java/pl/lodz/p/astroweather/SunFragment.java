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
        this.setRetainInstance(true);
        View content = inflater.inflate(R.layout.fragment_sun, container, false);
        riseTime = (TextView) content.findViewById(R.id.riseTime);
        riseAzimuth = (TextView) content.findViewById(R.id.riseAzimuth);
        setTime = (TextView) content.findViewById(R.id.setTime);
        setAzimuth = (TextView) content.findViewById(R.id.setAzimuth);
        civilTimeDusk = (TextView) content.findViewById(R.id.duskTime);
        civilTimeDawn = (TextView) content.findViewById(R.id.dawnTime);
        return content;
    }
g
    public void update(AstroCalculator astroCalculator) {
        AstroCalculator.SunInfo sunInfo = astroCalculator.getSunInfo();
        riseTime.setText(Utils.formatAstroDateToString(sunInfo.getSunrise()));
        setTime.setText(Utils.formatAstroDateToString(sunInfo.getSunset()));
        riseAzimuth.setText(String.format(Locale.getDefault(), "%f deg", sunInfo.getAzimuthRise()));
        setAzimuth.setText(String.format(Locale.getDefault(), "%f deg", sunInfo.getAzimuthSet()));
        civilTimeDusk.setText(Utils.formatAstroDateToString(sunInfo.getTwilightEvening()));
        civilTimeDawn.setText(Utils.formatAstroDateToString(sunInfo.getTwilightMorning()));
    }
}
