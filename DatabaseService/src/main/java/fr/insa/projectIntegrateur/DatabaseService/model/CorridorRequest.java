package fr.insa.projectIntegrateur.DatabaseService.model;

public class CorridorRequest {
	private double lon1;
    private double lat1;
    private double lon2;
    private double lat2;
    private double radiusMeters;
    private int limit;

    public CorridorRequest() {}

    public CorridorRequest(double lon1, double lat1,
                            double lon2, double lat2,
                            double radiusMeters, int limit) {
        this.lon1 = lon1;
        this.lat1 = lat1;
        this.lon2 = lon2;
        this.lat2 = lat2;
        this.radiusMeters = radiusMeters;
        this.limit = limit;
    }
}
