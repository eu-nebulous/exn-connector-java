package eu.nebulouscloud.exn.core

class SchedulePublisher extends Publisher{
    private final int delay

    SchedulePublisher(Integer delay, String key, String address, boolean Topic, boolean FQDN) {
        super(key, address, Topic, FQDN)
        this.delay = delay
    }


}
