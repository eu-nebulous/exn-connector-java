package eu.nebulouscloud.exn.core


import org.apache.qpid.protonj2.client.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

/**
 * This is the core publisher class which abstract the logic to
 * publish events.
 *
 *
 * Using this class you define the AMQP address for which you wish
 * to publish messages.
 *
 * The class takes care of preparing the the {@link Message} including
 * content-type, message payload, and serialization
 *
 */


class SyncedPublisher extends Publisher{

    private static final Logger logger = LoggerFactory.getLogger(SyncedPublisher.class)

    public String replyAddress
    public boolean replyTopic
    public boolean replyFQDN
    protected AtomicReference<Map> replied
    private ConcurrentHashMap<String,Future<Map>> correlationIds
    private ExecutorService executor

    SyncedPublisher(String key, String address, boolean Topic, boolean FQDN=false,
                    String replyAddress="reply") {
        super(key, address, Topic, FQDN)
        this.replyAddress = this.address+"."+replyAddress
        this.replyTopic = Topic
        this.replyFQDN = FQDN
        this.replied = new AtomicReference<>()
        this.correlationIds = new ConcurrentHashMap<>()
        this.executor= Executors.newSingleThreadExecutor()
    }


    public Map sendSync(Map body, String application, Map<String,String> properties, boolean raw) {
        String correlationId = UUID.randomUUID().toString().replace("-", "")

        Map<String,String> sendPropeties = new HashMap<>()

        if(properties != null){
            sendPropeties.putAll(properties)
            if(properties.containsKey("correlation-id")){
                correlationId = properties.get("correlation-id")

            }
        }else{
            sendPropeties.put("correlation-id",correlationId)
        }

        this.replied.set(null)

        Future<Map> res= executor.submit {
            while (replied.get() == null) {
                Thread.sleep(50)
            }
        }
        correlationIds.put(correlationId, res)


        send(body,application,sendPropeties,raw)
        res.get()
        Map ret = this.replied.get()
        this.replied.set(null)
        return ret
    }

    public boolean hasCorrelationId(String correlationId){
        return this.correlationIds.containsKey(correlationId)
    }


}
