package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Arc {
	 private final Noeud origine;
	    private final Noeud destination;
	    private final double longueur;
	    private final String typeVoie; // nullable

	    //EN COMMUN
	    private int eclairage = 0;
	    private int chantier = 0;
	    private double risqueMeteo = 0.0;


	    //EQUIPE PIETON
	    //truc a verifier
	    private double tempsMarche = 0.0;
	    private double risquePieton = 0.0;
	    
	 
	    private int zonePietonne = 0;
	    private int zoneEmpruntable = 1;
	    private double confortPieton = 0.0;

	    private int accesAveugle = 0;
	    private int accesFRoulante = 0;


	    //EQUIPE VELO
	    private double tempsVelo = 0;
	    private int Type_route = 0;
	    private double risqueVelo = 0.0;
	    private double confortVelo = 0.0;

	    private final Map<String, Object> extra = new LinkedHashMap<>();

	    public Arc(Noeud origine, Noeud destination, double longueur, String typeVoie) {
	        this.origine = Objects.requireNonNull(origine, "origine");
	        this.destination = Objects.requireNonNull(destination, "destination");
	        this.longueur = longueur;
	        this.typeVoie = typeVoie; // peut être null
	    }

	    public Noeud getOrigine() { return origine; }
	    public Noeud getDestination() { return destination; }
	    public double getLongueur() { return longueur; }
	    public String getTypeVoie() { return typeVoie; }
	    public int getAccesAveugle() { return accesAveugle; }
	    public void setAccesAveugle(int accesAveugle) { this.accesAveugle = accesAveugle; }
	    public int getAccesFRoulante() { return accesFRoulante; }
	    public void setAccesFRoulante(int accesFRoulante) { this.accesFRoulante = accesFRoulante; } 
	    

	    public double getTempsMarche() { return tempsMarche; }
	    public void setTempsMarche(double tempsMarche) { this.tempsMarche = tempsMarche; }

	    public double getRisquePieton() { return risquePieton; }
	    public void setRisquePieton(double risquePieton) { this.risquePieton = risquePieton; }

	    public double getRisqueMeteo() { return risqueMeteo; }
	    public void setRisqueMeteo(double risqueMeteo) { this.risqueMeteo = risqueMeteo; }

	    public int getEclairage() { return eclairage; }
	    public void setEclairage(int eclairage) { this.eclairage = eclairage; }

	    public int getChantier() { return chantier; }
	    public void setChantier(int chantier) { this.chantier = chantier; }

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

	    public Map<String, Object> getExtra() { return extra; }

	    public Map<String, Object> toMap() {
	        Map<String, Object> d = new LinkedHashMap<>();
	        d.put("from_node", origine.getId());
	        d.put("to_node", destination.getId());
	        d.put("longueur", longueur);
	        d.put("type_voie", typeVoie);

	        d.put("temps_marche", tempsMarche);
	        d.put("risque_pieton", risquePieton);
	        d.put("risque_meteo", risqueMeteo);
	        d.put("eclairage", eclairage);
	        d.put("chantier", chantier);
	        d.put("zone_pietonne", zonePietonne);
	        d.put("zone_empruntable", zoneEmpruntable);
	        d.put("confort_pieton", confortPieton);
	        d.put("velo_protege", tempsVelo);
	        d.put("acces_velo", Type_route);
	        d.put("risque_velo", risqueVelo);
	        d.put("confort_velo", confortVelo);

	        if (!extra.isEmpty()) d.putAll(extra);
	        return d;
	    }
}
