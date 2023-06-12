package com.redhat.cloudnative.hellocosmos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.redhat.cloudnative.hellocosmos.model.HelloCountry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;


@ApplicationScoped
@Path("/hello")
public class HelloCosmosResource {

    @Inject
    MeterRegistry registry;
       
    @Inject
    protected CosmosConnection connection;
       
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{country}")
    public String hello(String country) {

        System.out.println(String.format("Creating new Country Item fpr country [%s]", country));

        // FIXME - Business Code call to Lambda hander method

        HelloCountry hc = new HelloCountry();
        hc.setHello(country);
        hc.setId(country + "-" + System.currentTimeMillis());
        hc.setPartitionKey(country);
        CosmosItemResponse item = connection.getContainer().createItem(hc, new PartitionKey(hc.getPartitionKey()), new CosmosItemRequestOptions());
        System.out.println(String.format("Created item with request charge of %.2f within" + " duration %s", item.getRequestCharge(), item.getDuration()));
        
        registry.counter("country_counter", Tags.of("name", country)).increment();

        return "Hello "+hc.getHello()+"!";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/create")
    public String helloPost(HelloCountry hc) {

        // FIXME - Business Code call to Lambda hander method

        //  <CreateItem>
        //  Create item using container that we created
        CosmosItemResponse item = connection.getContainer().createItem(hc, new PartitionKey(hc.getPartitionKey()), new CosmosItemRequestOptions());
        System.out.println(String.format("Created item with request charge of %.2f within" + " duration %s", item.getRequestCharge(), item.getDuration()));
        
        registry.counter("country_counter", Tags.of("name", hc.getHello())).increment();

        return "Hello "+hc.getHello()+"!";
    }

}
