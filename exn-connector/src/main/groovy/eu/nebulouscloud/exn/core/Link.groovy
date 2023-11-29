package eu.nebulouscloud.exn.core

abstract class Link<T extends org.apache.qpid.protonj2.client.Link<T>>{

    protected String key
    protected String address
    public String linkAddress
    public boolean topic
    public boolean fqdn = false
    public org.apache.qpid.protonj2.client.Link<T> link

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
    }

    public String key(){
        return this.key
    }
    public String address(){
        return this.key
    }

    public setLink(String address, org.apache.qpid.protonj2.client.Link<T> link){
        this.link = link
        this.linkAddress =address
    }

}
