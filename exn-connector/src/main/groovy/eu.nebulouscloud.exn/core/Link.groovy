package eu.nebulouscloud.exn.core

import org.apache.qpid.protonj2.client.Delivery
import org.apache.qpid.protonj2.client.Link
import org.apache.qpid.protonj2.client.Message

abstract class Link<T extends org.apache.qpid.protonj2.client.Link<T>>{

    protected String key
    protected String address
    public boolean topic
    public boolean fqdn = false
    public org.apache.qpid.protonj2.client.Link<T> link

    public Link(
            String key,
            String address,
            boolean Topic,
            boolean FQDN
    ){


        this.fqdn = FQDN
        this.topic = Topic
        this.address = address
        this.key = key
    }

    public setLink(org.apache.qpid.protonj2.client.Link<T> link, Bootstrap bootstrap){
        this.link = link
    }

}
