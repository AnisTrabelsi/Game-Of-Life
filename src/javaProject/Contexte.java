package javaProject;

public class Contexte {
    private TableauDynamiqueND<Integer> tableau;
    private Coordonnee origine;

    public Contexte(TableauDynamiqueND<Integer> tableau, Coordonnee origine) {
        this.tableau = tableau;
        this.origine = origine;
    }

    public TableauDynamiqueND<Integer> getTableau() {
        return tableau;
    }

    public Coordonnee getOrigine() {
        return origine;
    }

    public void setOrigine(Coordonnee origine) {
        this.origine = origine;
    }


}