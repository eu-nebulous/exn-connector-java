package eu.nebulouscloud.exn.core

/**
 * This is a base class which abstract the Proton client Link
 * code, and provides a basis for the {@link Publisher} and
 * {@link Consumer} classes
 * @param <T>
 */
abstract class Link<T extends org.apache.qpid.protonj2.client.Link<T>>{

    protected String key
    protected String address
    public String linkAddress
    public boolean topic
    public boolean fqdn = false
    public org.apache.qpid.protonj2.client.Link<T> link
    private boolean active


    public Link(
            String key,
            String address,
            boolean topic,
            boolean FQDN
    ){


        this.fqdn = FQDN
        this.topic = topic
        this.address = address
        this.key = key
        this.active = true

    }

    public String key(){
        return this.key
    }

    public String address(){
        return this.address
    }

    public setLink(String address, org.apache.qpid.protonj2.client.Link<T> link){
        this.link = link
        this.linkAddress =address
    }

    boolean getActive() {
        return active
    }

    void setActive(boolean active) {
        this.active = active

    }
}
