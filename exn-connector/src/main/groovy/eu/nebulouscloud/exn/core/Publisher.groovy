package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Message
import org.apache.qpid.protonj2.client.Sender
import org.apache.qpid.protonj2.client.Tracker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Publisher extends Link<Sender> {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class)

    Publisher(String key, String address, boolean Topic, boolean FQDN=false) {
        super(key, address, Topic, FQDN)
    }

    public send(Map body) {
        logger.debug("{} Sending {}", this.address, body)
        if(body == null){
            body = [] as Map
        }
        def message = this.prepareMessage(body)
        Tracker tracker = this.link.send(message)
        tracker.awaitSettlement();
    }

    private Message<Map<String, Object>> prepareMessage(Map body){

        def toSend=[
                "when": ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        ]

        toSend.putAll(body)
        Message<Map<String, Object>> message = Message.create(toSend);
        message.contentType("application/json")
        message.to(this.linkAddress)
        return message

    }
}
