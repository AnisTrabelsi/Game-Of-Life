package javaProject;

import java.util.ArrayList;
import java.util.List;

public class Voisinage {
	  private List<Coordonnee> offsets;

	    public Voisinage() {
	        this.offsets = new ArrayList<>();
	    }

	    public void addOffset(Coordonnee offset) {
	        offsets.add(offset);
	    }

	    public List<Coordonnee> getOffsets() {
	        return offsets;
	    }

	    public static Voisinage createVoisinageG0() {
	        Voisinage voisinage = new Voisinage();
	      //  voisinage.addOffset(new Coordonnee(0, 0)); // La cellule elle-même
	        return voisinage;
	    }

	    public static Voisinage createVoisinageG2(boolean includeSelf) {
	        Voisinage voisinage = new Voisinage();
	        if (includeSelf) {
	            voisinage.addOffset(new Coordonnee(0)); // La cellule elle-même
	        }
	        voisinage.addOffset(new Coordonnee(-1)); // Voisin de gauche
	        voisinage.addOffset(new Coordonnee(1));  // Voisin de droite
	        return voisinage;
	    }

	    public static Voisinage createVoisinageG4(boolean includeSelf) {
	        Voisinage voisinage = new Voisinage();
	        if (includeSelf) {
	            voisinage.addOffset(new Coordonnee(0, 0)); // La cellule elle-même
	        }
	        voisinage.addOffset(new Coordonnee(-1, 0)); // Voisin de gauche
	        voisinage.addOffset(new Coordonnee(1, 0));  // Voisin de droite
	        voisinage.addOffset(new Coordonnee(0, -1)); // Voisin du dessus
	        voisinage.addOffset(new Coordonnee(0, 1));  // Voisin du dessous
	        return voisinage;
	    }

	    public static Voisinage createVoisinageG8(boolean includeSelf) {
	        Voisinage voisinage = new Voisinage();
	        if (includeSelf) {
	            voisinage.addOffset(new Coordonnee(0, 0)); // La cellule elle-même
	        }
	        voisinage.addOffset(new Coordonnee(-1, 0)); // Voisin de gauche
	        voisinage.addOffset(new Coordonnee(1, 0));  // Voisin de droite
	        voisinage.addOffset(new Coordonnee(0, -1)); // Voisin du dessus
	        voisinage.addOffset(new Coordonnee(0, 1));  // Voisin du dessous
	        voisinage.addOffset(new Coordonnee(-1, -1)); // Voisin haut gauche
	        voisinage.addOffset(new Coordonnee(1, -1));  // Voisin haut droite
	        voisinage.addOffset(new Coordonnee(-1, 1));  // Voisin bas gauche
	        voisinage.addOffset(new Coordonnee(1, 1));  // Voisin bas droite
	        return voisinage;
	    }

	    public static Voisinage createVoisinageG6(boolean includeSelf) {
	        Voisinage voisinage = new Voisinage();
	        if (includeSelf) {
	            voisinage.addOffset(new Coordonnee(0, 0, 0)); // La cellule elle-même
	        }
	        voisinage.addOffset(new Coordonnee(-1, 0, 0)); // Voisin de gauche
	        voisinage.addOffset(new Coordonnee(1, 0, 0));  // Voisin de droite
	        voisinage.addOffset(new Coordonnee(0, -1, 0)); // Voisin du dessus
	        voisinage.addOffset(new Coordonnee(0, 1, 0));  // Voisin du dessous
	        voisinage.addOffset(new Coordonnee(0, 0, -1)); // Voisin avant
	        voisinage.addOffset(new Coordonnee(0, 0, 1));  // Voisin arrière
	        return voisinage;
	    }

	    public static Voisinage createVoisinageG26(boolean includeSelf) {
	        Voisinage voisinage = new Voisinage();
	        if (includeSelf) {
	            voisinage.addOffset(new Coordonnee(0, 0, 0)); // La cellule elle-même
	        }
	        int[] coord = new int[3];
	        generateOffsetsG26(voisinage, coord, 3, 0, includeSelf);
	        return voisinage;
	    }

	    private static void generateOffsetsG26(Voisinage voisinage, int[] coord, int dimensions, int index, boolean includeSelf) {
	        if (index == dimensions) {
	            boolean isSelf = true;
	            for (int i = 0; i < dimensions; i++) {
	                if (coord[i] != 0) {
	                    isSelf = false;
	                    break;
	                }
	            }
	            if (!isSelf || includeSelf) {
	                voisinage.addOffset(new Coordonnee(coord.clone()));
	            }
	            return;
	        }

	        for (int i = -1; i <= 1; i++) {
	            coord[index] = i;
	            generateOffsetsG26(voisinage, coord, dimensions, index + 1, includeSelf);
	        }
	    }
}
