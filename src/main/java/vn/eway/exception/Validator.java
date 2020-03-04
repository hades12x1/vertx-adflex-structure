package vn.eway.exception;

import vn.eway.exception.validate.FieldViolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface Validator<T> extends Function<T, T> {
	static void requireNull(String fieldName, Object value, List<FieldViolation> fieldViolations) {
		if (value != null) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be null"));
		}
	}

	static void requireNonNull(String fieldName, Object value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non null"));
		}
	}

	static void requireNonEmpty(String fieldName, String value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || "".equals(value)) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non empty"));
		}
	}

	static void requireNonEmpty(String fieldName, Collection<?> value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value.isEmpty()) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non empty collection"));
		}
	}

	static void requireNonNegative(String fieldName, Integer value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value < 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non negative"));
		}
	}

	static void requireNonNegative(String fieldName, Long value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value < 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non negative"));
		}
	}

	static void requireNonNegative(String fieldName, Float value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value < 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non negative"));
		}
	}

	static void requireNonNegative(String fieldName, Double value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value < 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be non negative"));
		}
	}

	static void requirePositive(String fieldName, Integer value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value <= 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be positive"));
		}
	}

	static void requirePositive(String fieldName, Long value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value == null || value <= 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be positive"));
		}
	}

	static void optionPositive(String fieldName, Long value, List<FieldViolation> fieldViolations) throws ValidateException {
		if (value != null && value <= 0) {
			fieldViolations.add(new FieldViolation(fieldName, fieldName + " must be positive"));
		}
	}

	@Override
	default T apply(T t) {
		return validate(t);
	}

	default T validate(T t) throws ValidateException {
		List<FieldViolation> violations = new ArrayList<>();
		validate(t, violations);
		if (!violations.isEmpty()) {
			throw new ValidateException(violations);
		}
		return t;
	}

	void validate(T t, List<FieldViolation> fieldViolations);

}