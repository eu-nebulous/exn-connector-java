package eu.nebulouscloud.exn;


import eu.nebulouscloud.exn.core.Consumer;
import eu.nebulouscloud.exn.core.Context;
import eu.nebulouscloud.exn.core.Handler;
import eu.nebulouscloud.exn.core.Publisher;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


class MyCorrelationHandler extends ConnectorHandler {

    Logger logger = LoggerFactory.getLogger(MyConnectorHandler.class);

    @Override
    public void onReady(Context context) {

        ((MyCorrelationPublisher)context.getPublisher("matchme")).sendById("1");
    }
}

class MyCorrelationPublisher extends Publisher {
    public MyCorrelationPublisher() {
        super("matchme", "matchme", true);
    }

    public void sendById(String id){

        try{
            super.send(Map.of(
                    "dark_mode",true
            ),"a",
                    Map.of("correlation-id", id)
            );
        }catch (Exception e){}

    }
}

class TestCorrelationId{

    public static void main(String[] args) {
        try {
            Connector c = new Connector(
                    "ui",
                    new MyCorrelationHandler(),
                    List.of(
                            new MyCorrelationPublisher()
                    ),
                    List.of(
                            new Consumer("match_1","matchme", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                    try{
                                        if(matchCorrelationId(rawMessage,"1")){
                                            System.out.println("I am matching correlation ID 1");
                                            ((MyCorrelationPublisher)context.getPublisher("matchme")).sendById("2");
                                        }else{
                                            System.out.println("I did not match correlation ID "+rawMessage.correlationId());

                                        }
                                    }catch (Exception e){}
                                }
                            },"a", true),

                            new Consumer("match_2","matchme", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                    try{

                                        if(matchCorrelationId(rawMessage,"2")){
                                            System.out.println("I am matching correlation ID 2");
                                            ((MyCorrelationPublisher)context.getPublisher("matchme")).sendById("3");
                                        }else{
                                            System.out.println("I did not match correlation ID "+rawMessage.correlationId());

                                        }
                                    }catch (Exception e){}
                                }
                            },"a", true),

                            new Consumer("match_3","matchme", new Handler(){
                                @Override
                                public void onMessage(String key, String address, Map body, Message rawMessage, Context context) {
                                    try{

                                        if(matchCorrelationId(rawMessage,"3")){
                                            System.out.println("I am matching correlation ID 3");
                                        }else{
                                            System.out.println("I did not match correlation ID "+rawMessage.correlationId());

                                        }
                                    }catch (Exception e){}
                                }
                            },"a", true)
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


