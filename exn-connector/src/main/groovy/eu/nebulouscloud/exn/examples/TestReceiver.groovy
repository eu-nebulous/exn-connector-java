import eu.nebulouscloud.exn.Connector
import eu.nebulouscloud.exn.core.Bootstrap
import eu.nebulouscloud.exn.core.Consumer
import eu.nebulouscloud.exn.core.Context
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MyBootstrap extends Bootstrap{
    private static final Logger logger = LoggerFactory.getLogger(MyBootstrap.class)

    @Override
    def onMessage(String key, String address, Map body, Context context) {

        if(key == "ui_health"){
            logger.info("Received {} => {}", key, address)
        }

        if(key == "ui_all"){
            logger.info("These are my preferences => {}",body)
        }

    }
}

public static void main(String[] args) throws Exception {

    Connector c = new Connector(
            "ui",
            new MyBootstrap(),
            [],
            [
                new Consumer("ui_health","health", true),
                new Consumer("ui_all","eu.nebulouscloud.ui.preferences.>", true,true),
            ]
    )

    c.start()

}