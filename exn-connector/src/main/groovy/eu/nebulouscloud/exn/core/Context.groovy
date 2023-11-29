package eu.nebulouscloud.exn.core

class Context {

    private final String uri
    private final String base
    private final Map<String,Publisher> publishers = [:]
    private final Map<String,Consumer> consumers = [:]


    public Context(String uri, String base){
        this.base = base
        this.uri = uri
    }

    def getPublisher(key) {
        publishers[key]
    }

    boolean hasPublisher(key) {
        publishers.containsKey(key)
    }

    boolean hasConsumer(key) {
        consumers.containsKey(key)
    }

    void registerPublisher(publisher) {
        publishers[publisher.key()] = publisher
    }

    void registerConsumers(consumer) {
        consumers[consumer.key()] = consumer
    }

    String buildAddressFromLink(Link link) {
        String address = link.fqdn ? link.address : "${base}.${link.address}"
        if (link.topic) {
            address = "topic://${address}"
        }
        address
    }

    boolean matchAddress(Link link, event) {
        if (!event || !event.message || !event.message.address) {
            return false
        }
        String address = buildAddressFromLink(link)
        address == event.message.address
    }

    String buildAddress(String[] actions, boolean topic = false) {
        if (actions.length <= 0) {
            return base
        }
        String address = "${base}.${actions.join('.')}"
        if (topic) {
            address = "topic://${address}"
        }
        address
    }

}
