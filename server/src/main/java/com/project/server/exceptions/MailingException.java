package com.project.server.exceptions;

public class MailingException extends Exception{
    public MailingException(String message) {
        super(message);
    }

    public MailingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailingException(Throwable cause) {
        super(cause);
    }
}
