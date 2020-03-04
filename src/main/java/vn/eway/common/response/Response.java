package vn.eway.common.response;

import vn.eway.common.paging.Page;
import vn.eway.exception.business.BusinessErrorCode;
import vn.eway.exception.business.BusinessException;
import vn.eway.exception.validate.FieldViolation;

import java.util.List;

public class Response<T> {
	private T data;
	private Metadata meta = new Metadata();

	Response(T data, Metadata meta) {
		this.data = data;
		this.meta = meta;
	}

	public Response() {
	}

	public static <T> Response<T> ofSucceeded() {
		return ofSucceeded((T) null);
	}

	public static <T> Response<T> ofSucceeded(T data) {
		if (data instanceof Page) {
			return ofSucceeded((Page) data);
		}
		Response<T> response = new Response<>();
		response.data = data;
		response.meta.code = Response.Metadata.OK_CODE;
		return response;
	}

	public static <T> Response<List<T>> ofSucceeded(Page<T> data) {
		Response<List<T>> response = new Response<>();
		response.data = data.getContents();
		response.meta.code = Response.Metadata.OK_CODE;
		response.meta.page = data.getCurrentPage();
		response.meta.size = data.getPageSize();
		response.meta.total = data.getTotalElements();
		return response;
	}

	public static Response<Void> ofFailed(BusinessErrorCode errorCode) {
		return ofFailed(errorCode, null);
	}

	public static Response<Void> ofFailed(BusinessErrorCode errorCode, String message) {
		return ofFailed(errorCode, message, null);
	}

	public static Response<Void> ofFailed(BusinessErrorCode errorCode, String message, List<FieldViolation> errors) {
		Response<Void> response = new Response<>();
		response.meta.code = String.valueOf(errorCode.getCode());
		response.meta.message = message != null ? message : errorCode.getMessage();
		response.meta.errors = errors;
		return response;
	}

	public static Response<Void> ofFailed(BusinessException exception) {
		return ofFailed(exception.getErrorCode(), exception.getMessage());
	}

	public T getData() {
		return data;
	}

	public Metadata getMeta() {
		return meta;
	}

	public static class Metadata {
		public static final String OK_CODE = "200";
		String code;
		Integer size;
		Integer page;
		Long total;
		List<FieldViolation> errors;
		String message;

		public Metadata() {
		}

		Metadata(String code, Integer size, Integer page, Long total, List<FieldViolation> errors, String message) {
			this.code = code;
			this.size = size;
			this.page = page;
			this.total = total;
			this.errors = errors;
			this.message = message;
		}

		public String getCode() {
			return code;
		}

		public Integer getSize() {
			return size;
		}

		public Integer getPage() {
			return page;
		}

		public Long getTotal() {
			return total;
		}

		public List<FieldViolation> getErrors() {
			return errors;
		}

		public String getMessage() {
			return message;
		}
	}
}