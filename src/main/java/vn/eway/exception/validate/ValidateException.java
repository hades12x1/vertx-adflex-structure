package vn.eway.exception.validate;


import java.util.List;

public class ValidateException extends RuntimeException {
	private List<FieldViolation> fields;

	public ValidateException(List<FieldViolation> fields) {
		this(null, fields);
	}

	public ValidateException(String msg, List<FieldViolation> fields) {
		super(msg);
		this.fields = fields;
	}

	public List<FieldViolation> getFields() {
		return fields;
	}
}