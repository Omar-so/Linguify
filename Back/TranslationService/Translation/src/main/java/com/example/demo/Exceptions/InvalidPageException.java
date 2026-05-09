// Exceptions/InvalidPageException.java
package com.example.demo.Exceptions;

public class InvalidPageException extends RuntimeException {
    public InvalidPageException(String message) {
        super(message);
    }
}