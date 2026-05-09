// Exceptions/NoDataFoundException.java
package com.example.demo.Exceptions;

public class NoDataFoundException extends RuntimeException {
    public NoDataFoundException(String message) {
        super(message);
    }
}