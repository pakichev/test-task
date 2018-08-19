package com.ruber.xmltransmitter;

public class ConstructionException extends RuntimeException {

    public ConstructionException(String message) {
        super(message);
    }

    public ConstructionException(Throwable e) {
        super(e);
    }

}
