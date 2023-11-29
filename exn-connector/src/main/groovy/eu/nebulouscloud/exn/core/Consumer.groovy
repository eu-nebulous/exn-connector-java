package eu.nebulouscloud.exn.core

import eu.nebulouscloud.exn.handlers.ConnectorHandler
import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Receiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference

class Consumer extends Link<Receiver>{

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class)
    private Handler handler

    Consumer(String key, String address, Handler handler, boolean topic=true, boolean FQDN=false) {
        super(key, address, topic, FQDN)
        this.handler = handler
    }

    public onDelivery(Delivery delivery, AtomicReference<Context> context){
        logger.debug("Default on delivery for delivery for {}",this.linkAddress)
        Message message = delivery.message();

        Map body = this.processMessage(message, context)
        this.handler.onMessage(
                this.key,
                this.address,
                body,
                message,
                context
        )
        delivery.accept();
    }

    public Map processMessage(Message message, AtomicReference<Context> context){
        logger.debug("Processing message for{}",this.linkAddress)
        return (Map)message.body()
    }

}
