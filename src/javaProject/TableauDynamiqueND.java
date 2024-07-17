package javaProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.swing.JTextArea;

public class TableauDynamiqueND<T> {
    private Object data;
    private int[] dimensions;
    private BiConsumer<int[], Cellule<T>> action;

    public TableauDynamiqueND(int... dimensions) {
        this.dimensions = dimensions;
        this.data = initialize(dimensions, 0);
        initializeGridToZero();
    }

    private void initializeGridToZero() {
        int[] indices = new int[this.dimensions.length];
        setZeroRecursive(indices, 0);
    }

    private void setZeroRecursive(int[] indices, int depth) {
        if (depth == dimensions.length) {
            this.set((T) Integer.valueOf(0), indices.clone());
        } else {
            for (int i = 0; i < dimensions[depth]; i++) {
                indices[depth] = i;
                setZeroRecursive(indices, depth + 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object initialize(int[] dims, int index) {
        int size = dims[index];
        if (index == dims.length - 1) {
            Cellule<T>[] array = (Cellule<T>[]) new Cellule[size];
            for (int i = 0; i < size; i++) {
                array[i] = new Cellule<>();
            }
            return array;
        } else {
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = initialize(dims, index + 1);
            }
            return array;
        }
    }

    public void set(T value, int... indices) {
        Object array = this.data;
        for (int i = 0; i < indices.length - 1; i++) {
            array = ((Object[]) array)[indices[i]];
        }
        ((Cellule<T>[]) array)[indices[indices.length - 1]].setValue(value);
    }

    public T get(int... indices) {
        Object array = this.data;
        for (int i = 0; i < indices.length - 1; i++) {
            array = ((Object[]) array)[indices[i]];
        }
        return ((Cellule<T>[]) array)[indices[indices.length - 1]].getValue();
    }

    public T getValueAt(int... indices) {
        return get(indices);
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public List<Coordonnee> getVoisins(Coordonnee origine, Voisinage voisinage) {
        List<Coordonnee> voisins = new ArrayList<>();
        for (Coordonnee offset : voisinage.getOffsets()) {
            Coordonnee voisinCoord = origine.add(offset);
            if (isWithinBounds(voisinCoord)) {
                voisins.add(voisinCoord);
            }
        }
        return voisins;
    }

    public boolean isWithinBounds(Coordonnee coord) {
        for (int i = 0; i < coord.getDimension(); i++) {
            int index = coord.getCoord(i);
            if (index < 0 || index >= dimensions[i]) {
                return false;
            }
        }
        return true;
    }

    public void parcourir() {
        int[] indices = new int[dimensions.length];
        parcourirRec(data, indices, 0);
    }

    private void parcourirRec(Object array, int[] indices, int index) {
        if (index == dimensions.length - 1) {
            Cellule<T>[] cellArray = (Cellule<T>[]) array;
            for (int i = 0; i < dimensions[index]; i++) {
                indices[index] = i;
                if (action != null) {
                    action.accept(indices, cellArray[i]);
                }
            }
        } else {
            Object[] objArray = (Object[]) array;
            for (int i = 0; i < dimensions[index]; i++) {
                indices[index] = i;
                parcourirRec(objArray[i], indices, index + 1);
            }
        }
    }

    public void parcourirEtAfficher(JTextArea textArea) {
        parcourirEtAfficherRec(data, new int[dimensions.length], 0, textArea);
    }

    private void parcourirEtAfficherRec(Object array, int[] indices, int depth, JTextArea textArea) {
        if (depth == dimensions.length) {
            textArea.append(((Cellule<T>) array).getValue() + " ");
        } else {
            Object[] objArray = (Object[]) array;
            for (int i = 0; i < dimensions[depth]; i++) {
                indices[depth] = i;
                parcourirEtAfficherRec(objArray[i], indices, depth + 1, textArea);
                if (depth < dimensions.length - 1) {
                    textArea.append("\n");
                }
            }
        }
    }

    public void setAction(BiConsumer<int[], Cellule<T>> action) {
        this.action = action;
    }

    public boolean compare(TableauDynamiqueND<T> other) {
        if (!Arrays.equals(this.dimensions, other.dimensions)) {
            return false;
        }

        final boolean[] areEqual = new boolean[] { true };
        int[] indices = new int[dimensions.length];
        compareRec(this.data, other.data, indices, 0, areEqual);
        return areEqual[0];
    }

    private void compareRec(Object thisArray, Object otherArray, int[] indices, int depth, boolean[] areEqual) {
        if (depth == dimensions.length - 1) {
            Cellule<T>[] thisCells = (Cellule<T>[]) thisArray;
            Cellule<T>[] otherCells = (Cellule<T>[]) otherArray;
            for (int i = 0; i < dimensions[depth]; i++) {
                indices[depth] = i;
                if (!Objects.equals(thisCells[i].getValue(), otherCells[i].getValue())) {
                    areEqual[0] = false;
                    return;
                }
            }
        } else {
            Object[] thisObjArray = (Object[]) thisArray;
            Object[] otherObjArray = (Object[]) otherArray;
            for (int i = 0; i < dimensions[depth]; i++) {
                indices[depth] = i;
                if (areEqual[0]) {
                    compareRec(thisObjArray[i], otherObjArray[i], indices, depth + 1, areEqual);
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TableauDynamiqueND<T> that = (TableauDynamiqueND<T>) obj;
        return compare(that);
    }
}
