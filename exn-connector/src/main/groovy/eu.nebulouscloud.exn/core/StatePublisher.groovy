package eu.nebulouscloud.exn.core

class StatePublisher extends Publisher{
    StatePublisher() {
        super("state", "state", true, false)
    }

    private sendStateMessage(String state){
        this.send(["state": message_type,"message": None])
    }

    public starting(){
        this.sendStateMessage("starting")
    }

    def started(){
        this.sendStateMessage("started")
    }

    def ready(){
        this.sendStateMessage("ready")
    }

    def stopping(){
        this.sendStateMessage("stopping")
    }

    def stopped(){
        this.sendStateMessage("stopped")
    }

    def custom(state){
        this.sendStateMessage(state)
    }

}
