package vn.eway.verticle.business;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.eway.business.BookBusiness;
import static vn.eway.constant.Constant.*;

public class BussinessVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(BussinessVerticle.class);

    @Override
    public void start() {
        BookBusiness bookBusiness = BookBusiness.create(vertx);
        new ServiceBinder(vertx)
                .setAddress(ChannelAddress.ADD_BUS_SERVICE)
                .register(BookBusiness.class, bookBusiness);

        System.out.println("Bussiness thread: " + Thread.currentThread().getName());
    }
}
