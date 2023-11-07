package eu.nebulouscloud.exn.core

abstract class Bootstrap {

    boolean running=true
    boolean initialized=false
    private Context context

    public setReady(Context context){
        this.initialized = true
        this.context = context
    }

    public stop(){
        this.running=false
    }

    abstract public onMessage(String key, String address, Map body, Context context)


}
