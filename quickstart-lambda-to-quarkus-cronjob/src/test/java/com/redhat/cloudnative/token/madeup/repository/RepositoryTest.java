package com.redhat.cloudnative.token.madeup.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import org.junit.Test;

import com.google.inject.Inject;

import io.quarkus.runtime.Startup;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;

@Startup
@ApplicationScoped
@QuarkusTest
public class RepositoryTest {

    private static final Logger Log = Logger.getLogger(DefaultRepository.class);

    // @Inject
    // DefaultRepository repository = new DefaultRepository();

    @Test
    public void testDTOCreate(){
 
    }
    
    private DTO getTestDTO()    {
        DTO testDTO = new DTO();
        testDTO.setId(""+System.currentTimeMillis());
        testDTO.setAud("VW");
        testDTO.setClientId("RegPair");
        testDTO.setEndpoint("http://testregistration.com");
        testDTO.setExpMargin(23.2);
        testDTO.setPrivateKey("ASDSFSDFDSFDGFGGFHGFJHHJGHJG");  
        testDTO.setTenant("US");
        return testDTO;      
    }
}
