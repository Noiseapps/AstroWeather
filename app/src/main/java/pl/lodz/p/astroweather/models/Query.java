package pl.lodz.p.astroweather.models;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class Query<T> {

    private T results;
    private String created;

    public String getCreated() {
        try {
            return new DateTime(created).toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return created;
        }
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }
}
