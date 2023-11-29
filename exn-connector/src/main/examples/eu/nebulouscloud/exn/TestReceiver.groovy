import eu.nebulouscloud.exn.Connector
import eu.nebulouscloud.exn.core.Consumer
import eu.nebulouscloud.exn.core.Context
import eu.nebulouscloud.exn.core.Handler
import eu.nebulouscloud.exn.handlers.ConnectorHandler
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.exceptions.ClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference


class MyConnectorHandler extends ConnectorHandler {
    @Override
    def onReady(AtomicReference<Context> context) {
        println ("Ready start working")
    }
}

class MyCustomConsumerHandler extends Handler{
    @Override
    def onMessage(String key, String address, Map body, Message message, AtomicReference<Context> context) {
        println "Received by custom handler ${key} => ${address} = ${body}"
    }
}


public static void main(String[] args) {
    try {

        Connector c = new Connector(
                "ui",
                new MyConnectorHandler(),
                [],
                [
                        new Consumer("ui_health","health", new MyCustomConsumerHandler(), true),
                        new Consumer("ui_all","eu.nebulouscloud.ui.preferences.>", new Handler(){
                            @Override
                            def Object onMessage(String key, String address, Map body, Message rawMessage, AtomicReference<Context> context) {
                                if(key == "ui_all"){
                                    println "These are my preferences => ${body}"
                                }
                            }
                        },true,true),
                ]
        )
        c.start()
    } catch (ClientException e) {
        e.printStackTrace();
    }
}

