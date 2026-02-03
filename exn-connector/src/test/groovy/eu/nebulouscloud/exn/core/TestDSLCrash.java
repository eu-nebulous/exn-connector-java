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

public class TestDSLCrash {

	static Logger LOGGER = LoggerFactory.getLogger(TestDSLCrash.class);
	private Connector myEXNClient;

	private class MyConnectorHandler extends ConnectorHandler {

		@Override
		public void onReady(Context context) {
			LOGGER.info("Starting test");
		}
	}

	private TestDSLCrash() throws InterruptedException {
		myEXNClient = new Connector(
				"thisINotImportant",
				new MyConnectorHandler(),
				List.of(
				),
				List.of(
					new Consumer("synced-consumer", "eu.nebulouscloud.ui.dsl.generic", new Handler() {
						@Override
						public void onMessage(String key, String address, Map body, Message message, Context context) {
							LOGGER.info("Received a message ke '{}' from '{}'",key,address);
                        }
					},true,true)
				),
				false,
				false, new StaticExnConfig("localhost", 61616, "admin", "zFLM8zMsPq1mcafK"));
		myEXNClient.start();
	}

	public static void main(String[] args) throws InterruptedException {
		new TestDSLCrash();
	}
}
