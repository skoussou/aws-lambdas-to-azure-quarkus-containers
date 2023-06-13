package com.redhat.cloudnative.token.madeup;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import com.redhat.cloudnative.token.madeup.repository.DTO;
import com.redhat.cloudnative.token.madeup.repository.DefaultRepository;

import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class DBSeeder {
    
    private static final Logger Log = Logger.getLogger(DBSeeder.class);
    @Inject
    DefaultRepository repository;
    public void seed()  {
        DTO dto = createDTO();
        //repository.deleteDTO(dto);
        repository.storeDTO(dto);
    }

    private DTO createDTO()    {

        Map<String, String> headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-key", "PARAM:someApiKey");

        Map<String, Map<String, String>> requestData = new HashMap();
        requestData.put("headers", headers);

        Map<String, String> tokenMetaData = new HashMap();
        tokenMetaData.put("exp", "0");
        tokenMetaData.put("oExp", "0");

        DTO dto =  null;
        try {
            dto = DTO.builder()
            .id("testID")
            .clientId("testcLIENTID")
            .aud("Management Service")
            .secret("PARAM:someActorSecret")
            .endpoint("https://TEST/oauth/token")
            .requestData(requestData)
            .tokenMetaData(tokenMetaData)
            .expMargin(0)
            .build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Log.info("Seeded DTO : "+dto);
        return dto;
    }

}
