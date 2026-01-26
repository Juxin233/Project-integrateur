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

	public double getLon1() {
		return lon1;
	}

	public void setLon1(double lon1) {
		this.lon1 = lon1;
	}

	public double getLat1() {
		return lat1;
	}

	public void setLat1(double lat1) {
		this.lat1 = lat1;
	}

	public double getLon2() {
		return lon2;
	}

	public void setLon2(double lon2) {
		this.lon2 = lon2;
	}

	public double getLat2() {
		return lat2;
	}

	public void setLat2(double lat2) {
		this.lat2 = lat2;
	}

	public double getRadiusMeters() {
		return radiusMeters;
	}

	public void setRadiusMeters(double radiusMeters) {
		this.radiusMeters = radiusMeters;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
