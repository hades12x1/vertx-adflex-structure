package vn.eway.common.utils;

import io.vertx.core.MultiMap;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import vn.eway.exception.ValidateException;
import vn.eway.exception.Validator;
import vn.eway.exception.validate.FieldViolation;

import java.util.Collections;
import java.util.function.Predicate;

public class HandlerHelper {
	public static <T> T getBody(RoutingContext routingContext, Class<T> clazz) throws ValidateException {
		try {
			return Json.decodeValue(routingContext.getBody(), clazz);
		} catch (DecodeException e) {
			throw new ValidateException("Require json object of type: " + clazz.getName(), null);
		}
	}

	public static <T> T getBody(RoutingContext routingContext, Class<T> clazz, Validator<T> validator) throws ValidateException {
		try {
			return validator.validate(Json.decodeValue(routingContext.getBody(), clazz));
		} catch (DecodeException e) {
			throw new ValidateException("Require json object of type: " + clazz.getName() + ": " + e.getMessage(), null);
		}
	}

	public static int getIntParam(RoutingContext routingContext, String paramName) throws ValidateException {
		try {
			return Integer.parseInt(routingContext.request().getParam(paramName));
		} catch (NumberFormatException e) {
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be int")));
		}
	}

	public static int getIntParam(RoutingContext routingContext, String paramName, Predicate<Integer> testFn) throws ValidateException {
		try {
			Integer value = Integer.parseInt(routingContext.request().getParam(paramName));
			if (testFn.test(value)) {
				return value;
			}
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid int")));
		} catch (NumberFormatException e) {
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid int")));
		}
	}

	public static long getLongParam(RoutingContext routingContext, String paramName) throws ValidateException {
		try {
			return Long.parseLong(routingContext.request().getParam(paramName));
		} catch (NumberFormatException e) {
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be long")));
		}
	}

	public static long getLongParam(RoutingContext routingContext, String paramName, Predicate<Long> testFn) throws ValidateException {
		try {
			Long value = Long.parseLong(routingContext.request().getParam(paramName));
			if (testFn.test(value)) {
				return value;
			}
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid long")));
		} catch (NumberFormatException e) {
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid long")));
		}
	}

	public static float getFloatParam(RoutingContext routingContext, String paramName, Predicate<Float> testFn) throws ValidateException {
		try {
			Float value = Float.parseFloat(routingContext.request().getParam(paramName));
			if (testFn.test(value)) {
				return value;
			}
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid float")));
		} catch (NumberFormatException e) {
			throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid float")));
		}
	}

	public static JsonObject getQueryParams(RoutingContext routingContext, String paramName, String... others) {
		JsonObject result = new JsonObject();
		MultiMap params = routingContext.queryParams();
		addQueryParam(params, paramName, result);
		for (String p : others) {
			addQueryParam(params, p, result);
		}
		return result;
	}

	private static void addQueryParam(MultiMap params, String paramName, JsonObject dest) {
		String value = params.get(paramName);
		if (value != null) {
			dest.put(paramName, value);
		}
	}

	public static String getStringParam(RoutingContext routingContext, String paramName) {
		String value = routingContext.request().getParam(paramName);
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		throw new ValidateException(Collections.singletonList(new FieldViolation(paramName, paramName + " must be valid string")));
	}
}