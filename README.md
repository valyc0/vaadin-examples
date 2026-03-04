# My App

This app was created with Bootify.io - tips on working with the code [can be found here](https://bootify.io/next-steps/).

## Development

Update your local database connection in `application.yml` or create your own `application-local.yml` file to override settings for development.

During development it is recommended to use the profile `local`. In IntelliJ `-Dspring.profiles.active=local` can be added in the VM options of the Run Configuration after enabling this property in "Modify options".

After starting the application it is accessible under `localhost:8080`.

## Build per produzione

Il progetto include un profilo Maven `production` che:
- Abilita `vaadin.productionMode=true`
- Esclude il tooling di sviluppo Vaadin
- Compila e ottimizza il bundle frontend

### Tramite script (consigliato)

```bash
# Build JAR di produzione
./build-prod.sh

# Avvio applicazione da JAR
./start-prod.sh

# Build immagine Docker + push su registry locale
./build-docker.sh
```

### Manuale

```bash
# Build JAR
mvn clean package -Pproduction -DskipTests

# Avvio JAR
java -jar target/my-app-0.0.1-SNAPSHOT.jar
```

## Docker

L'immagine Docker viene costruita tramite **Jib** (Google) direttamente da Maven, senza Dockerfile.

```bash
# Build immagine locale
mvn jib:dockerBuild -Pproduction -DskipTests

# Push manuale su registry locale (porta 5000)
docker tag my-app:latest localhost:5000/my-app:latest
docker push localhost:5000/my-app:latest

# Avvio container
docker run -p 8080:8080 localhost:5000/my-app:latest
```

Le immagini prodotte sono:
- `my-app:latest`
- `my-app:0.0.1-SNAPSHOT`
- `localhost:5000/my-app:latest` *(registry locale Kind)*
- `localhost:5000/my-app:0.0.1-SNAPSHOT` *(registry locale Kind)*

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)  
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)  
* [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin)
