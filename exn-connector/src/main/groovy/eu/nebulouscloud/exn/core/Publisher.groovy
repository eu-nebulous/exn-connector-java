package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Sender
import org.apache.qpid.protonj2.client.Tracker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.MenuSelectionManager
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * This is the core publisher class which abstract the logic to
 * publish events.
 *
 *
 * Using this class you define the AMQP address for which you wish
 * to publish messages.
 *
 * The class takes care of preparing the the {@link Message} including
 * content-type, message payload, and serialization
 *
 */



class Publisher extends Link<Sender> {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class)

    /**
     *
     * @param key This is unique identifier of the Publisher.
     * @param address This is the AMQP address which will be appended to the
     *       {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()} for example
     *       if the base name is "foo", and the component is "bar" and the address is "hello"
     *       the AMQP address will be compiled as "foo.bar.hello"
     *
     * @param topic A boolean parameter defining wether the address relates to a topic of a queue
     *        if it is a topic then "topic://" will be pre-appended to the address so the
     *        result will be "topic://foo.bar.hello"
     * @param FQDN - If you wish to ignore the {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()}
     *               and subscribe to an arbitrary address, then set this to true, and you are
     *               responsible for writing the fully qualified address for the {@link #address}
     *               parameter
     */
    Publisher(String key, String address, boolean Topic, boolean FQDN=false) {
        super(key, address, Topic, FQDN)
    }

    /**
     * This method send the body without filtering
     * on a specific application.
     *
     * This method should be overriden
     *
     * @param body
     * @return
     */
    public void send() {
        send(null,'',false)
    }

    public void send(Map body) {
        send(body,'',false)
    }

    public void send(Map body, String application) {
        send(body,application, false)
    }

    public void send(Map body, String application, Map<String,String> properties) {
        send(body, application, properties, false)
    }

    public void send(Map body, String application, boolean raw) {
        send(body, application, null,  raw)
    }

    /**
     * Use this method to send a message using this
     * publisher, filtering on the specific applications
     *
     * @param body This is the payload of the message
     * @param application This is the application for which to send the message to
     * @param properties This is a map of AMQP properties which can be parsed in the message generation
     *                   supported properties are
     *
     *                   - reply-to
     *                   - correlation-id
     *
     * @param raw Do not append default message keys
     * @return
     */
    public void send(Map body, String application, Map<String,String> properties, boolean raw) {

        logger.debug("{} Sending {}-> {} ", this.linkAddress, body,properties)
        if(body == null){
            body = [:] as Map
        }

        def message = this.prepareMessage(body, properties, raw)
        if(application != null && application != ''){
            message.subject(application)
            message.property('application',application)
        }
        Tracker tracker = this.link.send(message)
        tracker.awaitSettlement();
    }


    protected Message<Map<String, Object>> prepareMessage(Map body, Map<String,String> properties, boolean raw){

        def Map<String,Object> toSend=[:]

        if(!raw){
            toSend["when"] = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        }

        toSend.putAll(body)
        Message<Map<String, Object>> message = Message.create(toSend);
        message.messageId(UUID.randomUUID().toString().replace("-", ""))

        if(properties !=null){

            if(properties.containsKey('reply-to')){
                message.replyTo(properties.get('reply-to'))
            }

            if(properties.containsKey('correlation-id')){
                message.correlationId(properties.get('correlation-id'))
            }

        }


        message.contentType("application/json")
        message.correlationId()


        message.to(this.linkAddress)
        return message

    }


}
