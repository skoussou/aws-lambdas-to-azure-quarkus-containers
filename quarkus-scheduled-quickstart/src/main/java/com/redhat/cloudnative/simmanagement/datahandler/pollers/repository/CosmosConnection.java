package com.redhat.cloudnative.simmanagement.datahandler.pollers.repository;

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
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Startup
@ApplicationScoped
public class CosmosConnection {

    private static final Logger Log = Logger.getLogger(CosmosConnection.class);

    //FIXME - Look for cosmos in application.properties and update accordingly
    @ConfigProperty(name = "cosmos.database")
    protected String databaseName;

    @ConfigProperty(name = "cosmos.container")
    protected String containerName;

    @ConfigProperty(name = "cosmos.partitionkey")
    public String partitionkey;

    @ConfigProperty(name = "cosmos.host")
    public String host;

    @ConfigProperty(name = "cosmos.master.key")
    public String masterKey;

    private CosmosDatabase database;
    private CosmosContainer container;

    private CosmosClient client;

    public CosmosConnection(){
        Log.debug("Creating Cosmos Client Constructor");
    }

    @PostConstruct
    public void init()    {

        Log.info("Creating Cosmos Client for container : "+ containerName);

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
            .endpoint(host)
            .key(masterKey)
            .preferredRegions(preferredRegions)
            .userAgentSuffix(containerName)
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildClient();

            try {
                createDatabaseIfNotExists();
                createContainerIfNotExists();
            } catch (Exception e) {
                e.printStackTrace();
            }

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