package eu.nebulouscloud.exn;


import eu.nebulouscloud.exn.core.Context;
import eu.nebulouscloud.exn.core.Publisher;
import eu.nebulouscloud.exn.core.StatePublisher;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.util.List;
import java.util.Map;

class MyPublisher extends Publisher {

    public MyPublisher() {
        super("preferences", "preferences.changed", true);
    }

    public void send(){
        super.send(Map.of(
                "dark_mode",true
                    ),"a");
    }
}


class MyPublisherTestConnectorHandler extends ConnectorHandler {


    @Override
    public void onReady(Context context) {
        System.out.println("Ready");


        /*
            Here we are checking to see inf the default `state` publisher is
            available. Even though this should be already be known by the
            developer, a check never did any harm.

            The state publisher is a core publisher with the required
            methods to publish component state.

            Calling these methods and bootstrapping them into the application
            logic falls on the developer.

         */


        if(context.hasPublisher("state")){

            StatePublisher sp = (StatePublisher) context.getPublisher("state");

            sp.starting();
            sp.started();
            sp.custom("forecasting");
            sp.stopping();
            sp.stopped();

        }

        /**
         * This is an example of a default Publisher just sending an arbitrary message
         *
         */
        if(context.hasPublisher("config")) {

            (context.getPublisher("config")).send(
                    Map.of("hello","world"),
                    "one"
            );
            (context.getPublisher("config")).send(
                    Map.of("good","bye"),
                    "two"
            );

        }

        /**
         * This is an example of an extended publisher where the body of the message
         * is managed internally by the class
         */
        (context.getPublisher("preferences")).send();

    }

}

class TestPublisher{
    public static void main(String[] args) {
        try {

            Connector c = new Connector(

                    "ui",
                    new MyPublisherTestConnectorHandler(),
                    List.of(
                            new Publisher("config","config",true),
                            new MyPublisher()
                    ),
                    List.of(),
                    true,
                    true,
                    new StaticExnConfig(
                            "localhost",
                            5672,
                            "admin",
                            "admin",
                            5
                    )

            );

            c.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
