package vn.eway.exception.business;

public class BusinessErrorCode {
    private int code;
    private String message;
    private int httpStatus;

    public BusinessErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
