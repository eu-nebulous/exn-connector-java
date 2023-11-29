package eu.nebulouscloud.exn;


import eu.nebulouscloud.exn.core.Consumer;
import eu.nebulouscloud.exn.core.Context;
import eu.nebulouscloud.exn.core.Handler;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

class MyCustomConsumerHandler extends Handler{
    Logger logger = LoggerFactory.getLogger(MyCustomConsumerHandler.class);
    @Override
    public void onMessage(String key, String address, Map body, Message message, Context context) {
        logger.info("Received by custom handler {} => {} = {}", key,address,String.valueOf(body));
    }
}

class MyConnectorHandler extends ConnectorHandler {

    Logger logger = LoggerFactory.getLogger(MyConnectorHandler.class);

    @Override
    public void onReady(Context context) {
        logger.info ("Ready start working");
        context.registerConsumer(new Consumer("ui_health","health", new MyCustomConsumerHandler(), true));


        /**
         * We can then de-register the consumer
         */
        new Thread(){
            @Override
            public void run() {

                try {
                    logger.debug("Waiting for 50 s to unregister consumer");
                    Thread.sleep(30000);
                    context.unregisterConsumer("ui_health");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
}


class TestReceiver{


    public static void main(String[] args) {
        try {
            Connector c = new Connector(
                    "ui",
                    new MyConnectorHandler(),
                    List.of(),
                    List.of(
                            new Consumer("ui_all","eu.nebulouscloud.ui.preferences.>", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                    if(Objects.equals(key, "ui_all")){
                                        System.out.println("These are my preferences => "+ String.valueOf(body));
                                    }
                                }
                            },true,true),
                            new Consumer("config_one","config", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                        System.out.println("These are my ONE config => "+ String.valueOf(body));
                                }
                            },"one", true),
                            new Consumer("config_two","config", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                    System.out.println("These are my TWO config => "+ String.valueOf(body));
                                }
                            },"two", true)

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


