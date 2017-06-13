package pl.lodz.p.astroweather.models;

public class Query<T> {

    private T results;

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }
}
