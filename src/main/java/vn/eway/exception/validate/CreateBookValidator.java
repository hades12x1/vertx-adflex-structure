package vn.eway.exception.validate;


import vn.eway.exception.Validator;
import vn.eway.model.Book;

import java.util.List;


public class CreateBookValidator implements Validator<Book> {

	@Override
	public void validate(Book book, List<FieldViolation> fieldViolations) {
		Validator.requireNonEmpty("name", book.getName(), fieldViolations);
	}
}