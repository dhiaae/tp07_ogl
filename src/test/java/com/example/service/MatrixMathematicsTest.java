package com.example.service;

import com.example.exception.NoSquareException;
import com.example.exception.SingularMatrixException;
import com.example.model.Matrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixMathematicsTest {

    @Test
    void determinant_1x1() throws NoSquareException {
        Matrix m = new Matrix(1, 1);
        m.setValueAt(0, 0, 5);
        assertEquals(5.0, MatrixMathematics.determinant(m), 1e-9);
    }

    @Test
    void determinant_2x2() throws NoSquareException {
        Matrix m = new Matrix(2, 2);
        m.setValueAt(0, 0, 1);
        m.setValueAt(0, 1, 2);
        m.setValueAt(1, 0, 3);
        m.setValueAt(1, 1, 4);
        assertEquals(-2.0, MatrixMathematics.determinant(m), 1e-9);
    }

    @Test
    void determinant_nonSquare_throws() {
        Matrix m = new Matrix(2, 3);
        assertThrows(NoSquareException.class, () -> MatrixMathematics.determinant(m));
    }

    @Test
    void inverse_singular_throws() throws NoSquareException {
        Matrix m = new Matrix(2, 2);
        m.setValueAt(0, 0, 1);
        m.setValueAt(0, 1, 2);
        m.setValueAt(1, 0, 2);
        m.setValueAt(1, 1, 4); // determinant = 0
        assertThrows(SingularMatrixException.class, () -> MatrixMathematics.inverse(m));
    }

    @Test
    void inverse_2x2_knownValues() throws NoSquareException {
        Matrix m = new Matrix(2, 2);
        m.setValueAt(0, 0, 4);
        m.setValueAt(0, 1, 7);
        m.setValueAt(1, 0, 2);
        m.setValueAt(1, 1, 6); // determinant = 10

        Matrix inv = MatrixMathematics.inverse(m);

        assertEquals(0.6, inv.getValueAt(0, 0), 1e-9);
        assertEquals(-0.7, inv.getValueAt(0, 1), 1e-9);
        assertEquals(-0.2, inv.getValueAt(1, 0), 1e-9);
        assertEquals(0.4, inv.getValueAt(1, 1), 1e-9);
    }
}
