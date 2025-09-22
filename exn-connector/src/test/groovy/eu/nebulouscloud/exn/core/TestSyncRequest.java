package eu.nebulouscloud.exn.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.nebulouscloud.exn.Connector;
import eu.nebulouscloud.exn.handlers.ConnectorHandler;
import eu.nebulouscloud.exn.settings.StaticExnConfig;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestSyncRequest {

    static Logger LOGGER = LoggerFactory.getLogger(TestSyncRequest.class);
    protected ObjectMapper om = new ObjectMapper();
    private Connector myEXNClient;
    static Context context;

    private TestSyncRequest() throws InterruptedException {
        String appID = UUID.randomUUID().toString();

        myEXNClient = new Connector(
                "thisINotImportant",
                new ConnectorHandler() {
                    public void onReady(Context context) {
                        TestSyncRequest.context = context;
                        LOGGER.info("Optimiser-controller connected to ActiveMQ, got connection context {}", context);
                    }
                },
                List.of(new Publisher("thisIsTheReplier", "eu.nebulouscloud.cfsb.test.get_node_candidates_multi.reply", true, true)),
                List.of(
                        new Consumer("thisIsTheConsumer", "eu.nebulouscloud.cfsb.test.get_node_candidates_multi",
                                new Handler() {
                                    @Override
                                    public void onMessage(String key, String address, Map body, Message message, Context context) {
                                        try {
                                            context.getPublisher("thisIsTheReplier")
                                                    .send(Map.of("body", "{'hello':'world'}"),
                                                            appID,
                                                            Map.of("correlation-id", message.correlationId().toString())
                                                    );

                                        } catch (ClientException e) {
                                            throw new RuntimeException(e);
                                        }

                                    }
                                }, true, true)
                ),
                new StaticExnConfig("localhost", 5672, "admin", "nebulous", 15, "eu.nebulouscloud")
        );

        myEXNClient.start();

        LOGGER.info("Starting test");
        String queryBody = "[[{\"type\":\"AttributeRequirement\",\"requirementClass\":\"hardware\",\"requirementAttribute\":\"ram\",\"requirementOperator\":\"GEQ\",\"value\":\"8192\"},{\"type\":\"AttributeRequirement\",\"requirementClass\":\"hardware\",\"requirementAttribute\":\"cores\",\"requirementOperator\":\"GEQ\",\"value\":\"4\"},{\"type\":\"NodeTypeRequirement\",\"nodeTypes\":[\"IAAS\"],\"jobIdForBYON\":\"\",\"jobIdForEDGE\":\"\"},{\"type\":\"AttributeRequirement\",\"requirementClass\":\"cloud\",\"requirementAttribute\":\"id\",\"requirementOperator\":\"EQ\",\"value\":\"669b7f95-08cb-4f6a-a830-2b7ab366194f\"}]]";
        ObjectMapper mapper = new ObjectMapper();
        int publisherNameCounter = 0;
        while (true) {
            SyncedPublisher findBrokerNodeCandidatesMultiple = null;
            try {
                findBrokerNodeCandidatesMultiple = new SyncedPublisher(
                        "findBrokerNodeCandidatesMultiple" + publisherNameCounter++,
                        "eu.nebulouscloud.cfsb.test.get_node_candidates_multi",
                        true,
                        true,1000000
                );
                Context context = TestSyncRequest.context;
                context.registerPublisher(findBrokerNodeCandidatesMultiple);
                Map<String, Object> msg = Map.of("body", queryBody);
                Map<String, Object> response = findBrokerNodeCandidatesMultiple.sendSync(msg, appID, null, false);
                ObjectNode jsonBody = mapper.convertValue(response, ObjectNode.class);

                List<JsonNode> result = Arrays.asList(
                        mapper.convertValue(
                                jsonBody.withArray("/body", JsonNode.OverwriteMode.ALL, true),
                                JsonNode[].class
                        )
                );
                LOGGER.info(appID + "->" + result.size());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (findBrokerNodeCandidatesMultiple != null) {
                    context.unregisterPublisher(findBrokerNodeCandidatesMultiple.key());
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new TestSyncRequest();
    }
}
