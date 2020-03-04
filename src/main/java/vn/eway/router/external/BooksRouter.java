package vn.eway.router.external;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.Validate;
import vn.eway.business.BookBusiness;
import vn.eway.common.response.ResponseUtils;
import vn.eway.common.utils.HandlerHelper;
import vn.eway.constant.Constant;
import vn.eway.exception.ExceptionHandler;
import vn.eway.exception.validate.CreateBookValidator;
import vn.eway.model.Book;

import java.util.Random;

public class BooksRouter {
    private final Vertx vertx;
    private BookBusiness bookBusiness;

    public BooksRouter(Vertx vertx) {
        this.vertx = vertx;
    }

    public final Router getRouter() {
        Router router = Router.router(this.vertx);
        bookBusiness = BookBusiness.createProxy(this.vertx, Constant.ChannelAddress.ADD_BUS_SERVICE);

        router.get("/").handler(this.listBook());
        router.get("/:id").handler(this.getBookById());
        router.post("/").handler(this.createBook());
        router.put("/").handler(this.updateBook());
        router.errorHandler(500, ExceptionHandler::handle);
        return router;
    }


    private Handler<RoutingContext> listBook() {
        return routingContext -> {
            JsonObject dataSample = new JsonObject();
            dataSample.put("http", "http-server");
            dataSample.put("_id", "test" + new Random().nextInt(10000));
            bookBusiness.inrichmentField(dataSample, res -> {
                if (res.succeeded()) {
                    ResponseUtils.ok(routingContext, res.result());
                } else {
                    ResponseUtils.internalServerError(routingContext, res.cause());
                }
            });
        };
    }

    private Handler<RoutingContext> createBook() {
        return routingContext -> {
            Book book = HandlerHelper.getBody(routingContext, Book.class, new CreateBookValidator());

            //Todo insert
        };
    }

    private Handler<RoutingContext> updateBook() {
        return routingContext -> {
            Book book = HandlerHelper.getBody(routingContext, Book.class, new CreateBookValidator());

            //Todo update
        };
    }

    private Handler<RoutingContext> getBookById() {
        return routingContext -> {
            String id = routingContext.pathParam("id");
            Validate.notBlank(id, "id must be not null");

            //Todo find by Id
        };
    }
}
