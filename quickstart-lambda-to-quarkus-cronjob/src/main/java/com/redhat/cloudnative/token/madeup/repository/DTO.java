package com.redhat.cloudnative.token.madeup.repository;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DTO {

  private String id;

  private String clientId;
  private String aud;
  private String tenant;

  private String endpoint;
  private String token;

  private String secret;
  private String privateKey;
  private String publicCert;

  private List<String> scopes;

  private Map<String, Map<String, String>> requestData;
  private Map<String, String> tokenMetaData;

  private double expMargin;

  //@DynamoDbPartitionKey
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  //@DynamoDbSortKey
  public String getAud() {
    return aud;
  }

  public void setAud(String aud) {
    this.aud = aud;
  }

  //@DynamoDbSecondaryPartitionKey(indexNames = {"GSI-SOME"})
  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public double getExpMargin() {
    return expMargin;
  }

  public void setExpMargin(double expMargin) {
    this.expMargin = expMargin;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Map<String, Map<String, String>> getRequestData() {
    return requestData;
  }

  public void setRequestData(Map<String, Map<String, String>> requestData) {
    this.requestData = requestData;
  }

  public Map<String, String> getTokenMetaData() {
    return tokenMetaData;
  }

  public void setTokenMetaData(
      Map<String, String> tokenMetaData) {
    this.tokenMetaData = tokenMetaData;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPublicCert() {
    return publicCert;
  }

  public void setPublicCert(String publicCert) {
    this.publicCert = publicCert;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }  

  @Override
  public String toString() {
    return "DTO{" +
        "id='" + id + '\'' +
        "clientId='" + clientId + '\'' +
        ", aud='" + aud + '\'' +
        ", tenant='" + tenant + '\'' +
        ", endpoint='" + endpoint + '\'' +
        ", token='" + token + '\'' +
        ", secret='" + secret + '\'' +
        ", privateKey='" + privateKey + '\'' +
        ", publicCert='" + publicCert + '\'' +
        ", scopes=" + scopes +
        ", requestData=" + requestData +
        ", tokenMetaData=" + tokenMetaData +
        ", expMargin=" + expMargin +
        '}';
  }
}
