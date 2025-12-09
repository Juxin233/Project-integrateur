package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Noeud {

    private final long id;
    private final double lat;
    private final double lon;
    

    private final Map<String, Object> extra = new LinkedHashMap<>();

    public Noeud(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public long getId() { return id; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }


    public Map<String, Object> getExtra() { return extra; }

    public Map<String, Object> toMap() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", id);
        d.put("lat", lat);
        d.put("lon", lon);

        if (!extra.isEmpty()) d.putAll(extra);
        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Noeud other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
