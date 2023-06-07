# Storing Secrets Securely in Git (for Practicing GitOps End to End)

## Context and Problem Statement

GitOps is a practice where we store configration files declaratively in Git and use that Git repositor as source of truth. Configuration files like Kubernetes objects such as Deployment, Services, Routes etc. are easy to store, but Kubernetes Secrets (which holds sensitive information such as passwords) are only leveraging base64 encoding and they are easy to decode. Hence we cannot check secrets file into Git as-is and we need to find a way to store them securely. 


## Considered Options

* HashiCorp Vault
* Sealed Secrets 
* External Secrets

SealedSecret is a way to seal (encrypt) secrets locally and store the encrypted secret definition in git repository. However it doesn't utilize any Secret Store Solution so that when something happen to SealedSecret, you loose the access to your secrets. (in a nutshell)

HashiCorp Vault is an enterprise secret management solution and can use Azure Key Vault as a backend. It can help us to retrieve secret from Azure Key Vault and mount in a secret. However this solution comes with an operational overhead and it needs HashiCorp Vault's Enterprise license.

[External Secrets](https://external-secrets.io/v0.5.6/provider-azure-key-vault/) can retrieve secrets from Azure Key Vault and store them as a secret in OpenShift namespace. It also updates the secret in OpenShift when the secret value changes in Azure Key Vault.
The way we retrieve the secret is a definition file of pointing the Key Vault and secret in Azure Key Vault in a YAML file and External Secrets itself handiles the file and reads the file and create the secret in a namespace. It requires a good RBAC (Role Based Access Control) for the namespaces so that only necessary users can access the secrets. In comparison with HashiCorp Vault it is simple and easy to use. 

## Decision Outcome
The decision was to go with External Secrets. This is because it will be simple to manage related resource through GitOps without exposing the secrets to the git history - and that the secrets themselves can be sufficiently protected by Azure Key Vault. The backend secret storage solution can also be exchanged since external secrets supports multiple providers.
