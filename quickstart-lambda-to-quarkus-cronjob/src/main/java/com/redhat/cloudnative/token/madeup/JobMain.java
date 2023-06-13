package com.redhat.cloudnative.token.madeup;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import com.redhat.cloudnative.token.madeup.repository.DefaultRepository;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;

@ApplicationScoped
@QuarkusMain
public class JobMain implements QuarkusApplication {

    private static final Logger Log = Logger.getLogger(JobMain.class);


    @Inject
    protected DefaultRepository repository;


    @Inject
    protected DBSeeder seeder;


    private ObjectMapper mapper;
    private HttpClient httpClient;

    @Override
    public int run(String... args) {
      Log.info("Running JobMain .... ");
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(Version.HTTP_1_1)
            .build();
        this.mapper = new ObjectMapper();

        //FIXME - Place business method here
       // this.handleRequest();
        return 0;
    }

}