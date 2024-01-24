package eu.nebulouscloud.exn.core

import eu.nebulouscloud.exn.handlers.ConnectorHandler
import groovy.util.logging.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/**
 * This class maintains the application states and is provided
 * inside the event loop.
 *
 * It includes a set of utilities which are helpful during
 * setup of your asynchronous application and after its
 * initialization.
 *
 * Through this class you can register consumers, as
 * well as publishers, once the event loop has been
 * initiated
 *
 */
class Context {

    Logger logger = LoggerFactory.getLogger(Context.class)

    private final String base
    private final Map<String,Publisher> publishers = [:]
    private final Map<String,Consumer> consumers = [:]
    private final ConnectorHandler handler
    private CountDownLatch registrations

    private Manager manager

    public Context(String base, ConnectorHandler handler){
        this.base = base
        this.handler = handler
    }

    Manager getManager() {
        return manager
    }

    /**
     *
     * This method is called when the context is started,
     * so it is a good location to initialize the consumers
     * already registered.
     * @param manager
     */
    public void setManager(Manager manager) {

        this.manager = manager

        logger.info("Registering {} consumers", this.consumers.size())
        this.manager.start()
        this.consumers.each({
            k,v -> {
                final Consumer c =v
                this.manager.startConsumer(this, c)

            }
        })

        logger.info("Registering {} publishers", this.publishers.size())
        this.publishers.each({
            k,v -> {

                final Publisher p =v
                this.manager.startPublisher(this, p)

            }
        })


        this.handler.onReady(this)


    }

    def Publisher getPublisher(key) {
        publishers[key] as Publisher
    }

    def Consumer getConsumer(key) {
        consumers[key] as Consumer
    }

    boolean hasPublisher(key) {
        publishers.containsKey(key)
    }

    boolean hasConsumer(key) {
        consumers.containsKey(key)
    }

    void registerPublisher(Publisher publisher) {
        if(publishers.containsKey(publisher.key)){
            logger.warn("Trying to register a publisher that is already registered {}=>{} ",publisher.key(), publisher.address())
            return
        }

        publishers[publisher.key()] = publisher
        if(this.manager !=null && this.manager.getRunning()){
            final Publisher p =publisher
            this.manager.startPublisher(this,p)
        }
    }

    void registerConsumer(Consumer consumer) {
        if(consumers.containsKey(consumer.key)){
            logger.warn("Trying to register a consumer that is already registered {}=>{} ",consumer.key(),consumer.address())
            return
        }
        logger.debug("Registering consumer {}=>{}",consumer.key(),consumer.address())
        consumers[consumer.key()] = consumer
        if(this.manager !=null && this.manager.getRunning()){
            final Consumer c = consumer
            this.manager.startConsumer(this,c)
        }
    }

    void unregisterConsumer(String key){
        logger.debug("Un-Registering consumer {}",key)
        if(consumers.containsKey(key)){
            Consumer c = consumers.get(key)
            c.active=false
            consumers.remove(key)
        }
    }

    void unregisterPublisher(String key){
        if(publishers.containsKey(key)){
            publishers.remove(key)
        }
    }


    void stop(){

        publishers.each {p -> {
            p.setActive(false)
            p.link.close()
        }}

        consumers.each {p -> {
            p.setActive(false)
            p.link.close()
        }}

        manager.stop()

    }

    String buildAddressFromLink(Link link) {
        String address = link.fqdn ? link.address : "${base}.${link.address}"
        if (link.topic) {
            address = "topic://${address}"
        }
        address
    }


}
