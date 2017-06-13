package pl.lodz.p.astroweather.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pl.lodz.p.astroweather.R;
import pl.lodz.p.astroweather.adapter.ForecastAdapter;
import pl.lodz.p.astroweather.models.Forecast;
import pl.lodz.p.astroweather.models.Query;
import pl.lodz.p.astroweather.models.Units;
import pl.lodz.p.astroweather.models.WeatherResponse;

public class WeatherForecastFragment extends Fragment {
    private Query<WeatherResponse> query;
    private TextView refreshTime;
    private RecyclerView forecastList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        refreshTime = (TextView) view.findViewById(R.id.refreshTime);
        forecastList = (RecyclerView) view.findViewById(R.id.forecastList);
        forecastList.setLayoutManager(new LinearLayoutManager(getActivity()));
        super.onViewCreated(view, savedInstanceState);
        if (query != null) {
            this.update(query);
        }
    }

    public void update(Query<WeatherResponse> query) {
        this.query = query;
        if (refreshTime == null) return;
        refreshTime.setText(query.getCreated());
        final List<Forecast> forecast = query.getResults().getChannel().getItem().getForecast();
        final Units units = query.getResults().getChannel().getUnits();
        forecastList.setAdapter(new ForecastAdapter(getActivity(), forecast, units));
    }
}
