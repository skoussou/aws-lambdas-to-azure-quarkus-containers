package com.redhat.cloudnative.hellocosmos;

import java.util.ArrayList;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.ThroughputProperties;

import java.util.List;


@ApplicationScoped
public class CosmosConnection {

    private static final Logger Log = Logger.getLogger(CosmosConnection.class);


    @ConfigProperty(name = "cosmos.database")
    protected String databaseName;

    @ConfigProperty(name = "cosmos.container")
    protected String containerName;

    @ConfigProperty(name = "cosmos.partitionkey")
    public String partitionkey;

    @ConfigProperty(name = "cosmos.master.key")
    public String masterKey;

    @ConfigProperty(name = "attribute.connection_string.0")
    public String connectionString;

    public String host;

    private CosmosDatabase database;
    private CosmosContainer container;

    private CosmosClient client;

    private void tokenizeConnectionString() {
        // Example Connection String
        //AccountEndpoint=https://<url>.documents.azure.com:443/;AccountKey=tTXNZztGBFCVQg2N1ybcaR7IKHXCsDi4d0HTAbuupbtHzt0kvmaslLrZi5nCn3plA5CMrrmrhQSdr9GuBB9pmA==;
        // Necessary because crossplane doesn't provide exactly what we need
        Log.debug("* CONNECTION String: "+ connectionString);
        String[] tokens = connectionString.split(";");
        String[] nextTokens = tokens[0].split("=");
        this.host = nextTokens[1];
    }
    @PostConstruct
    public void init()    {


        Log.debug("*******************************************");
        Log.debug("* CONNECTION STARTING                     *");
        Log.debug("*******************************************");
        this.tokenizeConnectionString();

        Log.debug("Creating Cosmos Client");
        Log.debug("Using Azure Cosmos DB endpoint: " + this.host);
        Log.debug("masterKey : " + this.masterKey);
        Log.debug("*******************************************");

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
            .endpoint(host)
            .key(masterKey)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("HelloCosmos")
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildClient();

            try {
                // Production Recommended
                // connect();

                // Testing Recommended
                createDatabaseIfNotExists();
                createContainerIfNotExists();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    private void connect() {
        //  Create database if not exists
        database = client.getDatabase(databaseName);
        container = database.getContainer(containerName);
    }

    private void createDatabaseIfNotExists() throws Exception {
        database = client.getDatabase(databaseName);
        Log.info("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());
    }

    private void createContainerIfNotExists() throws Exception {

        container = database.getContainer(containerName);
        Log.info("Create container " + containerName + " if not exists.");

        List<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add("/"+partitionkey);
        PartitionKeyDefinition subpartitionKeyDefinition = new PartitionKeyDefinition();
        subpartitionKeyDefinition.setPaths(partitionKeyPaths);
        subpartitionKeyDefinition.setKind(PartitionKind.HASH);
        subpartitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, subpartitionKeyDefinition);

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

        Log.info("Checking container " + container.getId() + " completed!\n");
    }

    public CosmosClient getClient() {
        return client;
    }

    public CosmosContainer getContainer() {
        return container;
    }

    public String getDatabase(){
        return databaseName;
    }
}
