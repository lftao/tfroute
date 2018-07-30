package com.javatao.route.support;

public class RouteException extends RuntimeException {
    private static final long serialVersionUID = 7430449458407101783L;
    private String message;

    public RouteException() {}

    public RouteException(String message) {
        this.message = message;
    }

    public RouteException(Throwable cause) {
        super(cause);
    }

    public RouteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteException setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }
}