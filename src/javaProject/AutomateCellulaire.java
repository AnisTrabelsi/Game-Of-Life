package javaProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutomateCellulaire extends JFrame {
    private JComboBox<String> fileComboBox;
    private JButton runButton;
    private JPanel dimensionControlsPanel;
    private MatrixPanel matrixPanel;
    private JPanel tableauPanel; // Panel for stacking 1D arrays
    private TableauDynamiqueND<Integer> tableau;
    private XMLConfigReader configReader;
    private String rule;
    private JSpinner[] dimensionSpinners;
    private Timer timer;
    private Map<String, Voisinage> customNeighborhoods;
    private int stepCount = 0;
    private int maxSteps;
    private boolean isTriangleSierpinski = false; // Variable to check if the selected file is "triangle_sierpinski.xml"

    public AutomateCellulaire() {
        setTitle("Automate Cellulaire - Triangle de Sierpinski - jeu de la vie");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel fileLabel = new JLabel("Select XML file:");
        topPanel.add(fileLabel);

        File directory = new File(".");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".xml"));
        String[] fileNames = Arrays.stream(files).map(File::getName).toArray(String[]::new);
        fileComboBox = new JComboBox<>(fileNames);
        topPanel.add(fileComboBox);

        runButton = new JButton("Run");
        runButton.addActionListener(this::runButtonActionPerformed);
        topPanel.add(runButton);

        add(topPanel, BorderLayout.NORTH);

        dimensionControlsPanel = new JPanel();
        add(dimensionControlsPanel, BorderLayout.WEST);

        tableauPanel = new JPanel();
        tableauPanel.setLayout(new BoxLayout(tableauPanel, BoxLayout.Y_AXIS));

        matrixPanel = new MatrixPanel(null, new int[]{0});
        add(new JScrollPane(matrixPanel), BorderLayout.CENTER);

        timer = new Timer(100, this::timerActionPerformed);
    }

    private void runButtonActionPerformed(ActionEvent evt) {
        if (timer.isRunning()) {
            timer.stop();
        }
        
        String selectedFile = (String) fileComboBox.getSelectedItem();

        if (selectedFile != null) {
            isTriangleSierpinski = selectedFile.equals("triangle_sierpinski.xml");
            try {
                configReader = new XMLConfigReader(new File(selectedFile).getAbsolutePath());
                rule = configReader.getRule("rule");

                customNeighborhoods = configReader.readNeighborhoods(); // Load custom neighborhoods

                int dimensionality = configReader.getDimensionality();

                int[] dimensions = configReader.getDimensions();

                tableau = new TableauDynamiqueND<>(dimensions);
                List<Coordonnee> initialActiveCells = configReader.getInitialActiveCells(dimensions);
                for (Coordonnee coord : initialActiveCells) {
                    tableau.set(1, coord.getCoords());
                }

                maxSteps = dimensions[0];  // Assumption: number of steps equals the size of the 1D array

                dimensionControlsPanel.removeAll();
                if (dimensions.length > 2) {
                    dimensionSpinners = new JSpinner[dimensions.length - 2];
                    for (int i = 2; i < dimensions.length; i++) {
                        int max = dimensions[i] - 1;
                        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, max, 1));
                        int index = i + 1;
                        spinner.addChangeListener(e -> updateMatrixPanel());
                        dimensionControlsPanel.add(new JLabel("Index for 2D slice of Dimension " + index + ":"));
                        dimensionControlsPanel.add(spinner);
                        dimensionSpinners[i - 2] = spinner;
                    }
                } else {
                    dimensionSpinners = null; // No spinners needed for 1D or 2D
                }
                dimensionControlsPanel.revalidate();
                dimensionControlsPanel.repaint();

                int[] initialIndices = new int[dimensions.length];
                matrixPanel.setTableau(tableau);
                matrixPanel.setIndices(initialIndices);

                tableauPanel.removeAll();
                tableauPanel.revalidate();
                tableauPanel.repaint();

                if (isTriangleSierpinski) {
                    add(new JScrollPane(tableauPanel), BorderLayout.CENTER);
                    display1DArrays(); // Display initial array for triangle_sierpinski
                } else {
                    add(new JScrollPane(matrixPanel), BorderLayout.CENTER);
                    matrixPanel.repaint(); // Ensure matrixPanel is displayed for other files
                }

                stepCount = 0; // Reset the step count
                timer.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void timerActionPerformed(ActionEvent evt) {
        performNextStep();
    }

    private void performNextStep() {
        if (tableau != null && rule != null) {
            try {
                TableauDynamiqueND<Integer> newValues = new TableauDynamiqueND<>(tableau.getDimensions());
                tableau.setAction((indices, cell) -> {
                    Contexte contexte = new Contexte(tableau, new Coordonnee(indices));
                    int resultat = configReader.interpretRule(rule, contexte, customNeighborhoods);
                    newValues.set(resultat, indices);
                });

                tableau.parcourir();

                if (!tableau.equals(newValues)) {
                    tableau = newValues;
                    updateMatrixPanel();

                    stepCount++;
                    if (stepCount >= maxSteps) {
                        timer.stop();
                    }
                } else {
                    timer.stop();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during next step: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                timer.stop(); // Stop the timer in case of error
            }
        }
    }

    private void updateMatrixPanel() {
        if (isTriangleSierpinski) {
            display1DArrays();
        } else {
            if (tableau != null) {
                int[] indices = new int[tableau.getDimensions().length];
                if (dimensionSpinners != null) {
                    for (int i = 0; i < dimensionSpinners.length; i++) {
                        indices[i + 2] = (int) dimensionSpinners[i].getValue();
                    }
                }
                matrixPanel.setIndices(indices);
                matrixPanel.setTableau(tableau);
                matrixPanel.repaint();
            }
        }
    }

    private void display1DArrays() {
        int[] dimensions = tableau.getDimensions();
        int length = dimensions[0];

        JPanel rowPanel = new JPanel(new GridLayout(1, length));
        for (int j = 0; j < length; j++) {
            JLabel cellLabel = new JLabel();
            cellLabel.setOpaque(true);
            cellLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cellLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Set thinner border
            int[] indices = new int[]{j};
            int value = tableau.getValueAt(indices);
            cellLabel.setBackground(value == 1 ? Color.BLUE : Color.WHITE);
            rowPanel.add(cellLabel);
        }
        tableauPanel.add(rowPanel);

        tableauPanel.revalidate();
        tableauPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutomateCellulaire frame = new AutomateCellulaire();
            frame.setVisible(true);
        });
    }
}
