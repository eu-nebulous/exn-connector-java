package eu.nebulouscloud.exn

import eu.nebulouscloud.exn.core.*
import eu.nebulouscloud.exn.handlers.ConnectorHandler
import eu.nebulouscloud.exn.settings.ExnConfig
import org.aeonbits.owner.ConfigFactory
import org.apache.qpid.protonj2.client.Client
import org.apache.qpid.protonj2.client.Connection
import org.apache.qpid.protonj2.client.ConnectionOptions
import org.apache.qpid.protonj2.client.exceptions.ClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * This is the connector class of the EXNConnector. Through this
 * class you connect to the broker, defined the default consumers
 * and publishers.
 *
 * This abstract all the boiler plate required.
 *
 */
public class Connector {

    private static final Logger logger =  LoggerFactory.getLogger(Connector.class)
    private final String component
    private ExnConfig config
    private final Context context
    private Connection connection
    private Manager manager


    /**
     *
     * @param component This is the name of your component. It will be used in the
     *                  AMPQ addresses generated when FQDN is false. If the {@link ExnConfig#baseName()}
     *                  is 'eu.nebulouscloud' and your component name is 'ui', and your {@link Link#address} has
     *                  a value of 'config', then the AMQP address will be 'eu.nebulouscloud.ui'
     * @param handler This is the {@link ConnectorHandler} which will be called once the initialization process
     *                is complete
     * @param publishers A  list of publisher which will be ready upon initialization and added to the
     *                   {@link Context} automatically
     * @param consumers A list of consumers which will be ready upon initialization and added to the
     *                   {@link Context} automatically
     * @param enableState This will enable the default {@link StatePublisher}
     * @param enableHealth This will enable the default {@link SchedulePublisher} which will ping
     *                     the components health at {@link ExnConfig#healthTimeout()}
     *
     * @param configuration An optional {@link eu.nebulouscloud.exn.settings.StaticExnConfig} instance
     */
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
        this.config = ConfigFactory.create(ExnConfig.class)
        if (configuration == null ){
            configuration = ConfigFactory.create(ExnConfig.class)
        }
        this.config = configuration
        this.context = new Context(
                        "${configuration.baseName()}.${this.component}",
                        handler
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


        for( Consumer c : consumers){

            this.context.registerConsumer(c)
        }
        for( Publisher p : compiledPublishers){

            this.context.registerPublisher(p)
        }


    }

    /**
     * Stop everything
     */
    public void stop() {
        this.context.stop()

        def executor = Executors.newSingleThreadScheduledExecutor()
        executor
            .schedule(new Runnable() {
                @Override
                void run() {
                    try {
                        connection.close()
                        logger.info("Connector stopped gracefully.")
                    } catch (ClientException e) {
                    }

                    executor.shutdown()
                }
            },10, TimeUnit.SECONDS)

    }

    /**
     * Start everythin
     */
    public void start() {
        logger.info("Starting connector...")

        try {

            final Client client = Client.create();
            final ConnectionOptions connectionOpts = new ConnectionOptions();
            connectionOpts.user(config.username());
            connectionOpts.password(config.password());
            connectionOpts.reconnectEnabled(true);

            this.connection = client.connect(config.url(), config.port(), connectionOpts)
            this.manager = new Manager(this.connection)
            this.context.setManager(manager)

        } catch (Exception e) {
            logger.error("Error starting connector", e)
        }

    }

}


