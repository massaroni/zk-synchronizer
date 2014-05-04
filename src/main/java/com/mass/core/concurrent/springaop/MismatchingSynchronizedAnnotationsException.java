package com.mass.core.concurrent.springaop;

public class MismatchingSynchronizedAnnotationsException extends IllegalStateException {
    private static final long serialVersionUID = 908013594818007477L;

    public MismatchingSynchronizedAnnotationsException(final String arg0) {
        super(arg0);
    }

}
