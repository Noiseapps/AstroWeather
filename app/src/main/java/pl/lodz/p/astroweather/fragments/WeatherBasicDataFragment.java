package pl.lodz.p.astroweather.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import pl.lodz.p.astroweather.R;
import pl.lodz.p.astroweather.models.Query;
import pl.lodz.p.astroweather.models.WeatherResponse;

public class WeatherBasicDataFragment extends Fragment {
    private TextView refreshTime;
    private TextView temperatureValue;
    private TextView pressureValue;
    private TextView descriptionValue;
    private TextView humidityValue;
    private TextView visibilityValue;
    private TextView windForceDirection;
    private Query<WeatherResponse> query;
    private ImageView graphicalRepresentation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        refreshTime = (TextView) view.findViewById(R.id.refreshTime);
        graphicalRepresentation = (ImageView) view.findViewById(R.id.imageRepresentation);
        temperatureValue = (TextView) view.findViewById(R.id.temperatureValue);
        pressureValue = (TextView) view.findViewById(R.id.pressureValue);
        descriptionValue = (TextView) view.findViewById(R.id.descriptionValue);
        humidityValue = (TextView) view.findViewById(R.id.humidityValue);
        visibilityValue = (TextView) view.findViewById(R.id.visibilityValue);
        windForceDirection = (TextView) view.findViewById(R.id.windForceDirection);
        super.onViewCreated(view, savedInstanceState);
        if (query != null) {
            this.update(query);
        }
    }

    public void update(Query<WeatherResponse> query) {
        this.query = query;
        if (refreshTime == null) return;
        WeatherResponse weatherResponse = query.getResults();
        refreshTime.setText(query.getCreated());
        final String temp = weatherResponse.getChannel().getItem().getCondition().getTemp();
        final String tempUnit = weatherResponse.getChannel().getUnits().getTemperature();
        temperatureValue.setText(String.format("%s°%s", temp, tempUnit));

        final String pressure = weatherResponse.getChannel().getAtmosphere().getPressure();
        final String pressureUnit = weatherResponse.getChannel().getUnits().getPressure();
        pressureValue.setText(String.format("%s %s", pressure, pressureUnit));

        descriptionValue.setText(weatherResponse.getChannel().getItem().getCondition().getText());

        final String humidity = weatherResponse.getChannel().getAtmosphere().getHumidity();
        humidityValue.setText(String.format("%s%%", humidity));

        final String visibility = weatherResponse.getChannel().getAtmosphere().getHumidity();
        final String distanceUnit = weatherResponse.getChannel().getUnits().getDistance();
        visibilityValue.setText(String.format("%s %s", visibility, distanceUnit));

        final String speed = weatherResponse.getChannel().getWind().getSpeed();
        final String direction = weatherResponse.getChannel().getWind().getDirection();
        final String speedUnit = weatherResponse.getChannel().getUnits().getSpeed();
        windForceDirection.setText(String.format("%s %s @ %s°", speed, speedUnit, direction));

        final String imageCode = weatherResponse.getChannel().getItem().getCondition().getCode();
        String imgUrl = String.format(Locale.getDefault(), "http://l.yimg.com/a/i/us/we/52/%s.gif", imageCode);
        Picasso.with(getActivity()).load(imgUrl).into(graphicalRepresentation);
    }
}
