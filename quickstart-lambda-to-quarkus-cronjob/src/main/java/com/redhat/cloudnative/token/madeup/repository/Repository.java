package com.redhat.cloudnative.token.madeup.repository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface Repository {

  void storeDTO(DTO dto);

  DTO getDTO(DTO dto);

  DTO deleteDTO(DTO dto);

  Iterable<DTO> listDTOs();

  void updateDTO(DTO dto);
}
