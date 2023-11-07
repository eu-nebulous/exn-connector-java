package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Receiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Consumer extends Link<Receiver>{

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class)

    Consumer(String key, String address, boolean Topic=true, boolean FQDN=false) {
        super(key, address, Topic, FQDN)
    }

    @Override
    public setLink(org.apache.qpid.protonj2.client.Link<Receiver> link, Bootstrap bootstrap){
        super.setLink(link,bootstrap)
        while(bootstrap.running){
            Delivery delivery = this.link.receive()
            if(delivery != null){
                Message<Map<String,Object>> receive = delivery.message()
                this.onMessage(receive.body())
            }
        }

    }

    public onMessage(Map<String,Object> body){
        logger.debug("{} Got {}", this.address, body)
    }

}
