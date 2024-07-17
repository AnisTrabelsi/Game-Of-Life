package javaProject;

import java.util.Arrays;

public class Operateurs {
    public static int et(int val1, int val2) {
        return (val1 != 0 && val2 != 0) ? 1 : 0;
    }

    public static int ou(int val1, int val2) {
        return (val1 == 0 && val2 == 0) ? 0 : 1;
    }

    public static int non(int val) {
        return (val == 0) ? 1 : 0;
    }

    public static int sup(int val1, int val2) {
        return (val1 > val2) ? 1 : 0;
    }

    public static int supeq(int val1, int val2) {
        return (val1 >= val2) ? 1 : 0;
    }

    public static int eq(int val1, int val2) {
        return (val1 == val2) ? 1 : 0;
    }

    public static int compter(TableauDynamiqueND<Integer> tableau, Coordonnee origine, Voisinage voisinage) {
        int count = 0;
        for (Coordonnee offset : voisinage.getOffsets()) {
            Coordonnee voisinCoord = origine.add(offset);
            if (tableau.isWithinBounds(voisinCoord)) {
                Integer value = tableau.get(voisinCoord.getCoords()); 
                if (value != null && value == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    public static int add(int val1, int val2) {
        return val1 + val2;
    }

    public static int sub(int val1, int val2) {
        return val1 - val2;
    }

    public static int mul(int val1, int val2) {
        return val1 * val2;
    }

    public static int max(int val1, int val2) {
        return Math.max(val1, val2);
    }

    public static int min(int val1, int val2) {
        return Math.min(val1, val2);
    }


    public static int si(int val1, int val2, int val3) {
        return (val1 != 0) ? val2 : val3;
    }
}