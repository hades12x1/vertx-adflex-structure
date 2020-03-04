package vn.eway.exception;

import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceException;
import vn.eway.common.response.Response;
import vn.eway.common.response.ResponseUtils;
import vn.eway.exception.business.BusinessErrorCode;
import vn.eway.exception.business.BusinessException;
import vn.eway.exception.validate.ValidateException;

public class ExceptionHandler {
    public static void handle(RoutingContext routingContext) {
        handle(routingContext, routingContext.failure());
    }

    public static void handle(RoutingContext routingContext, Throwable cause) {
        if (cause instanceof ValidateException) {
            handleValidateException(routingContext, (ValidateException) cause);
            return;
        }
        if (cause instanceof ServiceException) {
            handleServiceException(routingContext, (ServiceException) cause);
            return;
        }
        if (cause instanceof BusinessException) {
            handleBusinessException(routingContext, (BusinessException) cause);
            return;
        }
        handleException(routingContext, cause);
    }

    private static void handleException(RoutingContext routingContext, Throwable cause) {
        BusinessErrorCode errCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Response response = Response.ofFailed(errCode, cause.getMessage());
        ResponseUtils.json(routingContext, errCode.getHttpStatus(), response);
    }

    private static void handleValidateException(RoutingContext routingContext, ValidateException cause) {
        BusinessErrorCode errCode = ErrorCode.INVALID_PARAMETERS;
        Response response = Response.ofFailed(errCode, cause.getMessage() != null ? cause.getMessage() : errCode.getMessage(), cause.getFields());
        ResponseUtils.json(routingContext, errCode.getHttpStatus(), response);
    }

    private static void handleBusinessException(RoutingContext routingContext, BusinessException cause) {
        Response response = Response.ofFailed(cause);
        ResponseUtils.json(routingContext, cause.getErrorCode().getHttpStatus(), response);
    }

    private static void handleServiceException(RoutingContext routingContext, ServiceException cause) {
        BusinessErrorCode errorCode = ErrorCode.valueOf(cause.failureCode());
        if (errorCode == null) {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        }
        Response response = Response.ofFailed(errorCode, cause.getMessage());
        ResponseUtils.json(routingContext, errorCode.getHttpStatus(), response);
    }

    private static boolean isClientError(int httpStatus) {
        return httpStatus < 500;
    }

    public static boolean isServerError(int httpStatus) {
        return httpStatus >= 500;
    }

    public static BusinessErrorCode getErrorCode(Throwable cause) {
        if (cause instanceof ValidateException) {
            return ErrorCode.INVALID_PARAMETERS;
        }
        if (cause instanceof ServiceException) {
            BusinessErrorCode errorCode = ErrorCode.valueOf(((ServiceException) cause).failureCode());
            if (errorCode == null) {
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            }
            return errorCode;
        }
        if (cause instanceof BusinessException) {
            return ((BusinessException) cause).getErrorCode();
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }
}
