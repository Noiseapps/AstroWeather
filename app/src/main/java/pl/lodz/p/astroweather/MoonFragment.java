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

public class MoonFragment extends Fragment {

    private TextView riseTime;
    private TextView setTime;
    private TextView newMoonDate;
    private TextView fullMoonDate;
    private TextView moonPhase;
    private TextView moonAgeDays;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.setRetainInstance(true);
        View content = inflater.inflate(R.layout.fragment_moon, container, false);
        riseTime = (TextView) content.findViewById(R.id.riseTime);
        setTime = (TextView) content.findViewById(R.id.setTime);
        newMoonDate = (TextView) content.findViewById(R.id.newMoonDate);
        fullMoonDate = (TextView) content.findViewById(R.id.fullMoonDate);
        moonPhase = (TextView) content.findViewById(R.id.moonPhase);
        moonAgeDays = (TextView) content.findViewById(R.id.moonAgeDays);

        return content;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void update(AstroCalculator astroCalculator) {
        AstroCalculator.MoonInfo moonInfo = astroCalculator.getMoonInfo();
        riseTime.setText(Utils.formatAstroDateToStringTimeOnly(moonInfo.getMoonrise()));
        setTime.setText(Utils.formatAstroDateToStringTimeOnly(moonInfo.getMoonrise()));
        newMoonDate.setText(Utils.formatAstroDateToStringDateOnly(moonInfo.getNextNewMoon()));
        fullMoonDate.setText(Utils.formatAstroDateToStringDateOnly(moonInfo.getNextFullMoon()));

        int illuminationPercentValue = (int) (moonInfo.getIllumination() * 100);
        moonPhase.setText(String.format(Locale.getDefault(), "%d %%", illuminationPercentValue));
        moonAgeDays.setText(String.format(Locale.getDefault(), "%d dni", (int) moonInfo.getAge()));
    }
}
