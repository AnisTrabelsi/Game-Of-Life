package javaProject;

import java.util.Arrays;

public class Coordonnee {
    public int[] coords;

    public Coordonnee(int... coords) {
        this.coords = coords;
    }

    public int getCoord(int index) {
        return coords[index];
    }

    public int[] getCoords() {
        return coords;
    }

    public int getDimension() {
        return coords.length;
    }

    public Coordonnee add(Coordonnee other) {
        if (this.coords.length != other.getDimension()) {
            throw new IllegalArgumentException("Les dimensions des coordonnées doivent être égales.");
        }
        int[] newCoords = new int[coords.length];
        for (int i = 0; i < coords.length; i++) {
            newCoords[i] = this.coords[i] + other.getCoord(i);
        }
        return new Coordonnee(newCoords);
    }

    public Coordonnee subtract(Coordonnee other) {
        if (this.coords.length != other.getDimension()) {
            throw new IllegalArgumentException("Les dimensions des coordonnées doivent être égales.");
        }
        int[] newCoords = new int[coords.length];
        for (int i = 0; i < coords.length; i++) {
            newCoords[i] = this.coords[i] - other.getCoord(i);
        }
        return new Coordonnee(newCoords);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordonnee)) {
            return false;
        }
        Coordonnee other = (Coordonnee) obj;
        return Arrays.equals(this.coords, other.coords);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coords);
    }
}
