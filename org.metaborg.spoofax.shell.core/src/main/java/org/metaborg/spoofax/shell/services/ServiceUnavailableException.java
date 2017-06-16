package org.metaborg.spoofax.shell.services;

public class ServiceUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String service;

    public ServiceUnavailableException(String service) {
        this.service = service;
    }

    @Override
    public String getMessage() {
        return "Could not provide the following service: " + service;
    }

}
