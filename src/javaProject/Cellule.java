package javaProject;

public class Cellule<T> {
	private T value;

    // Constructeur pour créer une cellule vide
    public Cellule() {
        this.value = null;
    }

    // Constructeur pour créer une cellule avec une valeur
    public Cellule(T value) {
        this.value = value;
    }

    // Méthode pour obtenir la valeur de la cellule
    public T getValue() {
        return value;
    }

    // Méthode pour définir la valeur de la cellule
    public void setValue(T value) {
        this.value = value;
    }
}
