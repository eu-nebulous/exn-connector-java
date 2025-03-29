package eu.nebulouscloud.exn.core;

import eu.nebulouscloud.exn.Connector;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 */

public class TestOpenConnection {

	static Logger LOGGER = LoggerFactory.getLogger(TestOpenConnection.class);
	private Connector myEXNClient;

	private class MyConnectorHandler extends ConnectorHandler {

		@Override
		public void onReady(Context context) {


			LOGGER.info("Starting test");
			String appId ="1234";

			SyncedPublisher sp = (SyncedPublisher) context.getPublisher("synced-publisher");
			Object ret = sp.sendSync(
					Map.of("appId",appId,
							"index", 1
					),appId,null,true
			);
			LOGGER.info("{}",ret);

			// This is how to unregister a published and the synced publishers
			context.unregisterPublisher("synced-publisher");


			LOGGER.info("Does the consumer still exist? {}, ",context.hasConsumer("synced-publisher-reply") );

			// This is will also attempt to close the link if it is still open
			myEXNClient.stop();
		}
	}

	private TestOpenConnection() throws InterruptedException {
		myEXNClient = new Connector(
				"thisINotImportant",
				new MyConnectorHandler(),
				List.of(
					new SyncedPublisher("synced-publisher", "eu.nebulouscloud.exn.test.get", true, true),
					new Publisher("publisher-reply", "eu.nebulouscloud.exn.test.get.reply", true, true)
				),
				List.of(
					new Consumer("synced-consumer", "eu.nebulouscloud.exn.test.get", new Handler() {
						@Override
						public void onMessage(String key, String address, Map body, Message message, Context context) {
                            String correlationId= null;
							try {
								correlationId= (String) message.correlationId();
                            } catch (ClientException e) {
                                throw new RuntimeException(e);
                            }
							HashMap m = new HashMap();
							m.put("hell","world");
							m.put("appId", body.get("appId"));
							Map<String, String> stringObjectMap = Map.of("correlation-id", correlationId);
							((Publisher)context.getPublisher("publisher-reply")).send(
									m , String.valueOf(body.get("appId")), stringObjectMap);
                        }
					},true,true)
				),
				false,
				false, new StaticExnConfig("localhost", 5672, "admin", "admin"));
		myEXNClient.start();
	}

	public static void main(String[] args) throws InterruptedException {
		new TestOpenConnection();
	}
}
