package javaProject;

import javax.swing.*;
import java.awt.*;

public class MatrixPanel extends JPanel {
    private TableauDynamiqueND<Integer> tableau;
    private int[] indices;  // Indices pour sélectionner les coupes des dimensions supérieures

    public MatrixPanel(TableauDynamiqueND<Integer> tableau, int[] initialIndices) {
        this.tableau = tableau;
        this.indices = initialIndices;
        setPreferredSize(new Dimension(400, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tableau == null) {
            return;  // Ne rien dessiner si le tableau est null
        }

        int[] dimensions = tableau.getDimensions();
        if (dimensions.length == 1) {
            draw1D(g, dimensions[0]);
        } else if (dimensions.length >= 2) {
            draw2D(g, dimensions[0], dimensions[1]);
        }
    }

    private void draw1D(Graphics g, int length) {
        int cellWidth = getWidth() / length;
        for (int i = 0; i < length; i++) {
            int[] sliceIndices = indices.clone();
            sliceIndices[0] = i;
            int value = tableau.getValueAt(sliceIndices);
            if (value == 1) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(i * cellWidth, 0, cellWidth, getHeight());
            g.setColor(Color.GRAY);
            g.drawRect(i * cellWidth, 0, cellWidth, getHeight());
        }
    }

    private void draw2D(Graphics g, int rows, int cols) {
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int[] sliceIndices = indices.clone();
                sliceIndices[0] = i;
                sliceIndices[1] = j;
                int value = tableau.getValueAt(sliceIndices);
                if (value == 1) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.GRAY);
                g.drawRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }
    }

    public void setTableau(TableauDynamiqueND<Integer> tableau) {
        this.tableau = tableau;
        repaint();
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
        repaint();
    }
}
