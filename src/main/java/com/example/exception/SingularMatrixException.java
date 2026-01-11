package com.example.exception;

/**
 * Thrown when trying to invert a singular matrix (determinant == 0).
 */
public class SingularMatrixException extends RuntimeException {

    public SingularMatrixException() {
        super("Matrix is singular (determinant is 0). Inverse does not exist.");
    }

    public SingularMatrixException(String message) {
        super(message);
    }
}
