package vn.eway.verticle.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class ReportDailyWorker extends AbstractVerticle implements Job {

    @Override
    public void start(Promise<Void> promise) {
        promise.complete();
    }

    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("Test worker.");
    }

}
