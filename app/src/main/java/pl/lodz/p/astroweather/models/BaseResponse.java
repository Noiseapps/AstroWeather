package pl.lodz.p.astroweather.models;

public class BaseResponse<T> {
    private Query<T> query;

    public Query<T> getQuery() {
        return query;
    }

    public void setQuery(Query<T> query) {
        this.query = query;
    }
}
