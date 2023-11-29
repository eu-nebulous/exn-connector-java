import eu.nebulouscloud.exn.Connector
import eu.nebulouscloud.exn.core.Consumer
import eu.nebulouscloud.exn.core.Context
import eu.nebulouscloud.exn.core.Publisher
import eu.nebulouscloud.exn.core.StatePublisher
import eu.nebulouscloud.exn.handlers.ConnectorHandler
import eu.nebulouscloud.exn.settings.ExnConfig
import eu.nebulouscloud.exn.settings.StaticExnConfig
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.exceptions.ClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference

class MyPublisher extends Publisher{

    MyPublisher() {
        super('preferences', 'preferences.changed', true)
    }

    public send(){
        super.send([
                "preferences": [
                        "dark_mode": true
                ]
        ])
    }
}


class MyConnectorHandler extends ConnectorHandler {


    @Override
    def onReady(AtomicReference<Context> context) {
        println ("Ready")


        /*
            Here we are checking to see inf the default `state` publisher is
            available. Even though this should be already be known by the
            developer, a check never did any harm.

            The state publisher is a core publisher with the required
            methods to pubilsh component state.

            Calling these methods and bootstraing them into the application
            logic falls on the developer.

         */


        if(context.get().hasPublisher('state')){

            StatePublisher sp = context.get().getPublisher("state") as StatePublisher

            sp.starting()
            sp.started()
            sp.custom("forecasting")
            sp.stopping()
            sp.stopped()

        }

        /**
         * This is an example of a default Publisher just sending an arbitrary message
         *
         */
        (context.get().getPublisher("config") as Publisher).send([
                "hello": "world"
        ] as Map)

        /**
         * This is an example of an extended publisher where the body of the message
         * is managed internally by the class
         */
        (context.get().getPublisher("preferences") as MyPublisher).send()

    }

}


public static void main(String[] args) {
    try {


        Connector c = new Connector(

                "ui",
                new MyConnectorHandler(),
                [
                        new Publisher("config","config",true),
                        new MyPublisher()
                ],
                [],
                true,
                true,
                new StaticExnConfig(
                        'localhost',
                        5672,
                        "admin",
                        "admin"
                )

        )

        c.start()
    } catch (ClientException e) {
        e.printStackTrace();
    }
}

