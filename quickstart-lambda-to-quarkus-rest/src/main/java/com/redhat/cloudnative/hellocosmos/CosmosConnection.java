package com.wirelesscar.vw.residency.hellocosmos;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

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
import com.azure.cosmos.models.ThroughputProperties;

@ApplicationScoped
public class CosmosConnection {

    @ConfigProperty(name = "cosmos.database")
    protected String databaseName;

    @ConfigProperty(name = "cosmos.container")
    protected String containerName;

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
        //AccountEndpoint=https://residency-token-hoarder-db-account.documents.azure.com:443/;AccountKey=tTXNZztGBFCVQg2N1ybcaR7IKHXCsDi4d0HTAbuupbtHzt0kvmaslLrZi5nCn3plA5CMrrmrhQSdr9GuBB9pmA==;
        // Necessary because crossplane doesn't provide exactly what we need
        String[] tokens = connectionString.split(";");
        String[] nextTokens = tokens[0].split("=");
        this.host = nextTokens[1];
    }
    @PostConstruct
    public void init()    {

        this.tokenizeConnectionString();

        System.out.println("Creating Cosmos Client");
        System.out.println("Using Azure Cosmos DB endpoint: " + this.host);
        System.out.println("masterKey : " + this.masterKey);

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
                connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    private void connect() {
        //  Create database if not exists
        database = client.getDatabase(databaseName);
        container = database.getContainer(containerName);
    }

    public CosmosContainer getContainer() {
        return container;
    }
}
