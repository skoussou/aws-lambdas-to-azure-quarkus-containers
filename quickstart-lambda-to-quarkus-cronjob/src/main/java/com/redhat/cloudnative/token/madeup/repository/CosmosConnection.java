package com.redhat.cloudnative.token.madeup.repository;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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

import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class CosmosConnection {

    private static final Logger Log = Logger.getLogger(CosmosConnection.class);

    @ConfigProperty(name = "cosmos.database")
    protected String databaseName;

    @ConfigProperty(name = "cosmos.container")
    protected String containerName;

    @ConfigProperty(name = "attribute.primary_master_key")
    public String masterKey;

    @ConfigProperty(name = "attribute.connection_string.0")
    public String connectionString;

    @ConfigProperty(name = "cosmos.partitionkey")
    public String partitionkey;   

    public String host; 
    private CosmosDatabase database;
    private CosmosContainer container;

    private CosmosClient client;

    public CosmosConnection(){
        Log.info("Creating Cosmos Client Constructor");
    }

    private void tokenizeConnectionString() {
        // Example Connection String
        //AccountEndpoint=https://<domain>.documents.azure.com:443/;AccountKey=tTXNZztGBFCVQg2N1ybcaR7IKHXCsDi4d0HTAbuupbtHzt0kvmaslLrZi5nCn3plA5CMrrmrhQSdr9GuBB9pmA==;
        String[] tokens = connectionString.split(";");
        String[] nextTokens = tokens[0].split("=");
        this.host = nextTokens[1];
    }

    @PostConstruct
    public void init()    {

        this.tokenizeConnectionString();

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
            .endpoint(host)
            .key(masterKey)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("TokenCache")
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildClient();

            try {
                createDatabaseIfNotExists();
                createContainerIfNotExists();
                //scaleContainer();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    private void createDatabaseIfNotExists() throws Exception {
        database = client.getDatabase(databaseName);
        if (database == null)   {
            Log.info("Create database " + databaseName + " if not exists.");

            //  Create database if not exists
            CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
            database = client.getDatabase(databaseResponse.getProperties().getId());

            Log.info("Checking database " + database.getId() + " completed!\n");
        }
    }

    private void createContainerIfNotExists() throws Exception {

        container = database.getContainer(containerName);
        if (container == null)  {
            System.out.println("Create container " + containerName + " if not exists.");

            //  Create container if not exists

            List<String> partitionKeyPaths = new ArrayList<String>();
            partitionKeyPaths.add("/clientId");
            PartitionKeyDefinition subpartitionKeyDefinition = new PartitionKeyDefinition();
            subpartitionKeyDefinition.setPaths(partitionKeyPaths);
            subpartitionKeyDefinition.setKind(PartitionKind.HASH);
            subpartitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
            
            //  <CreateContainerIfNotExists>
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, subpartitionKeyDefinition);    

            CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
            container = database.getContainer(containerResponse.getProperties().getId());
        }

        Log.info("Checking container " + container.getId() + " completed!\n");
    }

    private void scaleContainer() throws Exception {
        Log.info("Scaling container " + containerName + ".");

        try {
            // You can scale the throughput (RU/s) of your container up and down to meet the needs of the workload. Learn more: https://aka.ms/cosmos-request-units
            ThroughputProperties currentThroughput = container.readThroughput().getProperties();
            int newThroughput = currentThroughput.getManualThroughput() + 100;
            container.replaceThroughput(ThroughputProperties.createManualThroughput(newThroughput));
            Log.info("Scaled container to " + newThroughput + " completed!\n");
        } catch (CosmosException e) {
            if (e.getStatusCode() == 400)
            {
                System.err.println("Cannot read container throuthput.");
                System.err.println(e.getMessage());
            }
            else
            {
                throw e;
            }
        }
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