package eu.nebulouscloud.exn

import eu.nebulouscloud.exn.core.*
import eu.nebulouscloud.exn.handlers.CoreHandler
import eu.nebulouscloud.exn.settings.ExnConfig
import org.aeonbits.owner.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Connector {

    private static final Logger logger = LoggerFactory.getLogger(Connector.class)
    private final String component
    private final Bootstrap bootstrap
    private final Publisher[] publishers
    private final Consumer[] consumers
    private final CoreHandler handler

    public Connector(
            String component,
            Bootstrap bootstrap,
            List<Publisher> publishers,
            List<Consumer> consumers,
            boolean enableState = true,
            boolean enableHealth = true
    ){

        assert component

        this.consumers = consumers
        this.publishers = publishers
        this.bootstrap = bootstrap
        this.component = component

        ExnConfig config = ConfigFactory.create(ExnConfig.class)
        Context context = new Context(
                "${config.url()}:${config.port()}",
                "${config.baseName()}.${this.component}"
        )


        if(enableState){
            publishers.add(
                    new StatePublisher()
            )
        }

        if(enableHealth){
            publishers.add(
                    new SchedulePublisher(
                            config.healthTimeout(),
                            'health',
                            'health',
                            true,
                            false
                    )
            )
        }

        this.handler = new CoreHandler(
                context,
                bootstrap,
                publishers,
                consumers
        )


    }

    public start(){
        this.handler.start()
    }

}