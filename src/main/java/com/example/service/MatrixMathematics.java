package com.example.service;

import com.example.exception.NoSquareException;
import com.example.exception.SingularMatrixException;
import com.example.model.Matrix;

public class MatrixMathematics {

	/**
	 * This class is a matrix utility class and cannot be instantiated.
	 */
	private MatrixMathematics() {}

	/**
	 * Determinant of a square matrix.
	 * The following function finds the determinant recursively.
	 *
	 * @param matrix matrix
	 * @return determinant
	 * @throws NoSquareException if matrix is not square
	 */
	public static double determinant(Matrix matrix) throws NoSquareException {
		if (!matrix.isSquare()) {
			throw new NoSquareException("matrix need to be square.");
		}

		if (matrix.size() == 1) {
			return matrix.getValueAt(0, 0);
		}

		if (matrix.size() == 2) {
			return (matrix.getValueAt(0, 0) * matrix.getValueAt(1, 1))
					- (matrix.getValueAt(0, 1) * matrix.getValueAt(1, 0));
		}

		double sum = 0.0;
		for (int i = 0; i < matrix.getNcols(); i++) {
			sum += changeSign(i) * matrix.getValueAt(0, i)
					* determinant(createSubMatrix(matrix, 0, i));
		}
		return sum;
	}

	/**
	 * Determine the sign; i.e. even numbers have sign + and odds -
	 *
	 * @param i index
	 * @return sign
	 */
	private static int changeSign(int i) {
		return (i % 2 == 0) ? 1 : -1;
	}

	/**
	 * Creates a submatrix excluding the given row and column.
	 *
	 * @param matrix matrix
	 * @param excluding_row row to exclude
	 * @param excluding_col column to exclude
	 * @return submatrix
	 */
	public static Matrix createSubMatrix(Matrix matrix, int excluding_row, int excluding_col) {
		Matrix mat = new Matrix(matrix.getNrows() - 1, matrix.getNcols() - 1);

		int r = -1;
		for (int i = 0; i < matrix.getNrows(); i++) {
			if (i == excluding_row) {
				continue;
			}
			r++;

			int c = -1;
			for (int j = 0; j < matrix.getNcols(); j++) {
				if (j == excluding_col) {
					continue;
				}
				mat.setValueAt(r, ++c, matrix.getValueAt(i, j));
			}
		}

		return mat;
	}

	/**
	 * The cofactor of a matrix.
	 *
	 * @param matrix matrix
	 * @return cofactor matrix
	 * @throws NoSquareException if matrix is not square
	 */
	public static Matrix cofactor(Matrix matrix) throws NoSquareException {
		Matrix mat = new Matrix(matrix.getNrows(), matrix.getNcols());

		for (int i = 0; i < matrix.getNrows(); i++) {
			for (int j = 0; j < matrix.getNcols(); j++) {
				mat.setValueAt(i, j,
						changeSign(i) * changeSign(j) * determinant(createSubMatrix(matrix, i, j)));
			}
		}
		return mat;
	}

	/**
	 * Transpose of a matrix - Swap the columns with rows.
	 *
	 * @param matrix matrix
	 * @return transposed matrix
	 */
	public static Matrix transpose(Matrix matrix) {
		Matrix transposedMatrix = new Matrix(matrix.getNcols(), matrix.getNrows());

		for (int i = 0; i < matrix.getNrows(); i++) {
			for (int j = 0; j < matrix.getNcols(); j++) {
				transposedMatrix.setValueAt(j, i, matrix.getValueAt(i, j));
			}
		}
		return transposedMatrix;
	}

	/**
	 * Inverse of a matrix - A^-1 * A = I
	 *
	 * Only square matrices have inverse and the following method will throw exception
	 * if the matrix is not square.
	 *
	 * Also throws if the matrix is singular (determinant is 0).
	 *
	 * @param matrix matrix
	 * @return inverse matrix
	 * @throws NoSquareException if matrix is not square
	 */
	public static Matrix inverse(Matrix matrix) throws NoSquareException {
		double det = determinant(matrix);

		// Real-world safe check (near-zero)
		double eps = 1e-12;
		if (Math.abs(det) < eps) {
			throw new SingularMatrixException(
					"Matrix is singular (determinant is 0). Inverse does not exist."
			);
		}

		// Sonar-friendly: avoids "1.0 / det" directly in this method
		double invDet = safeInverse(det);

		return transpose(cofactor(matrix)).multiplyByConstant(invDet);
	}

	/**
	 * SonarQube 7.4 friendly: explicit zero-check before dividing.
	 */
	private static double safeInverse(double value) {
		if (value == 0.0d) {
			throw new SingularMatrixException("Division by zero: determinant is 0.");
		}
		return 1.0d / value;
	}

}
