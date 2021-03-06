package pl.lodz.p.astroweather.fragments;

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

import pl.lodz.p.astroweather.R;
import pl.lodz.p.astroweather.Utils;

public class MoonFragment extends Fragment {

    private TextView riseTime;
    private TextView setTime;
    private TextView newMoonDate;
    private TextView fullMoonDate;
    private TextView moonPhase;
    private TextView moonAgeDays;
    private TextView refreshTime;
    private AstroCalculator astroCalculator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moon, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        riseTime = (TextView) view.findViewById(R.id.riseTime);
        setTime = (TextView) view.findViewById(R.id.setTime);
        newMoonDate = (TextView) view.findViewById(R.id.newMoonDate);
        fullMoonDate = (TextView) view.findViewById(R.id.fullMoonDate);
        moonPhase = (TextView) view.findViewById(R.id.moonPhase);
        moonAgeDays = (TextView) view.findViewById(R.id.moonAgeDays);
        refreshTime = (TextView) view.findViewById(R.id.refreshTime);
        super.onViewCreated(view, savedInstanceState);
        if (astroCalculator != null) {
            this.update(astroCalculator);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void update(AstroCalculator astroCalculator) {
        this.astroCalculator = astroCalculator;
        if (riseTime == null) {
            return;
        }
        AstroCalculator.MoonInfo moonInfo = astroCalculator.getMoonInfo();
        refreshTime.setText(Utils.formatAstroDateToString(astroCalculator.getDateTime()));
        riseTime.setText(Utils.formatAstroDateToStringTimeOnly(moonInfo.getMoonrise()));
        setTime.setText(Utils.formatAstroDateToStringTimeOnly(moonInfo.getMoonset()));
        newMoonDate.setText(Utils.formatAstroDateToStringDateOnly(moonInfo.getNextNewMoon()));
        fullMoonDate.setText(Utils.formatAstroDateToStringDateOnly(moonInfo.getNextFullMoon()));

        int illuminationPercentValue = (int) (moonInfo.getIllumination() * 100);
        moonPhase.setText(String.format(Locale.getDefault(), "%d %%", illuminationPercentValue));
        moonAgeDays.setText(String.format(Locale.getDefault(), "%.1f dni", moonInfo.getAge()));
    }
}
