package com.redhat.cloudnative.token.madeup.repository;

import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class DefaultRepository implements Repository {

  private static final Logger Log = Logger.getLogger(DefaultRepository.class);

  @Inject
  CosmosConnection connection;

  CosmosContainer table;

  public DefaultRepository() {
    Log.info("Constructor ");
  }

  @PostConstruct
  public void initialise() {
    Log.info("PostConstruct ");
    this.table = connection.getContainer();
  }

  @Override
  public void storeDTO(DTO dto) {
    table.createItem(dto);
  }

  @Override
  public void updateDTO(DTO dto) {
    table.upsertItem(dto);
  }

  @Override
  public DTO getDTO(DTO dto) {
    CosmosItemResponse<DTO> response = table.readItem(dto.getId(), new PartitionKey(dto.getClientId()), DTO.class);
    return response.getItem();
  }

  @Override
  public DTO deleteDTO(DTO dto) {
    table.deleteItem(dto, null);
    return dto;
  }

  @Override
  public Iterable<DTO> listDTOs() {
    return table.queryItems("select * from c", null, DTO.class);
  }
}
