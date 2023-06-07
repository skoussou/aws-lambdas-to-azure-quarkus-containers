# Using Github Packages in Maven

## Create a Personal Access Token so that your workstation can authenticate with Github packages.

Visit the Github documentation on Person Access tokens, and follow it. It can be found here

   * https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token


## Configure Maven on you workstation so that it can deploy to Github packages

Base Document for this is here :

   * https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry


## Example

On you work station you should have a settings.xml in $HOME_DIR/.m2/settings.xml

It is necessary to add a new github profile to your settings.xml or replace entirely with one like this :

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>redhat-ga-repository</activeProfile>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
        </repository>
        <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/cariad-cloud/maven</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
    <!-- Configure the Red Hat GA Maven repository -->
    <profile>
      <id>redhat-ga-repository</id>
      <repositories>
        <repository>
          <id>redhat-ga-repository</id>
          <url>https://maven.repository.redhat.com/ga</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>redhat-ga-repository</id>
          <url>https://maven.repository.redhat.com/ga</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>    
  </profiles> 

  <servers>
    <server>
      <id>github</id>
      <username><your github username></username>
      <password><your github personal access token></password>
    </server>
  </servers>

</settings>
```

If you already have a settings.xml add the <profile> and <server> to it and set <activeProfile> to github

Next Configure you maven/Java project to use Github Packages, add the following to you pom.xml, beneath the <version> entry :

```
  <distributionManagement>
		<repository>
			<id>github</id>
			<name>Github</name>
			<url>https://maven.pkg.github.com/cariad-cloud/maven</url>
		</repository>
	</distributionManagement>
```


As a result the Top of your pom.xml should look like this :

```
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.wirelesscar.vw.residency</groupId>
  <artifactId>hello-cosmos</artifactId>
  <version>1.0.1-SNAPSHOT</version>


  <distributionManagement>
    <repository>
      <id>github</id>
      <name>Github</name>
      <url>https://maven.pkg.github.com/cariad-cloud/maven</url>
    </repository>
  </distributionManagement>

  <properties>
    <compiler-plugin.version>3.8.1</compiler-plugin.version>
    <failsafe.useModulePath>false</failsafe.useModulePath>
    .........
```   

## Adding jar file dependencies that we do not have the source code for

```
mvn deploy:deploy-file -Dfile=./[JAR].jar 
    -DpomFile=./pom.xml 
    -DrepositoryId=github 
    -Durl=https://maven.pkg.github.com/cariad-cloud/maven
    -Dtoken=GH_TOKEN
```    
