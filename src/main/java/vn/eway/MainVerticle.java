package vn.eway;


import io.netty.util.concurrent.SucceededFuture;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import vn.eway.verticle.business.BussinessVerticle;
import vn.eway.verticle.database.es.EsClickVeticle;
import vn.eway.verticle.database.es.EsConversionVerticle;
import vn.eway.verticle.database.es.EsPaymentVerticle;
import vn.eway.verticle.database.mongo.MetaMongoVerticle;
import vn.eway.verticle.database.mongo.SdkMongoVerticle;
import vn.eway.verticle.http.HttpServerVerticle;
import vn.eway.verticle.worker.ReportDailyWorker;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> promise) {
        vertx.executeBlocking(result -> {
            try {
                // Config Verticle Controller
                deployController();

                // Config Verticle Business handler
                deployBusinessVerticle();

                // Config Verticle Database
                deployMongoVerticle();
                deployElasticSearchVerticle();

                // Config Verticle Workder
                deployWorkerVeticle();

                promise.complete();
            } catch (Exception ex) {
                promise.fail(ex);
            }
        }, res -> {
            if (res.succeeded()) {
                LOGGER.info("Deploy all verticle success!");
            } else {
                LOGGER.error("@MainVerticle - #start fail: " + res.cause());
            }
        });
    }

    private void deployController() {
        DeploymentOptions httpOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.http.handle", 5))
                .setConfig(config());
        vertx.deployVerticle(HttpServerVerticle.class, httpOption,
                promise -> this.getStatusDeploy(promise, "HttpServerVerticle"));
    }

    private void deployBusinessVerticle() {
        DeploymentOptions businessOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.business.handle", 5))
                .setConfig(config());
        vertx.deployVerticle(BussinessVerticle.class, businessOption,
                promise -> this.getStatusDeploy(promise, "BussinessVerticle"));
    }

    private void deployMongoVerticle() {
        DeploymentOptions mongoSdkOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.mongo.sdk", 5))
                .setConfig(config());
        DeploymentOptions mongoMetaOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.mongo.meta", 5))
                .setConfig(config());

        vertx.deployVerticle(SdkMongoVerticle.class, mongoSdkOption,
                promise -> this.getStatusDeploy(promise, "SdkMongoVerticle"));

        vertx.deployVerticle(MetaMongoVerticle.class, mongoMetaOption,
                promise -> this.getStatusDeploy(promise, "MetaMongoVerticle"));
    }

    private void deployElasticSearchVerticle() {
        DeploymentOptions esClickOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.es.click", 5))
                .setConfig(config());
        DeploymentOptions esConversionOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.es.conversion", 5))
                .setConfig(config());
        DeploymentOptions esPaymentOption = new DeploymentOptions()
                .setInstances(config().getInteger("instance.es.payment", 5))
                .setConfig(config());

        vertx.deployVerticle(EsClickVeticle.class, esClickOption,
                promise -> this.getStatusDeploy(promise, "EsClickVerticle"));

        vertx.deployVerticle(EsConversionVerticle.class,
                esConversionOption, promise -> this.getStatusDeploy(promise, "EsConversionVerticle"));

        vertx.deployVerticle(EsPaymentVerticle.class, esPaymentOption,
                promise -> this.getStatusDeploy(promise, "EsPaymentVerticle"));
    }

    private void deployWorkerVeticle() {
        DeploymentOptions workerVerticle = new DeploymentOptions()
                .setWorker(true)
                .setConfig(config());
        vertx.deployVerticle(new ReportDailyWorker(), workerVerticle, promise -> {
            try {
                if (promise.succeeded()) {
                    Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity("ApTrigger", "group")
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInMinutes(config().getInteger("run.worker.minute", 1))
                                            .repeatForever()
                            )
                            .build();

                    JobDetail job = JobBuilder.newJob(ReportDailyWorker.class)
                            .withIdentity("ReportDailyWorkder", "group")
                            .build();

                    Scheduler scheduler = new StdSchedulerFactory().getScheduler();
                    scheduler.start();
                    scheduler.scheduleJob(job, trigger);
                    LOGGER.info("@MainVerticle: deploy worker success.");
                } else {
                    LOGGER.error("@MainVerticle #deployWorkerVerticle: " + promise.cause());
                }
            } catch (Exception ex) {
                LOGGER.error("@MainVerticle #deployWorkerVerticle: " + ex);
                throw new RuntimeException(ex);
            }
        });
    }

    void getStatusDeploy(AsyncResult<String> promise, String verticleName) {
        if (promise.succeeded()) {
            LOGGER.info("@MainVerticle: deploy success " + verticleName + "!");
        } else {
            LOGGER.error("@MainVerticle: deploy fail " + verticleName + "!");
            throw new RuntimeException(promise.cause());
        }
    }
}