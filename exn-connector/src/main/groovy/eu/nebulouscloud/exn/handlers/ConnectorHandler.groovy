package eu.nebulouscloud.exn.handlers

import eu.nebulouscloud.exn.core.Context
import org.apache.qpid.protonj2.client.Message

import java.util.concurrent.atomic.AtomicReference

abstract class ConnectorHandler {

    boolean initialized=false
    private AtomicReference<Context> context

    public setReady(AtomicReference<Context> context){
        this.initialized = true
        this.context = context
        this.onReady(context)
    }


    public onReady(AtomicReference<Context> context){

    }

}
