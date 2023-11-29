package eu.nebulouscloud.exn.core

import eu.nebulouscloud.exn.handlers.ConnectorHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

public class ContextTest {

    def Context c

    @BeforeEach
    public void initContext(){
        c = new Context("uri","base",new ConnectorHandler() {
            @Override
            void onReady(Context context) {

            }
        })
    }

    @Test
    public void testPublishersRegistration(){

        def publisher = new Publisher("test","address",true)
        c.registerPublisher(publisher)

        Assertions.assertTrue(c.hasPublisher("test"))
        Assertions.assertEquals(publisher,c.getPublisher("test"))
    }


    @Test
    public void testConsumersRegistration(){

        def consumer = new Consumer("test","address",new Handler(){})
        c.registerConsumer(consumer)

        Assertions.assertTrue(c.hasConsumer("test"))
        Assertions.assertEquals(consumer,c.getConsumer("test"))
    }



    @Test
    public void testBuildAddressFromLink(){


        def consumer = new Consumer("test","address",new Handler(){})
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "topic://base.address");

        consumer = new Consumer("test","address",new Handler(){},false)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "base.address");

        consumer = new Consumer("test","address",new Handler(){},false,true)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "address");

        consumer = new Consumer("test","address",new Handler(){},true,true)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "topic://address");


        def publisher = new Publisher("test","address",true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "topic://base.address");

        publisher = new Publisher("test","address",false)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "base.address");

        publisher = new Publisher("test","address",false,true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "address");

        publisher = new Publisher("test","address",true,true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "topic://address");

    }

    @Test
    public void testMatchAddress(){


        def consumer = new Consumer("test","address",new Handler(){},false)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "base.address");

        consumer = new Consumer("test","address",new Handler(){},false,true)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "address");

        consumer = new Consumer("test","address",new Handler(){},true,true)
        Assertions.assertEquals(c.buildAddressFromLink(consumer), "topic://address");


        def publisher = new Publisher("test","address",true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "topic://base.address");

        publisher = new Publisher("test","address",false)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "base.address");

        publisher = new Publisher("test","address",false,true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "address");

        publisher = new Publisher("test","address",true,true)
        Assertions.assertEquals(c.buildAddressFromLink(publisher), "topic://address");

    }


}
