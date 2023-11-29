package eu.nebulouscloud.exn.core

/**
 * This is an extension of the {@link Publisher} class
 * which allows the user to automatically send message
 * at scheduled intervals.
 *
 */
class SchedulePublisher extends Publisher{
    private final int delay

    /**
     *
     * @param delay The delay in seconds we wish to send the recurring messages
     * @param key This is unique identifier of the Publisher.
     * @param address This is the AMQP address which will be appended to the
     *       {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()} for example
     *       if the base name is "foo", and the component is "bar" and the address is "hello"
     *       the AMQP address will be compiled as "foo.bar.hello"
     *
     * @param topic A boolean parameter defining wether the address relates to a topic of a queue
     *        if it is a topic then "topic://" will be pre-appended to the address so the
     *        result will be "topic://foo.bar.hello"
     * @param FQDN - If you wish to ignore the {@link eu.nebulouscloud.exn.settings.ExnConfig#baseName()}
     *               and subscribe to an arbitrary address, then set this to true, and you are
     *               responsible for writing the fully qualified address for the {@link #address}
     *               parameter
     */
    SchedulePublisher(Integer delay, String key, String address, boolean Topic, boolean FQDN) {
        super(key, address, Topic, FQDN)
        this.delay = delay
    }


}
