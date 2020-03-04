package vn.eway.verticle.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.eway.common.utils.BasicAuth;
import vn.eway.exception.ExceptionHandler;
import vn.eway.router.external.BooksRouter;

import java.util.HashSet;
import java.util.Set;

public class HttpServerVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LogManager.getLogger(HttpServerVerticle.class);

	@Override
	public void start(Promise<Void> promise) {
		Integer portNumber = config().getInteger("http.port", 8888);
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		router.route().pathRegex("/books/.*").handler(BasicAuth.createAuthShiro(vertx, config()));

		router.route().handler(LoggerHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(new LoggerHandlerImpl(false, LoggerHandler.DEFAULT_FORMAT));
		setupCors(router);


		BooksRouter booksRouter = new BooksRouter(vertx);
		router.mountSubRouter("/books", booksRouter.getRouter());

		router.errorHandler(500, ExceptionHandler::handle);
		server.requestHandler(router)
			.listen(portNumber, result -> {
				if (result.succeeded()) {
					LOGGER.info("HTTP server running on port " + portNumber);
					promise.complete();
				} else {
					LOGGER.error("Could not start a HTTP server", result.cause());
					promise.fail(result.cause());
				}
			});
	}

	private void setupCors(Router router) {
		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.OPTIONS);
		allowedMethods.add(HttpMethod.DELETE);
		allowedMethods.add(HttpMethod.PATCH);
		allowedMethods.add(HttpMethod.PUT);
		router.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods));
	}
}
