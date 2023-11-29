package eu.nebulouscloud.exn.core



/**
 * This is an extension of the {@link Publisher} class
 * which abstracts the definition to send component states,
 * which are required by the NebulOuScomponentns.
 *
 * An instance of this class is created during the boostrap
 * process and available using the `state` key in the
 * {@link Context}
 *
 */
class StatePublisher extends Publisher{
    StatePublisher() {
        super("state", "state", true, false)
    }

    private sendStateMessage(String state){
        this.send(["state": state, "message":""])
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
