package javaProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLConfigReader {
    private Document document;
    private Random random = new Random();

    public XMLConfigReader(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(new File(filePath));
        document.getDocumentElement().normalize();
    }

    public String getRule(String ruleId) {
        NodeList nodeList = document.getElementsByTagName("rule");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getAttribute("id").equals(ruleId)) {
                    return element.getTextContent();
                }
            }
        }
        return null;
    }

    public int getDimensionality() {
        NodeList nodeList = document.getElementsByTagName("dimensionality");
        if (nodeList.getLength() > 0) {
            return Integer.parseInt(nodeList.item(0).getTextContent());
        }
        return 0; // Default or error handling
    }

    public int[] getDimensions() {
        NodeList nodeList = document.getElementsByTagName("dimensions");
        if (nodeList.getLength() > 0) {
            String[] dimStrings = nodeList.item(0).getTextContent().split(",");
            int[] dimensions = new int[dimStrings.length];
            for (int i = 0; i < dimStrings.length; i++) {
                dimensions[i] = Integer.parseInt(dimStrings[i].trim());
            }
            return dimensions;
        }
        return new int[0]; // Default or error handling
    }

    public Map<String, Voisinage> readNeighborhoods() {
        Map<String, Voisinage> neighborhoods = new HashMap<>();
        NodeList neighborhoodList = document.getElementsByTagName("neighborhood");
        for (int i = 0; i < neighborhoodList.getLength(); i++) {
            Node node = neighborhoodList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element neighborhoodElement = (Element) node;
                String neighborhoodId = neighborhoodElement.getAttribute("id");
                NodeList offsets = neighborhoodElement.getElementsByTagName("offset");
                Voisinage voisinage = new Voisinage();

                for (int j = 0; j < offsets.getLength(); j++) {
                    Node offsetNode = offsets.item(j);
                    if (offsetNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element offsetElement = (Element) offsetNode;
                        List<Integer> coordsList = new ArrayList<>();

                        // Read coordinates dynamically based on attribute names
                        int k = 0;
                        while (offsetElement.hasAttribute("dim" + k)) {
                            String coord = offsetElement.getAttribute("dim" + k);
                            coordsList.add(Integer.parseInt(coord));
                            k++;
                        }
                        // Convert List<Integer> to int[]
                        int[] coords = coordsList.stream().mapToInt(Integer::intValue).toArray();
                        voisinage.addOffset(new Coordonnee(coords));
                    }
                }
                neighborhoods.put(neighborhoodId, voisinage);
            }
        }
        return neighborhoods;
    }

    public List<Coordonnee> getInitialActiveCells(int... dimensions) {
        List<Coordonnee> activeCells = new ArrayList<>();
        NodeList initialStateNodes = document.getElementsByTagName("initialState").item(0).getChildNodes();

        for (int i = 0; i < initialStateNodes.getLength(); i++) {
            Node node = initialStateNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equals("cell")) {
                    String[] coords = element.getTextContent().split(",");
                    int[] intCoords = new int[coords.length];
                    for (int j = 0; j < coords.length; j++) {
                        intCoords[j] = Integer.parseInt(coords[j].trim());
                    }
                    activeCells.add(new Coordonnee(intCoords));
                } else if (element.getTagName().equals("random")) {
                    int probability = Integer.parseInt(element.getTextContent().trim());
                    initializeRandomly(activeCells, probability, new int[dimensions.length], 0, dimensions);
                }
            }
        }
        return activeCells;
    }

    private void initializeRandomly(List<Coordonnee> activeCells, int probability, int[] indices, int depth, int[] dimensions) {
        if (depth == dimensions.length) {
            if (random.nextInt(100) < probability) {
                activeCells.add(new Coordonnee(indices.clone()));
            }
        } else {
            for (int i = 0; i < dimensions[depth]; i++) {
                indices[depth] = i;
                initializeRandomly(activeCells, probability, indices, depth + 1, dimensions);
            }
        }
    }

    private void afficherPile(Stack<Object> stack) {
        System.out.println("État actuel de la pile: " + stack);
    }

    public int interpretRule(String rule, Contexte contexte, Map<String, Voisinage> customNeighborhoods) {
        Stack<Object> stack = new Stack<>();
        String[] tokens = rule.split("[(,)]");
        int dimensionality = contexte.getTableau().getDimensions().length;

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            try {
                if (customNeighborhoods.containsKey(token)) {
                    Voisinage voisinage = customNeighborhoods.get(token);
                    // Vérification de la compatibilité des dimensions
                    if (voisinage.getOffsets().size() > 0 && voisinage.getOffsets().get(0).getCoords().length != dimensionality) {
                        throw new IllegalArgumentException("Le voisinage personnalisé '" + token + "' n'est pas compatible avec les dimensions du tableau.");
                    }
                    stack.push(voisinage);
                    afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                    continue;
                }

                switch (token) {
                    case "SI":
                    case "SUP":
                    case "SUPEQ":
                    case "EQ":
                    case "COMPTER":
                    case "ET":
                    case "OU":
                    case "NON":
                    case "ADD":
                    case "SUB":
                    case "MUL":
                    case "MIN":
                    case "MAX":
                    case "VAL":
                        stack.push(token);
                        afficherPile(stack); // Afficher la pile après avoir poussé l'opérateur
                        break;
                    case "G0":
                        stack.push(Voisinage.createVoisinageG0());
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G2":
                        if (dimensionality != 1) {
                            throw new IllegalArgumentException("G2 s'applique uniquement aux tableaux 1D.");
                        }
                        stack.push(Voisinage.createVoisinageG2(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G4":
                        if (dimensionality != 2) {
                            throw new IllegalArgumentException("G4 s'applique uniquement aux tableaux 2D.");
                        }
                        stack.push(Voisinage.createVoisinageG4(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G8":
                        if (dimensionality != 2) {
                            throw new IllegalArgumentException("G8 s'applique uniquement aux tableaux 2D.");
                        }
                        stack.push(Voisinage.createVoisinageG8(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G6":
                        if (dimensionality != 3) {
                            throw new IllegalArgumentException("G6 s'applique uniquement aux tableaux 3D.");
                        }
                        stack.push(Voisinage.createVoisinageG6(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G26":
                        if (dimensionality != 3) {
                            throw new IllegalArgumentException("G26 s'applique uniquement aux tableaux 3D.");
                        }
                        stack.push(Voisinage.createVoisinageG26(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G2*":
                        if (dimensionality != 1) {
                            throw new IllegalArgumentException("G2 s'applique uniquement aux tableaux 1D.");
                        }
                        stack.push(Voisinage.createVoisinageG2(false));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G4*":
                        stack.push(Voisinage.createVoisinageG4(true));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G8*":
                        stack.push(Voisinage.createVoisinageG8(true));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G6*":
                        stack.push(Voisinage.createVoisinageG6(true));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                    case "G26*":
                        stack.push(Voisinage.createVoisinageG26(true));
                        afficherPile(stack); // Afficher la pile après avoir poussé le voisinage
                        break;
                   
                    default:
                        if (Character.isDigit(token.charAt(0))) {
                            stack.push(Integer.parseInt(token));
                            afficherPile(stack); // Afficher la pile après avoir poussé le nombre
                        }
                        break;
                }
            } catch (Exception e) {
                System.out.printf("Erreur lors du traitement du token '%s': %s%n", token, e.getMessage());
                e.printStackTrace();
                throw e;  // Réacheminer l'exception après la gestion
            }
        }

        // Maintenant que la pile est remplie, traiter les opérateurs
        while (stack.size() > 1) {
            Stack<Object> tempStack = new Stack<>();
            Object element = stack.pop();
            tempStack.push(element);

            // Déplacer les éléments jusqu'à rencontrer un opérateur
            while (!stack.isEmpty() && !(stack.peek() instanceof String)) {
                tempStack.push(stack.pop());
            }

            if (!stack.isEmpty() && stack.peek() instanceof String) {
                String operator = (String) stack.pop();
                System.out.printf("Opérateur retiré pour traitement: %s%n", operator);

                // Traiter l'opérateur avec les opérandes dans tempStack
                Object result = processOperator(tempStack, operator, contexte);
                stack.push(result);

                // Remettre les éléments restants de tempStack dans stack
                while (!tempStack.isEmpty()) {
                    stack.push(tempStack.pop());
                }
                afficherPile(stack); // Afficher la pile après avoir traité l'opérateur
            }
        }

        if (stack.size() != 1) throw new IllegalStateException("État final incorrect de la pile après traitement.");
        Object result = stack.pop();
        System.out.printf("Résultat final extrait de la pile: %s%n", result);
        return (int) result;
    }

    private Object processOperator(Stack<Object> tempStack, String operator, Contexte contexte) {
        int val1, val2, val3, cond;
        try {
            switch (operator) {
                case "SI":
                    cond = (int) tempStack.pop();  // Valeur si faux
                    val2 = (int) tempStack.pop();  // Valeur si vrai
                    val3 = (int) tempStack.pop();  // Condition
                    System.out.printf("Traitement SI(%d, %d, %d)%n", cond, val2, val3);
                    return Operateurs.si(cond, val2, val3);
                case "SUP":
                    val2 = (int) tempStack.pop();  // Second argument
                    val1 = (int) tempStack.pop();  // Premier argument
                    System.out.printf("Traitement SUP(%d, %d)%n", val1, val2);
                    return Operateurs.sup(val2, val1);
                case "SUPEQ":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement SUPEQ(%d, %d)%n", val1, val2);
                    return Operateurs.supeq(val2, val1);
                case "EQ":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement EQ(%d, %d)%n", val1, val2);
                    return Operateurs.eq(val1, val2);
                case "COMPTER":
                    Voisinage voisinage = (Voisinage) tempStack.pop(); // L'objet Voisinage
                    System.out.printf("Traitement COMPTER(%s)%n", voisinage);
                    return Operateurs.compter(contexte.getTableau(), contexte.getOrigine(), voisinage);
                case "ET":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement ET(%d, %d)%n", val1, val2);
                    return Operateurs.et(val1, val2);
                case "OU":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement OU(%d, %d)%n", val1, val2);
                    return Operateurs.ou(val1, val2);
                case "NON":
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement NON(%d)%n", val1);
                    return Operateurs.non(val1);
                case "ADD":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement ADD(%d, %d)%n", val1, val2);
                    return Operateurs.add(val1, val2);
                case "SUB":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement SUB(%d, %d)%n", val1, val2);
                    return Operateurs.sub(val1, val2);
                case "MUL":
                    val2 = (int) tempStack.pop();
                    val1 = (int) tempStack.pop();
                    System.out.printf("Traitement MUL(%d, %d)%n", val1, val2);
                    return Operateurs.mul(val1, val2);
                default:
                    throw new IllegalArgumentException("Opérateur inconnu: " + operator);
            }
        } catch (Exception e) {
            System.out.printf("Erreur lors de l'opération '%s': %s%n", operator, e.getMessage());
            throw e;
        }
    }
}
