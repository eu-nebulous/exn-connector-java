package eu.nebulouscloud.exn.handlers

import eu.nebulouscloud.exn.core.Bootstrap
import eu.nebulouscloud.exn.core.Consumer
import eu.nebulouscloud.exn.core.Context
import eu.nebulouscloud.exn.core.Publisher
import eu.nebulouscloud.exn.settings.ExnConfig
import org.aeonbits.owner.ConfigFactory
import org.apache.qpid.protonj2.client.Client
import org.apache.qpid.protonj2.client.Connection
import org.apache.qpid.protonj2.client.ConnectionOptions
import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Receiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CoreHandler{

    private static final Logger logger = LoggerFactory.getLogger(CoreHandler.class)

    private final Context context
    private final Bootstrap bootstrap
    private final List<Publisher> publishers
    private final List<Consumer> consumers

    public CoreHandler(
            Context context,
            Bootstrap bootstrap,
            List<Publisher> publishers,
            List<Consumer> consumers
    ){

        this.consumers = consumers
        this.publishers = publishers
        this.bootstrap = bootstrap
        this.context = context
    }


    public stop(){
        this.bootstrap.running=false
    }

    public start(){

        ExnConfig config = ConfigFactory.create(ExnConfig.class)
        final Client client = Client.create();
        final ConnectionOptions connectionOpts = new ConnectionOptions();
        connectionOpts.user(config.username());
        connectionOpts.password(config.password());
        connectionOpts.reconnectEnabled(true);

        try (Connection connection = client.connect(config.url(), config.port(), connectionOpts)){

                for( Publisher p : publishers){
                    String address = this.context.buildAddressFromLink(p)
                    p.setLink(connection.openSender(address),this.bootstrap)
                    this.context.registerPublisher(p)
                    logger.debug("Registering publisher {}",p)
                }

                for( Consumer c: consumers){
                    String address = this.context.buildAddressFromLink(c)
                    logger.debug("Registering consumer {}", address)
                    c.setLink(connection.openReceiver(address),this.bootstrap)
                    this.context.registerConsumers(consumers)
                }

                this.bootstrap.setReady(this.context)

        } catch (Exception e) {
            logger.error("Error starting connector",e)
        }

    }
}
