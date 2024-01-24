package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference

/**
 * This is the handler class for the {@link Consumer}. You will need to
 * create extension of this class in order to handle incoming messages.
 */
abstract class Handler {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class)

    /**
     * This is the default handle method, which needs to be overwritten if we
     * need to handle 
      * @param key
     * @param address
     * @param body
     * @param message
     * @param context
     */

    public void onMessage(String key, String address, Map body, Message message, Context context){
        logger.debug("Default on message for delivery for {} => {} ({}) = {}",
                key,
                address,
                body)


    }

    public boolean matchCorrelationId(Message message, String id){

        boolean ret = false
        try{
            ret =  message.correlationId() == id
        }catch (Exception e){}

        return ret

    }

}
