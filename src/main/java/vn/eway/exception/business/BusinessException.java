package vn.eway.exception.business;

public class BusinessException extends RuntimeException {
	private BusinessErrorCode errorCode;

	public BusinessException(BusinessErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(BusinessErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public BusinessErrorCode getErrorCode() {
		return errorCode;
	}
}