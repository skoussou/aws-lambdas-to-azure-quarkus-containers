# Guidelines for updating and using the residency BOM

A BOM (Build of Material) has been created and will be maintained for the external maven dependencies of the residency included Java projects.

## Using the BOM

The residency BOM `com.wirelesscar:wirelesscar-residency-bom:1.0.0` has been uploaded to [github packages](https://github.com/cariad-cloud/maven/packages/1520201) is available has been uploaded) 

```XML
<dependency>
  <groupId>com.wirelesscar</groupId>
  <artifactId>wirelesscar-residency-bom</artifactId>
  <version>1.0.0</version>
</dependency>
```
In order to use it
a) add it to the `pom.xml` of your newly created repository removing any specific library versions from your `pom.xml` for the provided (from the BOM) maven dependencies 
b) Use this [`settings.xml`](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/using-github-packages-with-maven.md) to build your code.


## Updating the BOM

A github project has been created in repository [`residency-bom`](https://github.com/cariad-cloud/residency-bom) to maintain the BOM and a pipeline delivers it into github packages. In the case of an update of content increase the BOM version and inform the teams of the new version release. 