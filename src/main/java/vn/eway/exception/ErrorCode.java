package vn.eway.exception;

import vn.eway.exception.business.BusinessErrorCode;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ErrorCode {
	public static final BusinessErrorCode INTERNAL_SERVER_ERROR = new BusinessErrorCode(500, "Internal server error", 500);
	public static final BusinessErrorCode INTERNAL_DB_ERROR = new BusinessErrorCode(502, "Database error", 500);
	public static final BusinessErrorCode FORBIDDEN_ERROR = new BusinessErrorCode(401, "You dont have permission to access this resource", 403);
	public static final BusinessErrorCode INVALID_PARAMETERS = new BusinessErrorCode(400, "Invalid parameters", 400);
	public static final BusinessErrorCode INVALID_QUERY_FIELD_PARAMETER = new BusinessErrorCode(402, "Invalid field parameter", 400);

	private static Map<Integer, BusinessErrorCode> errorCodeMap;

	static {
		errorCodeMap = Arrays.stream(ErrorCode.class.getDeclaredFields())
			.filter(f -> Modifier.isStatic(f.getModifiers()) && f.getType().equals(BusinessErrorCode.class))
			.map(f -> {
				try {
					return (BusinessErrorCode) f.get(null);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			})
			.collect(Collectors.toMap(BusinessErrorCode::getCode, Function.identity()));
	}

	public static BusinessErrorCode valueOf(int errorCode) {
		return errorCodeMap.get(errorCode);
	}
}
