package eu.nebulouscloud.exn;


import eu.nebulouscloud.exn.core.*;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MySyncedHandler extends ConnectorHandler {

    Logger logger = LoggerFactory.getLogger(MySyncedHandler.class);

    @Override
    public void onReady(Context context) {


        if (context.hasPublisher("synced")) {

            logger.debug("Sending synced");
            Map ret = ((SyncedPublisher) context.getPublisher("synced")).sendSync(
                    Map.of("message", "hello world"),
                    null, null, false
            );
            logger.debug("Received synced " + ret);

        }
    }
}


class TestSyncedPublisher {

    static Logger logger = LoggerFactory.getLogger(TestSyncedPublisher.class);

    public static void main(String[] args) {
        try {
            Connector c = new Connector(
                    "ui",
                    new MySyncedHandler(),
                    List.of(
                            new SyncedPublisher("synced", "synced", true),
                            new Publisher("synced_reply", "synced.reply", true)
                    ),
                    List.of(
                            new Consumer("synced_consumer", "synced", new Handler() {
                                @Override
                                public void onMessage(String key, String address, Map body, Message message, Context context) {

                                    logger.debug("Replying in 5 seconds to " + context.getPublisher("synced_reply").address());

                                    Executors.newSingleThreadScheduledExecutor().schedule(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Map b = Map.of("correlation-id", message.correlationId());
                                                        context.getPublisher("synced_reply").send(
                                                                Map.of("all", "good"),
                                                                null,
                                                                b
                                                        );
                                                    } catch (Exception e) {
                                                        logger.error("Error replying", e);
                                                    }

                                                }
                                            }, 5, TimeUnit.SECONDS
                                    );


                                }
                            }, true)
                    ),
                    false,
                    false,
                    new StaticExnConfig(
                            "localhost",
                            5672,
                            "admin",
                            "admin"
                    )
            );
            c.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


