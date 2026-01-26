package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Arc {
	 	private final Noeud origine;
	    private final Noeud destination;
	    private final double longueur;
	    private final long id;
	    private int Type_route = 2; // 0 Pieton, 1 Velo, 2 Pieton & Velo
	    private int Type_Pieton ; // 0 Non accessible, 1 SH, 2 HV, 3 HM, 4 pout tout
	    private int Type_Velo ; //0 Non accessible, 1 accessible
	    
	    public int getType_Pieton() {
			return Type_Pieton;
		}

		public void setType_Pieton(int type_Pieton) {
			Type_Pieton = type_Pieton;
		}

		public int getType_Velo() {
			return Type_Velo;
		}

		public void setType_Velo(int type_velo) {
			Type_Velo = type_velo;
		}

		//EQUIPE PIETON
	    private double tempsMarche = 0.0;
	    private double risquePieton = 0.0;
	    private double diffPieton=0.0;
	 
	    private int zonePietonne = 0;
	    private int zoneEmpruntable = 1;
	    private double confortPieton = 0.0;

	    //EQUIPE VELO
	    private double tempsVelo = 0;
	    private double risqueVelo = 0.0;
	    private double confortVelo = 0.0;
	    private double diffVelo =0.0;
	    

		private final Map<String, Object> extra = new LinkedHashMap<>();

	    public Arc(Noeud origine, Noeud destination, double longueur, long id) {
	        this.origine = Objects.requireNonNull(origine, "origine");
	        this.destination = Objects.requireNonNull(destination, "destination");
	        this.longueur = longueur;
	        this.id = id;    
		}
	    
	    public Arc(Noeud origine, Noeud destination, double longueur, int typeRoute,int access_pieton,int access_velo,long id,
	    		double risquePieton,double risqueVelo,double confortPieton,double confortVelo,double diffPieton,double diffVelo ) {
	        this.origine = Objects.requireNonNull(origine, "origine");
	        this.destination = Objects.requireNonNull(destination, "destination");
	        this.longueur = longueur;
	        this.Type_route = typeRoute;
	        this.Type_Pieton = access_pieton;
	        this.Type_Velo = access_velo;
	        this.risqueVelo = risqueVelo;
	        this.risquePieton = risquePieton;
	        this.confortVelo = confortVelo;
	        this.confortPieton = confortPieton;
	        this.diffVelo = diffVelo;
	        this.diffPieton = diffPieton;
	        this.id = id; 
	    }
	    
	    public Noeud getOrigine() { return origine; }
	    public Noeud getDestination() { return destination; }
	    public double getLongueur() { return longueur; }

	    public double getTempsMarche() { return tempsMarche; }
	    public void setTempsMarche(double tempsMarche) { this.tempsMarche = tempsMarche; }

	    public double getRisquePieton() { return risquePieton; }
	    public void setRisquePieton(double risquePieton) { this.risquePieton = risquePieton; }

	    public int getZonePietonne() { return zonePietonne; }
	    public void setZonePietonne(int zonePietonne) { this.zonePietonne = zonePietonne; }

	    public int getZoneEmpruntable() { return zoneEmpruntable; }
	    public void setZoneEmpruntable(int zoneEmpruntable) { this.zoneEmpruntable = zoneEmpruntable; }

	    public double getTempsVelo() { return tempsVelo; }
	    public void setTempsVelo(double tempsVelo) { this.tempsVelo =
	    tempsVelo; }    


	    public double getConfortPieton() { return confortPieton; }
	    public void setConfortPieton(double confortPieton) { this.confortPieton = confortPieton; }

	    public int getType_route() { return Type_route; }
	    public void setType_route(int Type_route) { this.Type_route = Type_route; }

	    public double getRisqueVelo() { return risqueVelo; }
	    public void setRisqueVelo(double risqueVelo) { this.risqueVelo = risqueVelo; }

	    public double getConfortVelo() { return confortVelo; }
	    public void setConfortVelo(double confortVelo) { this.confortVelo = confortVelo; }
	    public double getDiffPieton() {return diffPieton;}

		public void setDiffPieton(double diffPieton) {this.diffPieton = diffPieton;}

		public double getDiffVelo() {return diffVelo;}

		public void setDiffVelo(double diffVelo) {this.diffVelo = diffVelo;}
		
	    public Map<String, Object> getExtra() { return extra; }

	    public Map<String, Object> toMap() {
	        Map<String, Object> d = new LinkedHashMap<>();
	        d.put("from_node", origine.getId());
	        d.put("to_node", destination.getId());
	        d.put("longueur", longueur);
	        d.put("type_route", Type_route);
	        d.put("access_pieton",Type_Pieton);
	        d.put("access_velo",Type_Velo);
	        d.put("temps_marche", tempsMarche);
	        d.put("risque_pieton", risquePieton);
	        d.put("confort_pieton", confortPieton);
	        d.put("diff_pieton", diffPieton);       
	        d.put("temps_velo", tempsMarche);
	        d.put("risque_velo", risqueVelo);
	        d.put("confort_velo", confortVelo);
	        d.put("diff_velo", diffVelo);
	        if (!extra.isEmpty()) d.putAll(extra);
	        return d;
	    }

		public long getId() {
			return id;
		}
}
