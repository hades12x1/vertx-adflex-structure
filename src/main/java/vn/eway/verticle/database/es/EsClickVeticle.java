package vn.eway.verticle.database.es;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static vn.eway.constant.Constant.*;
import vn.eway.service.ElasticRepository;
import vn.eway.service.io.elasticsearch.index.ClickIndex;

public class EsClickVeticle extends AbstractVerticle {
    private static Logger LOGGER = LogManager.getLogger(EsClickVeticle.class);
    private ClickIndex esClick;

    @Override
    public void start(Promise<Void> promise) {
        try {
            String uri = config().getString("es.clicks.uri");
            Validate.notEmpty(uri, "Uri es.clicks.uri must be not empty.");

            this.esClick = new ClickIndex(uri);

            ElasticRepository elasticSearch = ElasticRepository.create(this.esClick);
            new ServiceBinder(vertx)
                    .setAddress(ChannelAddress.BUS_ES_CLICK)
                    .register(ElasticRepository.class, elasticSearch);

            promise.complete();
        } catch (Exception ex) {
            LOGGER.error("@EsClickVerticle error: " + ex.getCause());
            promise.fail(ex);
        }
    }
}
