package com.renault.garage.exception;

/**
 * Exception thrown when the garage capacity is exceeded.
 */
public class GarageCapacityExceededException extends Exception {
    public GarageCapacityExceededException(String s) {
        super(s);
    }
}
