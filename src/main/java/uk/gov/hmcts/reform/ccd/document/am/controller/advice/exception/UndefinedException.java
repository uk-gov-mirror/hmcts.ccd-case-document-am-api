package uk.gov.hmcts.reform.ccd.document.am.controller.advice.exception;

public class UndefinedException extends RuntimeException {

    private static final long serialVersionUID = 2L;

    public UndefinedException(String message) {
        super(message);
    }
}