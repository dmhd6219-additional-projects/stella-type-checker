package org.stella.exceptions;

public class StellaException extends RuntimeException {

    private static boolean debug = true;

    private final String information;

    public StellaException(String message, String information) {
        super(message);

        this.information = information;
    }

    @Override
    public String getMessage() {
        if (debug) {
            return super.getMessage() + " : " + information;
        }
        return super.getMessage();
    }

    public String getInformation() {
        return information;
    }
}
