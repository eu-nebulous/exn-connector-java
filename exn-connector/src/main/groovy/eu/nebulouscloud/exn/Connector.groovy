package eu.nebulouscloud.exn

import eu.nebulouscloud.exn.core.*
import eu.nebulouscloud.exn.handlers.ConnectorHandler
import eu.nebulouscloud.exn.settings.ExnConfig
import org.aeonbits.owner.ConfigFactory
import org.apache.qpid.protonj2.client.*
import org.apache.qpid.protonj2.client.exceptions.ClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

public class Connector {

    private static final Logger logger = LoggerFactory.getLogger(Connector.class)
    private final String component
    private final Publisher[] publishers
    private final Consumer[] consumers
    private final ExecutorService executorService
    private Connection connection
    private ExnConfig config
    private final AtomicBoolean running
    private final AtomicReference<Context> context
    private final AtomicReference<ConnectorHandler> handler


    public Connector(
            String component,
            ConnectorHandler handler,
            List<Publisher> publishers,
            List<Consumer> consumers,
            boolean enableState = true,
            boolean enableHealth = true,
            ExnConfig configuration
    ) {

        assert component
        this.component = component
        this.consumers = consumers
        this.running = new AtomicBoolean(true);
        this.handler = new AtomicReference<>(handler)
        this.config = ConfigFactory.create(ExnConfig.class)

        if (configuration == null ){
            configuration = ConfigFactory.create(ExnConfig.class)
        }

        this.config = configuration
        this.context = new AtomicReference<>(
                new Context(
                        "${configuration .url()}:${configuration .port()}",
                        "${configuration .baseName()}.${this.component}"
                )
        )

        List<Publisher> compiledPublishers = new ArrayList<>()
        if (enableState) {
            compiledPublishers.add(
                    new StatePublisher()
            )
        }

        if (enableHealth) {
            compiledPublishers.add(
                    new SchedulePublisher(
                            this.config.healthTimeout(),
                            'health',
                            'health',
                            true,
                            false
                    )
            )
        }
        compiledPublishers.addAll(publishers)
        this.publishers = compiledPublishers
        this.executorService = Executors.newCachedThreadPool();

    }


    private void startQueueListener(Consumer consumer) {
        executorService.submit(new Runnable() {
            @Override
            void run() {
                String address = context.get().buildAddressFromLink(consumer)
                try {
                    Session session = connection.openSession().openFuture().get();
                    Receiver receiver = session.openReceiver(address).openFuture().get();
                    consumer.setLink(address,receiver)
                    while (running.get()) {
                        Delivery delivery = receiver.receive();
                        if (delivery != null) {
                            consumer.onDelivery(delivery, context)
                        }
                    }
                    receiver.close();
                    session.close();
                } catch (ClientException e) {
                    logger.error("Client exception for {} ",address,e)
                } catch (Exception e){
                    logger.error("General exception for {} ",address,e)
                }
            }
        });
    }


    public void stop() {
        try {
            running.set(false)
            connection.close()
            executorService.shutdown()
            logger.info("Connector stopped gracefully.")
        } catch (ClientException e) {
            logger.error("Error stopping connector ", e)
        }
    }

    public start() {
        logger.info("Starting connector...")

        try {
            final Client client = Client.create();
            final ConnectionOptions connectionOpts = new ConnectionOptions();
            connectionOpts.user(config.username());
            connectionOpts.password(config.password());
            connectionOpts.reconnectEnabled(true);
            this.connection = client.connect(config.url(), config.port(), connectionOpts)
            for (Publisher p : publishers) {

                String address = this.context.get().buildAddressFromLink(p)
                p.setLink(address,connection.openSender(address))
                logger.debug("Registering publisher {}", p)
                this.context.get().registerPublisher(p)

                if (p instanceof SchedulePublisher){
                    logger.debug("Adding scheduled publisher as scheduled publisher {}", p)
                    final Publisher threadPublisher = p;
                    this.executorService.submit(
                        new Runnable() {
                            @Override
                            void run() {
                                boolean healthy = true
                                while(healthy && running.get()){
                                    try{
                                        logger.debug("Processing scheduled executor [{}] {}  ", threadPublisher.key, address)
                                        threadPublisher.send()
                                        logger.debug("\t waiting for  {} = {}  ",address, threadPublisher.delay)
                                        Thread.sleep(threadPublisher.delay*1000)
                                    }catch (Exception e){
                                        logger.error("Error processing scheduled executor [{}] - disabling", threadPublisher.key,e)
                                        healthy=false
                                    }
                                }
                            }
                        }
                    )
                }


            }

            for (Consumer c : consumers) {
                logger.debug("Registering consumer {}", c)
                this.context.get().registerConsumers(c)
                this.startQueueListener(c)
            }

            this.executorService.submit(new Runnable() {
                @Override
                void run() {
                    while (running.get()){
                        Thread.sleep(1000)
                    }
                }
            })

            this.handler.get().setReady(this.context)


        } catch (Exception e) {
            logger.error("Error starting connector", e)
        }

    }

}


