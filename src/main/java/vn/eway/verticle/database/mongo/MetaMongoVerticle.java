package vn.eway.verticle.database.mongo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static vn.eway.constant.Constant.*;

import vn.eway.service.MongoRepository;

public class MetaMongoVerticle extends AbstractVerticle {
    private static Logger LOGGER = LogManager.getLogger(MetaMongoVerticle.class);
    private MongoClient metaMongo;

    @Override
    public void start(Promise<Void> promise) {
        try {
            String uri = config().getString("mongo.meta.uri");
            String dataSourceName = this.getClass().getName();
            Validate.notEmpty(uri, "Uri mongo.meta.uri must be not empty.");
            JsonObject configMongoMeta = new JsonObject().put("connection_string", uri);

            this.metaMongo = MongoClient.createShared(vertx, configMongoMeta);

            MongoRepository mongoRepository = MongoRepository.create(this.metaMongo);
            new ServiceBinder(vertx)
                    .setAddress(ChannelAddress.BUS_MONGO_META)
                    .register(MongoRepository.class, mongoRepository);

            promise.complete();
        } catch (Exception ex) {
            LOGGER.error("@MetaMongoVerticle error: " + ex.getCause());
            promise.fail(ex);
        }
    }

    @Override
    public void stop() {
        if (metaMongo != null) {
            metaMongo.close();
        }
    }
}
