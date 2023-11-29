package eu.nebulouscloud.exn.handlers

import eu.nebulouscloud.exn.core.Context
import org.apache.qpid.protonj2.client.Message

import java.util.concurrent.atomic.AtomicReference

/**
 * This is the main entry point once the application has started.
 *
 * Upon initialization and thread handling
 *
 *
 */
abstract class ConnectorHandler {


    /**
     * This method is called once all initilization has
     * completed and the {@link Context} has been instatiated.
     * @param context
     */
    public void onReady(Context context){

    }

}
