package eu.nebulouscloud.exn.core


import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Receiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the core consumer class which abstract the logic to
 * receive the event.
 *
 *
 * Using this class you define the AMQP address for which you wish
 * to receive messages.
 *
 * Once a message is received this can then be handled by a {@link Handler}
 * instance
 *
 */
class Consumer extends Link<Receiver>{


    private static final Logger logger = LoggerFactory.getLogger(Consumer.class)
    private Handler handler
    private String application

    /**
     *
     * @param key This is unique identifier of the Consumer.
     * @param address This is the AMQP address which will be appended to the
     *       {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()} for example
     *       if the base name is "foo", and the component is "bar" and the address is "hello"
     *       the AMQP address will be compiled as "foo.bar.hello"
     *
     * @param handler This is {@link Handler} class which you will use the process the message
     * @param application This is an optional key to filter messages for a specific application
     * @param topic A boolean parameter defining wether the address relates to a topic of a queue
     *        if it is a topic then "topic://" will be pre-appended to the address so the
     *        result will be "topic://foo.bar.hello"
     * @param FQDN - If you wish to ignore the {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()}
     *               and subscribe to an arbitrary address, then set this to true, and you are
     *               responsible for writing the fully qualified address for the {@link #address}
     *               parameter
     */
    Consumer(String key, String address, Handler handler, String application, boolean topic=true, boolean FQDN=false) {
        super(key, address, topic, FQDN)
        this.handler = handler
        this.application = application
    }


    Consumer(String key, String address, Handler handler, boolean topic=true, boolean FQDN=false) {
        this(key, address, handler, null, topic,FQDN)
    }

    public boolean hasApplication(){
        return this.application != null
    }

    public String getAplication(){
        return this.application
    }


    protected Map processMessage(Message message, Context context){
        logger.debug("Processing message for{}",this.linkAddress)
        return (Map)message.body()
    }

    protected void onDelivery(Delivery delivery, Context context){
        logger.debug("Default on delivery for delivery for {}",this.linkAddress)
        try {
            Message message = delivery.message();
            Map body = this.processMessage(message, context)
            this.handler.onMessage(
                    this.key,
                    this.address,
                    body,
                    message,
                    context
            )
            delivery.accept()
        }catch (Exception e){
            if(!delivery.state().isAccepted()){
                delivery.reject('Generic onMessage error',e.message)
            }
            logger.error('Generic onMessage error',e)
        }

    }

}
