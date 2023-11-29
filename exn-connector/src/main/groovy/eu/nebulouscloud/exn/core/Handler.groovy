package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference

abstract class Handler {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class)

    public void onMessage(String key, String address, Map body, Message message, AtomicReference<Context> context){
        logger.debug("Default on message for delivery for {} => {} ({}) = {}",
                key,
                address,
                body)


    }

}
