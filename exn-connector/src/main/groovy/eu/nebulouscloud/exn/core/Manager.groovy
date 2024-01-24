package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Connection
import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Receiver
import org.apache.qpid.protonj2.client.ReceiverOptions
import org.apache.qpid.protonj2.client.Session
import org.apache.qpid.protonj2.client.SourceOptions
import org.apache.qpid.protonj2.client.exceptions.ClientException
import org.apache.qpid.protonj2.types.Symbol
import org.apache.qpid.protonj2.types.messaging.AmqpValue
import org.apache.qpid.protonj2.types.messaging.Source
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


/**
 * This is the thread and connection handling manager.
 *
 * This class is instantiated during the bootstrap process
 * and it abstract the logic of maintaining separate threads
 * per {@link Consumer} and {@link SchedulePublisher}
 *
 * you do not need to instantiated this class. An instance
 * of this class is available in the {@link Context}
 */
class Manager {

    private Logger logger = LoggerFactory.getLogger(Manager.class)
    private final ExecutorService executorService
    private final AtomicBoolean running
    private Connection connection

    public Manager(Connection connection){
        this.connection = connection
        this.executorService = Executors.newCachedThreadPool();
        this.running = new AtomicBoolean(false);
    }

    protected boolean getRunning() {
        return running.get()
    }

    public stop(){
        this.running.set(false)
        executorService.shutdown()
    }


    public start(){
        this.running.set(true)
        this.executorService.submit(new Runnable() {
            @Override
            void run() {
                while (running){
                    Thread.sleep(1000)
                }
                logger.info("Closing")
            }
        })
    }

    /**
     * This is managed by the context, whose access is controlled by an atomic
     * reference. Should be thread safe
     *
     * @param context
     * @param consumers
     */
    protected void startPublisher(Context context, Publisher publisher) {
        String address = context.buildAddressFromLink(publisher)
        logger.debug("Registering publisher {} {} ", publisher,address)
        publisher.setLink(address,this.connection.openSender(address))

        if (publisher instanceof SchedulePublisher){
            logger.debug("Adding scheduled publisher as scheduled publisher {}", publisher)
            this.executorService.submit(
                    new Runnable() {
                        @Override
                        void run() {
                            boolean healthy = true
                            while(healthy && running){
                                try{
                                    logger.debug("Processing scheduled executor [{}] {}  ", publisher.key, address)
                                    publisher.send()
                                    logger.debug("\t waiting for  {} = {}  ",address, publisher.delay)
                                    Thread.sleep(publisher.delay*1000)
                                }catch (Exception e){
                                    logger.error("Error processing scheduled executor [{}] - disabling", publisher.key,e)
                                    healthy=false
                                }
                            }
                        }
                    }

            )
        }
        if(publisher instanceof SyncedPublisher){
            final p = publisher
            startConsumer(context, new Consumer(
                    publisher.key+"-reply",
                    publisher.replyAddress,
                    new Handler() {
                        @Override
                        void onMessage(String key, String onAddress, Map body, Message message, Context onAcontext) {
                            if(p.hasCorrelationId(message.correlationId())){
                                p.replied.set(body)
                            }
                        }
                    },
                    publisher.replyTopic,
                    publisher.replyFQDN
            ))
        }
    }


    /**
     * This is managed by the context, whose access is controlled by an atomic
     * reference. Should be thread safe
     *
     * @param context
     * @param consumers
     */
    protected startConsumer(Context context, Consumer consumer) {
        logger.debug("Starting consumer {} => {}", consumer.key(),consumer.address())

        CountDownLatch ready = new CountDownLatch(1)
        executorService.submit(new Runnable() {
            @Override
            void run() {
                String address = context.buildAddressFromLink(consumer)
                try {
                    Session session = connection.openSession().openFuture().get();

                    Source source = new Source();
                    source.setAddress(consumer.address());

                    ReceiverOptions receiverOptions = new ReceiverOptions();

                    if(consumer.hasApplication()){
                        String filterExpression = "application = '"+consumer.aplication+"'";
                        receiverOptions.sourceOptions().filters(
                            Collections.singletonMap(
                                    "apache.org:selector-filter:string",
                                    new AmqpValue(filterExpression))
                        );
                    }

                    Receiver receiver = session.openReceiver(address,receiverOptions).openFuture().get();

                    logger.info("Linking consumer {}", address)
                    if (consumer.hasApplication()){
                        logger.info("\t for application {}", consumer.getAplication())
                    }
                    consumer.setLink(address,receiver)
                    ready.countDown()
                    while (running && consumer.getActive()) {
                        Delivery delivery = receiver.receive();
                        logger.debug("received delivery {}", address)
                        if (delivery != null) {
                            if(consumer.hasApplication()){
                                if(consumer.getAplication() == delivery.message().subject()){
                                    consumer.onDelivery(delivery, context)
                                }
                            }else{
                                consumer.onDelivery(delivery, context)

                            }
                        }
                    }
                    logger.info("Stopping consumer {}", address)
                    receiver.close();
                    session.close();
                } catch (ClientException e) {
                    logger.error("Client exception for {} ",address,e)
                } catch (Exception e){
                    logger.error("General exception for {} ",address,e)
                }
            }
        });


        ready.await(5000, TimeUnit.SECONDS)

    }


}
