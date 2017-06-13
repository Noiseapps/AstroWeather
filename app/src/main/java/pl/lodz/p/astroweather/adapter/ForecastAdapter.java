package pl.lodz.p.astroweather.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import pl.lodz.p.astroweather.R;
import pl.lodz.p.astroweather.models.Forecast;
import pl.lodz.p.astroweather.models.Units;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private final Context context;
    private final List<Forecast> forecastList;
    private Units units;

    public ForecastAdapter(Context context, List<Forecast> forecastList, Units units) {
        this.context = context;
        this.forecastList = forecastList;
        this.units = units;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(item, units);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(forecastList.get(position));
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final ImageView imageRepresentation;
        final TextView description;
        final TextView temperatureRange;
        private final Units units;
        private final Picasso picasso;

        public ViewHolder(View itemView, Units units) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.date);
            imageRepresentation = (ImageView) itemView.findViewById(R.id.imageRepresentation);
            description = (TextView) itemView.findViewById(R.id.description);
            temperatureRange = (TextView) itemView.findViewById(R.id.temperatureRange);
            this.units = units;
            picasso = Picasso.with(date.getContext());
        }

        private void bind(Forecast forecast) {
            picasso.cancelRequest(imageRepresentation);
            final String imageCode = forecast.getCode();
            String imgUrl = String.format(Locale.getDefault(), "http://l.yimg.com/a/i/us/we/52/%s.gif", imageCode);
            date.setText(String.format("%s %s", forecast.getDay(), forecast.getDate()));
            description.setText(forecast.getText());
            temperatureRange.setText(String.format("%s°%s - %s°%s", forecast.getLow(), units.getTemperature(), forecast.getHigh(), units.getTemperature()));
            picasso.load(imgUrl).into(imageRepresentation);
        }
    }
}
