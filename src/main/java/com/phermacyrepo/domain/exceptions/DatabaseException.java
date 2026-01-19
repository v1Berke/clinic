package com.phermacyrepo.domain.exceptions;

public class DatabaseException extends DomainException {
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
