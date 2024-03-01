# Beanek személyre szabása

--

## Bean létrehozás sorrendje

* Konténer indítás két fázisban:
  * Konfiguráció (metaadatok) betöltése
  * Konkrét bean létrehozás
* Először felderíti a függőségeket, és annak megfelelően

---

## Sorrend testreszabása

* `@DependsOn` annotációval

---

## Lazy beanek

* Alapesetben eager bean létrehozás: konténer induláskor létrehozza
* Konfigurálható a `@Lazy` annotáció használatával
* Osztályra stereotype esetén
* A `@Bean` annotációval ellátott metódusra

---

## Körkörös injectálás

* Két bean egymásra hivatkozik
* Kerüljük
* Két konstruktor injection esetén hibaüzenet

---

## Bean scope

* singleton (alapértelmezett): egy az application contextben
* prototype: annyiszor létrehozott, ahányszor használt
* request: http kérésenként egy
* session: http sessionönként egy
* global-session: portlet környezetben global sessionönként egy
* `@Scope` annotációval

---

## Bean életciklus

* Példányosítás
* Injection
* Init metódusok
* Használatra kész
* Destroy metódusok
* Megszűnt

---

## Init és destroy metódusok

* `@Bean` annotáció paramétereként
* Init metódus `@Bean` annotációval ellátott metódusban meghívható
* `@PostConstruct` annotációval ellátott metódusok
* `@PreDestroy` annotációval ellátott metódusok

---

# Eseménykezelés

---

## Eseménykezelés

* Laza kapcsolat a komponensek között
* Küldő és fogadó nem tud egymástól
* Több fogadó is lehet
* Egy fogadó több eseményre is feliratkozhat

---

## Mechanizmus

* `ApplicationEventPublisher` injektálható (application context),
  majd a `publishEvent()` metódust kell meghívni
* Eseményt fogadónak az `ApplicationListener` interfészt kell implementálni
* Szinkron hívja, akár többet is

---

## Esemény

```java
@Value
public class EmployeeHasCreatedEvent {

    private String name;

}
```

---

## Esemény küldése

```java
private ApplicationEventPublisher publisher;

public void saveEmployee(String name) {
    // ...
    EmployeeHasCreatedEvent event = new EmployeeHasCreatedEvent(name);
    publisher.publishEvent(event);
}
```

---

## Esemény fogadása

```java
@Component
public class Listener {

    @EventListener
    public void handleEvent(EmployeeHasCreatedEvent event) {
        System.out.println("Employee has been created: %s".formatted(event.getName()));
    }
}
```

---

## Standard események

* `ContextRefreshedEvent`:  `ApplicationContext` indulásakor vagy frissítésekor
* `ContextStartedEvent`: `ApplicationContext` indulásakor
* `ContextStoppedEvent`: `ApplicationContext` leállásakor
* `ContextClosedEvent`: `ApplicationContext` lezárásakor
* `RequestHandledEvent`: HTTP kérésenként

---

# Profile

---

## Profile

* Különböző környezetben különböző beanek példányosodnak
* Névvel azonosított
* Egyszerre több profile is aktiválható
* Létezik egy `default` profile

---

## Beanek definiálása

```java
@Configuration
public class AppConfig {

    @Bean
    @Profile("normal")
    public EmployeeDao normalEmployeeDao() {
        return new SimpleEmployeeDao();
    }

    @Bean
    @Profile("postfix")
    public EmployeeDao postfixEmployeeDao() {
        return new PostfixEmployeeDao();
    }
}
```

---

## Profile aktiválása

* Több profile esetén vesszővel elválasztva
* `spring.profiles.active` property-vel

---

# Conditional beans

---

## Conditional beans

* Egy beant akkor akarunk konfigurálni, ha valamilyen feltétetel teljesül, pl. valamilyen környezetben,   valamilyen környezeti változó esetén, vagy valami van a classpath-on
* A profile is ezen mechanizmuson alapul (`ProfileCondition`), a `@Profile` annotáció ezzel van annotálva

---

## Conditional beans példa

```java
@Bean
@Conditional(TomcatCondition.class)
public ContainerBean containerBean() {
    return new TomcatBean();
}

@Bean
@Conditional(WebsphereCondition.class)
public ContainerBean containerBean() {
    return new WebSphereBean();
}
```

---

## Condition interfész

```java
public interface Condition {
    boolean matches(ConditionContext ctxt,
        AnnotatedTypeMetadata metadata);
}
```

---

## Condition interfész implementációk

```java
public class TomcatCondition implements Condition {
    public boolean matches(
            ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        return env.containsProperty("catalina.home");
    }
}
```
```java
public class WebSphereCondition implements Condition {
    public boolean matches(
            ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getClassLoader().getClass()
            .getCanonicalName().startsWith("com.ibm.w");
    }
}
```

---

# Aspektusorientált programozás

---

## Aspektusorientált programozás

* Az alkalmazás több pontján megjelenő funkcionalitás (un. cross-cutting concern)
* Tipikus példák: biztonság, tranzakciókezelés, naplózás

---

## AOP terminológia

* Advice: maga a funkcionalitás, amit el kell végezni
* Join point: az alkalmazás azon pontjai, ahol az advice-t be lehet illeszteni
* Pointcut: azon kiválasztott join pointok, ahol az adott advice-t le kell futtatni
* Aspect: advice és pointcut összessége

---

## Proxy

* Objektum, melyet a Spring létrehoz, miután az eredeti objektumra alkalmazta az aspektust
* Folyamat, mely során létrejön a proxy: weaving
* Spring esetén futásidejű
* Java SE dynamic proxy, ha a Spring bean interfészt implementál
* CGLIB, ha nem implementál interfészt - leszármaztatás (nem lehet `final`)

---

## AspectJ

* Aspektusorientált programozást lehetővé tevő keretrendszer
* Spring felhasznál belőle bizonyos részeket
* A Spring AOP pehelysúlyúbb megoldás

---

## Advice

* Csak metódus
* Before: metódus előtt
* After: metódus után, visszatéréstől függetlenül
* After-returning: sikeres visszatérés esetén
* After-throwing: kivétel esetén
* Around - metódust beburkolja

---

## Join point

* AspectJ kifejezésekkel
* Saját leíró nyelvvel

```java
execution(* spring.di.EmployeeService.saveEmployee(..))
```

---

## AOP függőség

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.8.10</version>
</dependency>
```

---

## Konfiguráció annotációkkal

* `@Aspect` - aspektus definiálása
* `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`

---

## AOP példa

```java
@Aspect
public class CounterAspect {

	private AtomicInteger count = new AtomicInteger();

	@Before("execution(* training.employeesdemo.EmployeesService.employees(..))")
	public void inc() {
		count.incrementAndGet();
	}

	public int getCount() {
		return count.get();
	}
}
```

* Legyen Spring bean (pl. `@Component`)
* Java konfig esetén `@EnableAspectJAutoProxy` annotáció a `@Configuration` annotációval ellátott osztályon

---

## Pointcut újrafelhasználása

```java
@Aspect
public class CounterAspect {

	private AtomicInteger count = new AtomicInteger();

	@Pointcut("execution(* training.employeesdemo.EmployeesService.employees(..))")
	public void employees() {
	}

	@Before("employees()")
	public void inc() {
		count.incrementAndGet();
	}

	public int getCount() {
		return count.get();
	}
}
```

---

## Around aspect

```java
@Around("employees()")
public Object logMethodCall(ProceedingJoinPoint joinpoint) {
    try {
        logger.info("The method " + joinpoint.getSignature().getName() + "() begins");

        Object result = joinpoint.proceed();

        log.info("The method " + method.getName() + "() ends with " + result);
        return result;
    } catch (Throwable t) {
      log.info("The method " + method.getName() + "() ends with exception");
        throw t;
    }
}
```

---

## Around aspect tulajdonságai

* Hívás megakadályozható
* Paraméterek módosíthatóak (`ProceedingJoinPoint.getArgs()`, `ProceedingJoinPoint.proceed(Object[] objects)`)
* Visszatérési érték módosítható

---

## Aspektusok sorrendje

* Az `@Order` annotációval megadható, előbb az alacsonyabb értékű

---

# Spring Boot naplózás

---

## Naplózás

* Spring belül a Commons Loggingot használja
* Előre be van konfigurálva az SLF4J, Logback
    * `jul-to-slf4j`, `log4j-to-slf4j`
* Alapesetben konzolra ír
* Naplózás szintje, és fájlba írás is állítható <br />az `application.properties` állományban

---

## Best practice

* SLF4J használata
* Lombok használata
* Paraméterezett üzenet

```java
private static final org.slf4j.Logger log =
  org.slf4j.LoggerFactory.getLogger(LogExample.class);
```

```java
@Slf4j
```

```java
log.info("Employee has been created");
log.debug("Employee has been created with name {}",
  employee.getName());
```

---

## Konfiguráció

* `application.properties`: szint, fájl
* Használható logger library specifikus konfigurációs fájl (pl. `logback.xml`)

```properties
logging.level.training = debug
```

---

### Slf4j2

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-spring-boot</artifactId>
</dependency>
```

---

# Validáció (Bean Validation), RFC 7807 vonatkozása

---

# Unit és integrációs tesztelés (MockMVC, WebTestClient, TestRestTemplate)

`employees-integrationtests` projekt

---

# Mapping frameworks, összehasonlításuk

https://www.baeldung.com/java-performance-mapping-frameworks

---

# SwaggerUI, OpenAPI

---

# Generálás OpenAPI alapján

`employees-openapi-generator` project


---

# Perzisztens keretrendszerek és összehasonlításuk

---

https://www.jtechlog.hu/2022/10/06/mybatis.html

---

# JdbcTemplate

# Spring Data JPA

# Séma inicializálás Flyway-jel

---

## Séma inicializálás

* Adatbázis séma létrehozása (táblák, stb.)
* Változások megadása
* Metadata table alapján  

---

## Elvárások

* SQL/XML leírás
* Platform függetlenség
* Lightweight
* Visszaállás korábbi verzióra
* Indítás paranccssorból, alkalmazásból
* Cluster támogatás
* Placeholder támogatás
* Modularizáció
* Több séma támogatása

---

## Flyway függőség

```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

Hibernate séma inicializálás kikapcsolás az
`application.properties` állományban:

```properties
spring.jpa.hibernate.ddl-auto=none
```

---

## Migration PostgreSQL esetén

`src/resources/db/migration/V1__employees.sql` állomány

```sql
create table employees (id int8 generated by default as identity, 
  emp_name varchar(255), primary key (id));

insert into employees (emp_name) values ('John Doe');
insert into employees (emp_name) values ('Jack Doe');
```

`flyway_schema_history` tábla

---

# Deklaratív tranzakciókezelés

---

## Tranzakciókezelés

![Tranzakciókezelés](images/tranzakcio-kezeles.png)

---

## Propagáció

![Propagáció](images/propagacio.png)

---

## Propagációs tulajdonságok

* `REQUIRED` (default): ha nincs tranzakció, indít egyet, ha van csatlakozik hozzá
* `REQUIRES_NEW`: mindenképp új tranzakciót indít
* `SUPPORTS`: ha van tranzakció, abban fut, ha nincs, nem indít újat
* `MANDATORY`: ha van tranzakció, abban fut, ha nincs, kivételt dob
* `NOT_SUPPORTED`: ha van tranzakció, a tranzakciót felfüggeszti, ha nincs, nem indít újat
* `NEVER`: ha van tranzakció, kivételt dob, ha nincs, <br /> nem indít újat

---

## Izoláció

* Izolációs problémák:
    * dirty read
    * non-repetable read
    * phantom read
* Izolációs szintek:
    * read uncommitted
    * read commited 
    * repeatable read
    * serializable

---

## Visszagörgetési szabályok

* Kivételekre lehet megadni, hogy melyik esetén történjen rollback
* Rollbackre explicit módon megjelölni
* Konténer dönt a commitról vagy rollbackről

---

## Timeout

* Timeout esetén kivétel

---

## Csak olvasható

* Spring esetén további optimalizációkat tud elvégezni, cache-eléssel kapcsolatos


---

# Integrációs tesztelés

---

## JPA repository tesztelése

* JPA repository-k tesztelésére
* `@DataJpaTest` annotáció, csak a repository réteget indítja el
    * Embedded adatbázis
    * Tesztbe injektálható: JPA repository,  `DataSource`, `JdbcTemplate`, <br /> `EntityManager`
* Minden teszt metódus saját tranzakcióban, végén rollback
* Service réteg már nem kerül elindításra
* Tesztelni:
    * Entitáson lévő annotációkat
    * Névkonvenció alapján generált metódusokat
    * Custom query

---

## DataJpaTest

```java
@DataJpaTest
public class EmployeesRepositoryIT {

  @Autowired
  EmployeesRepository employeesRepository;

  @Test
  void testPersist() {
    Employee employee = new Employee("John Doe");
    employeesRepository.save(employee);
    List<Employee> employees =
      employeesRepository.findAllByPrefix("%");
    assertThat(employees)
      .extracting(Employee::getName)
      .containsExactly("John Doe");
  }

}
```

---

## @SpringBootTest használata

* Teljes alkalmazás tesztelése
* Valós adatbázis szükséges hozzá, gondoskodni kell az elindításáról
* Séma inicializáció és adatfeltöltés szükséges

---

## Tesztek H2 adatbázisban

* `src\test\resources\application.properties` fájlban <br /> a teszteléshez használt DataSource

```properties
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=sa
```

---

## Séma inicializáció

* `spring.jpa.hibernate.ddl-auto` `create-drop` alapesetben, <br /> teszt lefutása végén eldobja a sémát
    * `create`-re állítva megmaradnak a táblák és adatok
* Ha van `schema.sql` a classpath-on, azt futtatja le
* Flyway vagy Liquibase használata

---

## Adatfeltöltés

* `data.sql` a classpath-on
* `@Sql` annotáció használata a teszten
* Programozott módon
    * Teszt osztályban `@BeforeEach` vagy `@AfterEach` <br /> annotációkkal megjelölt metódusokban
    * Publikus API-n keresztül
    * Injektált controller, service, repository, stb. használatával
    * Közvetlen hozzáférés az adatbázishoz <br /> (pl. `JdbcTemplate`)

---

## Tesztek egymásra hatása

* Csak külön adatokon dolgozunk - nehéz lehet a kivitelezése
* Teszteset maga előtt vagy után rendet tesz
* Állapot
    * Teljes séma törlése, séma inicializáció
    * Adatbázis import
    * Csak (bizonyos) táblák ürítése

---

### Docker compose

Függőség

`compose.yaml`

---

### Testcontainers

Függőségek

* `EmployeesApplicationIT`
* `EmployeesTestApplication`

---

# Kubernetes

---

## Kubernetes

* Container orchestration (bármilyen CRI implementáció, Docker, vagy akár containerd)
* Több fizikai számítógép (resource management)
* Konténerek szétosztása, életciklus (scheduling, affinity, anti-affinity)
* Deklaratív megközelítés
* Újraindítás alkalmazás/konténer hiba esetén (service management)
* Google -> CNCF
* (Google) Go-ban implementált
* [CNCF Cloud Native Interactive Landscape](https://landscape.cncf.io/)
* Különböző disztribúciók
  * On-premises és cloud megoldások is
  * Különböző gyártók
  * Különböző igények (pl. tesztelés, limitált erőforrások)

---

## Részei

* Master node (Control plane)
  * API server (REST, authentication, állapotmentes)
  * etcd (key-value adatbázis, konfiguráció és aktuális állapot)
  * Scheduler (dönt, hogy mely konténer hol indítható)
  * Controller manager (elvárt állapot fenntartásért felelős, monitoring loopok, update-eli az etcd-ben az állapotot)
  * Cloud controller manager (opcionális)
* Worker node
  * kubelet (konténerek vezérlése)
  * kube-proxy (hálózat)

---

## Hálózat

* Cluster DNS (based on CoreDNS)

---

## Magas rendelkezésreállás

* Legalább három master node (elég csak az etcd-ből)
* Legalább három worker node

---

## Windowson

* Docker Desktop
* Single node cluster

---

## Adminisztráció

* `kubectl` parancs
  * https://kubernetes.io/docs/reference/kubectl/cheatsheet/
  * Sokszor manifest fájlokat használ, melyek yaml fájlok
* GUI - [Lens](https://k8slens.dev/)
* REST API, különböző programozási nyelven megírt kliensek

---

## Objects

* Pod
  * Bálnaraj
  * Tipikusan egy konténer
  * 0-n ún. sidecar konténer
  * Konténerek egy ip-t kapnak (vigyázni a portütközéssel), kommunikálhatnak egymással localhoston vagy socketen
  * Ideiglenes ip, hiszen bármikor történhet újraindítás
  * Halandóak, ha leáll, hibára fut, Kubernetes törli és újat indít
  * Atomic unit
* Deployment
  * Inkább ezeket használjuk, bár podot is lehet
  * Deployment része a pod definíció
  * Skálázható
  * Rolling update

---

## Deployment strategy

* Recreate - először podok törlése, majd létrehozása
* Rolling update - Párhuzamosan, a leállási idő csökkentése miatt

---

## Service

* Egy stabil végpontot, DNS nevet, ip-címet és portot kínál
* Load-balance-ol a pod-ok között
* Típusai:
  * ClusterIP - clusteren belül, nincs külső hozzáférés
  * NodePort - saját LB kell (30_000-től felfelé), automatikusan CluserIP-t is csinál
  * LoadBalancer - ráül a clusteren kívül LB-re (saját infra, vagy cloud provider), kevésbé konfigolható
  * ExternalName - DNS CNAME alapján

---

## ConfigMap, Secret

---

## Persistent Volume

---

## StatefulSet

* Stateful alkalmazások esetén
* Hasonló, mint a Deployment, de biztosítja a Pod-ok egyediségét és sorrendiségét
* Akkor használjuk, ha szükség van valamelyikre:
  * Fix hálózati azonosító
  * Stable, persistent storage
  * Sorrendezett telepítés és skálázás
  * Sorrendezett frissítés

---

## Job, CronJob

---

## Helm

---

# OAuth 2.0 és OIDC használata

---

# KeyCloak indítása és konfigurálása

```shell
docker run -d -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8090:8080 --name keycloak jboss/keycloak
```

* `http://localhost:8090` címen elérhető, `admin` / `admin`
* Létre kell hozni egy Realm-et (`EmployeesRealm`)
* Létre kell hozni egy klienst, amihez meg kell adni annak azonosítóját, <br /> és hogy milyen url-en érhető el (`employees-frontend`)
    * Ellenőrizni a _Valid Redirect URIs_ értékét
* Létre kell hozni a szerepköröket (`employees_user`)
* Létre kell hozni egy felhasználót (a _Email Verified_ legyen _On_ értéken, hogy be lehessen vele jelentkezni), beállítani a jelszavát (a _Temporary_ értéke legyen _Off_, hogy ne kelljen jelszót módosítani), <br /> valamint hozzáadni a szerepkört a _Role Mappings_ fülön (`johndoe`)

## KeyCloak URL-ek

* Konfiguráció leírása

```
http://localhost:8090/auth/realms/EmployeesRealm/.well-known/openid-configuration
```

* Tanúsítványok

```
http://localhost:8090/auth/realms/EmployeesRealm/protocol/openid-connect/certs
```

* Token lekérése Resource owner password credentials használatával

```shell
curl -s --data "grant_type=password&client_id=employees-frontend&username=johndoe&password=johndoe" http://localhost:8090/auth/realms/EmployeesRealm/protocol/openid-connect/token | jq
```

```http
POST http://localhost:8090/auth/realms/EmployeesRealm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=employees-frontend&username=johndoe&password=johndoe
```

* A https://jws.io címen ellenőrizhető

## Frontend mint Client

* Függőség:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

```java
package employees;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry -> registry
                                .requestMatchers( "/create-employee")
                .authenticated()
//                                .hasRole("employee_admin")
                                .anyRequest()
                                .permitAll()
                        )
                .oauth2Login(Customizer.withDefaults())
                .logout(conf -> conf.
                                logoutSuccessUrl("/")
                        );
        return http.build();
    }

}
```

`application.yaml`


```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: employees-frontend
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/
            scope: openid,email,profile
        provider:
          keycloak:
            issuer-uri: http://localhost:8090/auth/realms/EmployeesRealm
```

* `EmployeesController`

```java
@GetMapping("/")
public ModelAndView listEmployees(Principal principal) {
    log.debug("Principal: {}", principal);
```

`OAuth2AuthenticationToken`

* Frontend újraindítás után is bejelentkezve marad

* Logout: `http://localhost:8090/auth/realms/EmployeesRealm/protocol/openid-connect/logout?redirect_uri=http://localhost:8080`
* Account Management: `http://localhost:8090/auth/realms/EmployeesRealm/account`

## Alternatív felhasználónév használata

`application.yaml`

```yaml
spring:
  security:
    oauth2:
        provider:
          keycloak:
            user-name-attribute: preferred_username
```

## Szerepkörök átvétele

`principal` / `principal` / `idtoken`

* Client Scopes/roles/Mappers/realm roles/Add to ID token
    * A szerepkörök csak ekkor lesznek benne az id tokenbe

* `SecurityConfig`

```java
@Bean
public GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return (authorities) -> authorities.stream().flatMap(authority -> {
        if (authority instanceof OidcUserAuthority oidcUserAuthority) {
            var realmAccess = (Map<String, Object>) oidcUserAuthority.getAttributes().get("realm_access");
            var roles = (List<String>)realmAccess.get("roles");


//                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
//                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

            // Map the claims found in idToken and/or userInfo
            // to one or more GrantedAuthority's and add it to mappedAuthorities
            return roles.stream()
                    .map(roleName -> "ROLE_" + roleName)
                    .map(SimpleGrantedAuthority::new);


        } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
            Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

            // Map the attributes found in userAttributes
            // to one or more GrantedAuthority's and add it to mappedAuthorities
            return Stream.of();
        }
        else if (authority instanceof SimpleGrantedAuthority simpleGrantedAuthority) {
            return Stream.of(simpleGrantedAuthority);
        }
        else {
            throw new IllegalStateException("Invalid authority: %s".formatted(authority.getClass().getName()));
        }
    }).toList();
}
```

# Access token továbbítása a backend felé

* `SecurityConfig`

```java
@Bean
public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {

    OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                    .authorizationCode()
                    .refreshToken()
                    .clientCredentials()
                    .build();

    DefaultOAuth2AuthorizedClientManager authorizedClientManager =
            new DefaultOAuth2AuthorizedClientManager(
                    clientRegistrationRepository, authorizedClientRepository);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    return authorizedClientManager;
}
```

```java
@Configuration(proxyBeanMethods = false)
public class ClientConfig {
    @Bean
    public EmployeesClient employeesClient(WebClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager) {
        var oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultOAuth2AuthorizedClient(true);

        var webClient = builder
                .baseUrl("http://localhost:8081")
                .apply(oauth2.oauth2Configuration())
                .build();
        var factory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient)).build();
        return factory.createClient(EmployeesClient.class);
    }
}
```

* Backend:

```java
@GetMapping
public List<EmployeeResource> listEmployees(@RequestHeader HttpHeaders headers) {
    log.debug("Headers: {}", headers);
    return employeesService.listEmployees();
}
```

```plain
Headers: [accept-encoding:"gzip", user-agent:"ReactorNetty/1.1.12", host:"localhost:8081", accept:"*/*", authorization:"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItcHJuVjJOWFk5ZjBlYnR4VDRySzdQRHo3X0NoMjc0WkhjbHVwejV6dDFZIn0.eyJleHAiOjE3MDE3MDMyMjMsImlhdCI6MTcwMTcwMjkyMywiYXV0aF90aW1lIjoxNzAxNzAxOTIxLCJqdGkiOiIyMzg1MjQzOC1hMDg0LTRjMDItODJmNi0wY2RlOGU3ODgzOTgiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvRW1wbG95ZWVzUmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNmNlNTcyNmItMDc0Mi00M2RjLWJkNDYtYjAwOWExYmFjZWI5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZW1wbG95ZWVzLWZyb250ZW5kIiwibm9uY2UiOiIyWERGeU80ZHlXVjl1THd2WHJQU2E3U09Lb1djVjZURU44cVRBM2JBZmI0Iiwic2Vzc2lvbl9zdGF0ZSI6ImI1MDY4NmViLThkZTgtNDkxYS05MGZhLWFlZGY1NjgzOTU0NiIsImFjciI6IjAiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRlZmF1bHQtcm9sZXMtZW1wbG95ZWVzcmVhbG0iLCJlbXBsb3llZXNfdXNlciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJzaWQiOiJiNTA2ODZlYi04ZGU4LTQ5MWEtOTBmYS1hZWRmNTY4Mzk1NDYiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiam9obmRvZSJ9.NmXHCLgus0vQWnUHK2LlJeHGfBT5X_jneNHjlm9PRT6qHqMF17rMiZXuVSoLewSK3oRATg_7qYH7Gcj0jzJxG8WNeJDp9tIVngd-S_KUGggssJpxHPUDVgY_clI7uQTbhPR6bz1Ye05Pf68M9XpRPkWsin9P73vdsBJ5jOCUioob-zbEkrB7uGCA68MQsSKamdyR8anNun3fqhsqaktbnJtn65uJjIfnigmUixY70T2Ic9OVrNTSIbN8UxX5Gam-92R-Qx61AFJC57HOrVzD6CV-VrFMy7TgRfJRNBS1ty7akB8Ag-bMbSkPfj_Z1Z_f_rCUcVAUfvAq24D9ZwjaVA"]
```

# Backend mint Resource Server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

```java
package employees;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(registry -> registry
                        .requestMatchers(HttpMethod.POST, "/api/employees")
                                .authenticated()
//                        .hasRole("employees_user")
                        .anyRequest()
                        .permitAll()
                )
                .oauth2ResourceServer(conf -> conf.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
```

`application.yaml`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8090/auth/realms/EmployeesRealm
```

* `http` fájlból a `POST` kérés: 

```json
{
  "timestamp": "2023-12-04T15:30:43.802+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/employees"
}
```

```java
@GetMapping
public List<EmployeeResource> listEmployees(@RequestHeader HttpHeaders headers, Principal principal) {
    log.debug("Principal: {}", principal);

```

```plain
JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@28b3d686, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=127.0.0.1, SessionId=null], Granted Authorities=[SCOPE_openid, SCOPE_profile, SCOPE_email]]
```

# Felhasználónév a backenden

```java
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;

import java.util.Collections;
import java.util.Map;

public class UsernameSubClaimAdapter implements Converter<Map<String, Object>, Map<String, Object>> {

    private final MappedJwtClaimSetConverter delegate = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

    @Override
    public Map<String, Object> convert(Map<String, Object> source) {
        Map<String, Object> convertedClaims = this.delegate.convert(source);
        String username = (String) convertedClaims.get("preferred_username");
        convertedClaims.put("sub", username);
        return convertedClaims;
    }
}
```

* `SecurityConfig`

```java
@Bean
public JwtDecoder jwtDecoderByIssuerUri(OAuth2ResourceServerProperties properties) {
    String issuerUri = properties.getJwt().getIssuerUri();
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
    // Use preferred_username from claims as authentication name, instead of UUID subject
    jwtDecoder.setClaimSetConverter(new UsernameSubClaimAdapter());
    return jwtDecoder;
}
```

# Szerepkörök a backenden

```java
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        var realmAccess = (Map<String, Object>) source.getClaims().get("realm_access");
        var roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```

* `SecurityConfig`

```java
@Bean
public Converter<Jwt,? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    // Convert realm_access.roles claims to granted authorities, for use in access decisions
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
    return converter;
}
```

---

# Kapcsolódás más rendszerhez WebClient/RestClient használatával, mockolás WireMockkal

`employees-sb3-client-demo` project

# JMS üzenet küldése és fogadása (ActiveMQ Artemis, RabbitMQ)

## ActiveMQ Artemis

```shell
docker run --detach --name employees-artemis -p 61616:61616 -p 8161:8161 --rm apache/activemq-artemis:latest-alpine
```

http://localhost:8161/

`artemis` / `artemis`

## RabbitMQ

https://www.jtechlog.hu/2020/09/11/rabbitmq.html

# Cache abstraction

---

## Cache abstraction

* Objektumok memóriában megőrzése
* Nem kell újra lekérni, pl. adatbázisból, hálózatról
* Deklaratív módon
    * Spring saját megoldása
    * JSR-107 szabvány (JCache)
* Különböző implementációk konfigurálhatóak be
* Metódushívás eredménye cache-elhető a paraméterek függvényében

---

## Cache-elés beállítása

```java
@Configuration
@EnableCaching
public class CacheConfig {

}
```

Alapesetben egy `ConcurrentMap` alapú implementáció

---

## Cache-elés metódus szinten

```java
@Cacheable("employee")
public EmployeeDto findEmployeeById(long id) {
  // ...
}
```

* Kulcs: `Long`, érték: `EmployeeDto`

---

## Cache-elés személyre szabása

* `key`: Spring EL-el megadható mi legyen a kulcs
* `keyGenerator`: kulcsgenerálás programozott módon `org.springframework.cache.interceptor.KeyGenerator` interfész implementációval
* `condition`: csak bizonyos feltétel mellett kerüljön be (Spring EL, pl `#page < 5`)
* `unless`: amikor a visszatérési érték nem cache-elhető (Spring EL)
* `sync`: szinkronizált

---

## Cache törlése

```java
@CacheEvict(value = "employees", allEntries = true)
// @CacheEvict(value = "employee", key = "#id")
@CachePut(value = "employee", key = "#id")
public EmployeeDto updateEmployee(long id, UpdateEmployeeCommand command) {
  // ...
}
```

* `@CacheEvict` `allEntries` paraméterrel töröl mindent
* `@CacheEvict` csak a kulccsal megadott értéket törli
* `@CachePut` mindig megtörténik a hívás, a visszatérési értéket azonnal elhelyezi a cache-ben

Visszatérési érték is hivatkozható a `result`-tal:

```java
@CachePut(value = "employee", key = "#result.id")
public EmployeeDto createEmployee(CreateEmployeeCommand command) {
  // ...
}
```

---

## Üres és összetett kulcs

* Üres kulcs: `SimpleKey[]`
* Összetett kulcs megadása EL-ben, több paraméterre hivatkozva: `{#id, #type}`

---

## Több annotáció használata

* Nincs `@Repeatable`

```java
@Caching(evict = {
        @CacheEvict(value = "employees", allEntries = true),
        @CacheEvict(value = "employee", key = "#id")
})
```

---

## Cache műveletek naplózása

```properties
logging.level.org.springframework.cache=trace
```

---

## Programozott hozzáférés

```java
// Injektálható
private CacheManager cacheManager;

public void evictSingleCacheValue(String cacheName, String cacheKey) {
    cacheManager.getCache(cacheName).evict(cacheKey);
}
```

---

# Cache Redis használatával

---

## Redis

```shell
docker run --name employees-redis -p 6379:6379 -d redis
docker exec -it employees-redis redis-cli ping
docker exec -it employees-redis redis-cli --scan
docker exec -it employees-redis redis-cli get employee::1  
docker exec -it employees-redis redis-cli get "employees::SimpleKey []"
```

---

## Függőségek és konfiguráció

```xml
<dependency>			
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

* `Serializable` objektumok

Az `application.properties`:

```properties
spring.cache.cache-names=employees,employee
spring.cache.redis.time-to-live=10m
```

# Modularizáció: Spring Modulith

https://www.jtechlog.hu/2022/12/19/spring-modulith.html

---

# Üzenet szerver oldalról

---

## Szerver oldali üzenetküldő <br /> megoldások

* Long polling
* WebSocket
* Server-Sents Events

---

## WebSocket

* Pehelysúlyú kommunikációs protokoll a TCP/IP felett
* Full-duplex
* HTTP Upgrade header HTTP protokollról WebSocketre váltásra, ugyanazon porton
* Kapcsolatfelvételt a kliens kezdeményezi, de utána kétirányú

---

## SockJS

* WebSocket emuláció
* Ha nem működik a WebSocket, pl. régi böngésző, vagy a proxy nem támogatja
* Szükség esetén visszavált alternatív protokollra, az API a WebSocket API marad (polling)

---

## STOMP

* Simple (or Streaming) Text Oriented Message Protocol
* MOM-okhoz való kapcsolódáshoz
* Nagyon hasonló a HTTP protokollhoz
* Frame-ekből áll
* Fejléc (kulcs-érték párok), törzs
* Parancsok, pl. `CONNECT`, `SEND`, `SUBSCRIBE`, stb.

---

## STOMP architektúra

<img src="images/stomp.png" alt="STOMP" width="600" />

---

## WebSocket függőség

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

* Tomcat implementációt használja WebSocket kezelésre

---

## JavaScript függőségek

```xml
<dependency>
	<groupId>org.webjars</groupId>
	<artifactId>sockjs-client</artifactId>
	<version>1.1.2</version>
</dependency>
<dependency>
	<groupId>org.webjars</groupId>
	<artifactId>stomp-websocket</artifactId>
	<version>2.3.3-1</version>
</dependency>
```

---

## Spring konfiguráció

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-endpoint").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic");
    }

}
```

---

## Spring controller

```java
@MessageMapping("/messages")
@SendTo("/topic/employees")
public Message sendMessage(MessageCommand command) {
    return new Message("Reply: " + command.getContent());
}
```

* `@MessageMapping` helyett `@SubscribeMapping` - csak a feliratkozó üzenetekre kerül meghívásra
* Alapesetben a `/topic/messages` (bejövő destination `/topic` prefix-szel) topicra válaszol, azonban ez felülírható a `@SendTo` annotációval

---

## JavaScript kliens - feliratkozás

```javascript
const socket = new SockJS('/websocket-endpoint');
     const stompClient = Stomp.over(socket);
     stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/employees', function (message) {
            const content = JSON.parse(message.body).content;
            console.log(content);
        });
    });
```

---

## JavaScript kliens - küldés

```javascript
stompClient.send("/app/messages", {}, JSON.stringify({"content": content}));
```

---

# WebSocket üzenetküldés üzleti logikából

---

## WebSocket üzenetküldés

```java
@Controller
public class WebSocketMessageController {

    private SimpMessagingTemplate template;

    // ...

    public void send() {
      // message létrehozása

      template.convertAndSend("/topic/employees", message); // JSON marshal
    }

}
```

---

# Spring Boot WebSocket kliens

---

## Parancssoros Spring Boot kliens

```java
@SpringBootApplication
public class EmployeeStompClient implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeStompClient.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // ...
    }
}
```

---

## Függőségek

.small-code-14[
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-json</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-websocket</artifactId>
    <version>4.3.25.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-messaging</artifactId>
    <version>4.3.25.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.glassfish.tyrus.bundles</groupId>
    <artifactId>tyrus-standalone-client</artifactId>
    <version>1.15</version>
</dependency>
```
]

* [Tyrus](https://tyrus-project.github.io/) JSR 356: Java API for WebSocket - Reference Implementation

---

## WebSocket kliens

```java
//WebSocketClient client = new StandardWebSocketClient();
WebSocketClient client = new SockJsClient(
  List.of(new WebSocketTransport(new StandardWebSocketClient())));

WebSocketStompClient stompClient = new WebSocketStompClient(client);
stompClient.setMessageConverter(new MappingJackson2MessageConverter());

StompSessionHandler sessionHandler = new MessageStompSessionHandler();
stompClient.connect("ws://localhost:8080/websocket-endpoint", sessionHandler);

new Scanner(System.in).nextLine();
```

---

## StompSessionHandler

```java
public class MessageStompSessionHandler implements StompSessionHandler {

    public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
        stompSession.subscribe("/topic/employees", this);
    }

    public Type getPayloadType(StompHeaders stompHeaders) {
            return Message.class;
    }
    
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        String content = ((Message) o).getContent();
        // ...    
    }
}
```

---

## Hibakezelés

```java
public void handleException(StompSession stompSession, StompCommand stompCommand,
      StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {
    throw new IllegalStateException("Exception by websocket communication", throwable);
}

public void handleTransportError(StompSession stompSession, Throwable throwable) {
    throw new IllegalStateException("Exception by websocket transport", throwable);
}
```

---

## Üzenet küldése


```java
ListenableFuture<StompSession> future = 
  stompClient.connect("ws://localhost:8080/websocket-endpoint", sessionHandler);
StompSession session = future.get(1000, TimeUnit.SECONDS);
// A MessageCommand JSON-be marshal-olható objektum
session.send("/app/messages", new MessageCommand(line));
```

---

# Server-Send Events (SSE)

---

## Server-Send Events (SSE)

* WebSocketnél egyszerűbb kommunikáció
* Szerver által küldött üzenetek
* Nyitott HTTP kapcsolat streamelve (Mime-type: `text/event-stream`)
* Formátuma:

```plaintext
event:message
:Employee has created
id:253c3eb8-fb59-410c-82b9-d2acfa89a5d5
retry:10000
data:{"content":"Employee has created: John Doe"}
```

---

## Spring controller

```java
@GetMapping("/api/employees/messages")
public SseEmitter getMessages() {
    SseEmitter emitter = new SseEmitter();
    // Ciklusban, késleltetetten
    emitter.send("Hello World");
    return emitter;
}
```

```java
@GetMapping("/api/employees/messages")
public SseEmitter getMessages() {
    SseEmitter emitter = new SseEmitter();
    // Ciklusban, késleltetetten, külön szálon
    messagesService.createMessages(emitter);
    return emitter;
}
```

---

## Üzenet összeállítás

```java
SseEmitter.SseEventBuilder builder = SseEmitter.event()
        .name("message")
        .comment("Employee has created")
        .id(UUID.randomUUID().toString())
        .reconnectTime(10_000)
        // JSON marshal
        .data(new Message("Employee has created: " + event.getName()));
emitter.send(builder);
```

---

## Emitter elmentése

```java
private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

@GetMapping("/api/employees/messages")
public SseEmitter getMessages() {
    SseEmitter emitter = new SseEmitter();
    emitters.add(emitter);
    return emitter;
}
```

---

## Küldés az emitternek

* Megszakadt kapcsolatok kezelésével

.small-code-14[
```java
@EventListener
public void employeeHasCreated(EmployeeHasCreatedEvent event) {
    List<SseEmitter> deadEmitters = new ArrayList<>();
    this.emitters.forEach(emitter -> {
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name("message")
                    .comment("Employee has created")
                    .id(UUID.randomUUID().toString())
                    .reconnectTime(10_000)
                    .data(new Message("Employee has created: " + event.getName()));
            emitter.send(builder);
        }
        catch (Exception e) {
            deadEmitters.add(emitter);
        }
    });

    this.emitters.removeAll(deadEmitters);
}
```
]

---

## JavaScript kliens


```javascript
const eventSource = new EventSource("api/employees/messages");
eventSource.addEventListener("message",
    function(event) {
        console.log(JSON.parse(event.data).content)
    });
```

---

# Server-Send Events (SSE) Java kliens

---

## Függőség

* Spring WebClient használatával

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## Java kód

```java
WebClient webClient = WebClient.create("http://localhost:8080");

ParameterizedTypeReference<ServerSentEvent<Message>> type
        = new ParameterizedTypeReference<ServerSentEvent<Message>>() {};

Flux<ServerSentEvent<Message>> messages = webClient
        .get()
        .uri("/api/employees/messages")
        .retrieve()
        .bodyToFlux(type);

messages.subscribe(event -> System.out.println(event.data().getContent()), 
        throwable -> System.out.println(throwable.getMessage()));
```


# SOAP webszolgáltatások CXF-fel

---

## Webszolgáltatás

* W3C definíció: hálózaton keresztüli gép-gép együttműködést támogató
szoftverrendszer
* Platform független
* Szereplők
    * Szolgáltatást nyújtó
    * Szolgáltatást használni kívánó

---

## SOAP alapú webszolgáltatások

* Szabványokon alapuló
    * SOAP
    * WSDL
* Kapcsolódó szabványok
    * HTTP
    * XML, és kapcsolódó szabványok: XSD, névterek

---

## SOAP

* W3C által karbantartott szabvány

![SOAP üzenet](images/soap-message.gif)
---

## Példa SOAP kérés

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Header/>
   <soap:Body>
      <listEmployees xmlns="http://training360.com/empapp"/>
   </soap:Body>
</soap:Envelope>
```

---

## Példa SOAP válasz

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Body>
      <listEmployeesResponse xmlns="http://training360.com/empapp">
         <employee>
            <id>1</id>
            <name>John Doe</name>
         </employee>
         <employee>
            <id>2</id>
            <name>Jane Doe</name>
         </employee>
      </listEmployeesResponse>
   </soap:Body>
</soap:Envelope>
```

---

## WSDL

* Web Services Description Language
* WSDL dokumentum: több állományból

---

## WSDL felépítése

<img src="images/wsdl.gif" alt="WSDL" width="400" />

---

## JAX-WS

* Szabvány SOAP webszolgáltatások és kliensek fejlesztésére
* Épít a JAXB-re
* Támogatja a kód és WSDL alapú megközelítést is

---

## Implementációk

* JAX-WS Reference Implementation
    * https://javaee.github.io/metro-jax-ws/
* Apache Axis2
    * http://axis.apache.org/axis2/java/core/
* CXF
    * http://cxf.apache.org/

---

## CXF

* JAX-WS implementáció
* Kétirányú generálás Java és WSDL között
* Spring támogatás
* WS-Addressing, WS-Policy, WS-ReliableMessaging és WS-Security támogatás
* Különböző data binding, pl. JAXB
* MTOM attachment
* HTTP(S)/JMS transport

---

## Függőségek

```xml
<dependency>
	<groupId>org.apache.cxf</groupId>
	<artifactId>cxf-spring-boot-starter-jaxws</artifactId>
	<version>3.3.3</version>
</dependency>
```

---

## Endpoint implementáció

```java
@WebService(serviceName = "EmployeeWebService", targetNamespace = EmployeeWebService.EMPAPP_NAMESPACE)
@Service
public class EmployeeWebService {

    public static final String EMPAPP_NAMESPACE = "http://training360.com/services/empapp";

    // ...

    @WebMethod
    @XmlElementWrapper(name = "employees")
    @WebResult(name = "employee")
    public List<EmployeeWdto> listEmployees() {
        // Külön réteg
        ModelMapper modelMapper = new ModelMapper();
        return employeeService.listEmployees().stream()
                .map(e -> modelMapper.map(e, EmployeeWdto.class)).collect(Collectors.toList());
    }

```

---

## Névtér

```java
@XmlSchema(namespace = EmployeeWebService.EMPAPP_NAMESPACE,
        elementFormDefault= XmlNsForm.QUALIFIED)
package empapp;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
```

---

## CXF Spring regisztráció

```java
@Bean
public Endpoint employeeEndpoint(EmployeeWebService employeeWebService) {
    var endpoint = new EndpointImpl(bus, employeeWebService);
    endpoint.publish("/employees");
    return endpoint;
}
```

* WSDL-ben szereplő URL felülbírálása

---

## Webszolgáltatás tesztelése

* Webszolgáltatásokat leíró lap: `http://localhost:8080/services`
* WSDL állomány: `http://localhost:8080/services/employees?wsdl`

---

## Naplózás

```java
@Bean
public Endpoint employeeEndpoint(Bus bus, EmployeesWebService employeesWebService) {
    var endpoint = new EndpointImpl(bus, employeesWebService);
    endpoint.setFeatures(List.of(loggingFeature()));
    endpoint.publish("/employees");
    return endpoint;
}

@Bean
public LoggingFeature loggingFeature() {
    var loggingFeature = new LoggingFeature();
    loggingFeature.setPrettyLogging(true);
    loggingFeature.setVerbose(true);
    loggingFeature.setLogMultipart(true);
    return loggingFeature;
}
```

---

# SOAP webszolgáltatások hívása CXF-fel

---

## Java kód generálás

```xml
<plugin>
	<groupId>org.apache.cxf</groupId>
	<artifactId>cxf-codegen-plugin</artifactId>
	<version>${cxf.version}</version>
	<executions>
		<execution>
			<id>generate-sources</id>
			<phase>generate-sources</phase>
			<configuration>
				<wsdlOptions>
					<wsdlOption>
						<wsdl>${basedir}/src/main/resources/hello.wsdl</wsdl>
					</wsdlOption>
				</wsdlOptions>
			</configuration>
			<goals>
				<goal>wsdl2java</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

---

## Webszolgáltatás hívás

```java
HelloEndpointService service = new HelloEndpointService(getUrl());
HelloEndpoint port = service.getHelloEndpointPort();
HelloRequest request = new HelloRequest();
request.setName(name);
HelloResponse response = port.sayHello(request);
return response.getMessage();
```

URL felülbírálása, tipikusan `application.properties` állományból

# Actuator

## Actuator alapok

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

* `http://localhost:8080/actuator` címen elérhető az <br /> enabled és exposed endpontok listája
* Logban:

```plaintext
o.s.b.a.e.web.EndpointLinksResolver:
  Exposing 2 endpoint(s) beneath base path '/actuator'
```

* További actuator végpontok bekapcsolása: <br /> `management.endpoints.web.exposure.include` <br />konfigurációval

---

## Actuator haladó

* Összes expose: `management.endpoints.web.exposure.include = *`
* Mind be van kapcsolva, kivéve a shutdown

```properties
management.endpoint.shutdown.enabled = true
```

* Saját fejleszthető
* Biztonságossá kell tenni

---

## Health

```json
{"status":"UP"}
```

```properties
management.endpoint.health.show-details = always
```

* Létező JDBC DataSource, MongoDB, JMS providers, stb.
* Saját fejleszthető (`implements HealthIndicator`)

---

## Health details

.small-code-14[
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1023531806720,
        "free": 537008545792,
        "threshold": 10485760,
        "path": ".",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```
]

---

## JVM belső működés

* Heap dump: `/heapdump` (bináris állomány)
* Thread dump: `/threaddump`

---

## Spring belső működés

* Beans: `/beans`
* Conditions: `/conditions`
    * Autoconfiguration feltételek teljesültek-e vagy sem - ettől függ, <br /> milyen beanek kerültek létrehozásra
* HTTP mappings: `/mappings`
    * HTTP mappings és a hozzá tartozó kiszolgáló metódusok
* Configuration properties: `/configprops`

---

## HttpExchange

* Ha van `HttpExchangeRepository` az application contextben
* Egyik implementációja: `InMemoryHttpExchangeRepository`
* Elosztott környezetben: Micrometer Tracing
* Megjelenik a `/httpexchanges` endpoint

---

## Kapcsolódó szolgáltatások <br /> és library-k

* `/caches` - Cache
* `/scheduledtasks` - Ütemezett feladatok
* `/flyway` - Flyway
* `/liquibase` - Liquibase
* `/integrationgraph` - Spring Integration
* `/sessions` - Spring Session
* `/jolokia` - Jolokia (JMX http-n keresztül)
* `/prometheus`

---

## Info

* `info` prefixszel megadott property-k belekerülnek

```properties
# Spring Boot 2.6 óta
management.info.env.enabled=true

info.appname = employees
```

```json
{"appname":"employees"}
```

---

## Property

* `/env` végpont - property source-ok alapján felsorolva
* `/env/info.appname` - értéke, látszik, hogy melyik property source-ból jött
* Spring Cloud Config esetén `POST`-ra módosítani is lehet <br /> (Spring Cloud Config Server használja)

---

# Build és Git információk megjelenítése

---

## Build információk megjelenítése

.small-code-14[
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
]

A `target/classes/META-INF` könyvtárban `build-info.properties` fájl 

.small-code-14[
```json
{
    "build": {
        "artifact": "employees",
        "name": "employees",
        "time": "2022-10-05T20:50:03.216Z",
        "version": "0.0.1-SNAPSHOT",
        "group": "training"
    }
}
```
]

---

## Git információk megjelenítése

```xml
<plugin>
  <groupId>io.github.git-commit-id</groupId>
  <artifactId>git-commit-id-maven-plugin</artifactId>
</plugin>
```

A `target/classes` könyvtárban `git.properties` fájl

```json
{
  "appname": "employees",
  "git": {
    "branch": "master",
    "commit": {
      "id": "d63acd0",
      "time": "2020-02-04T11:12:58Z"
    }
  }
}
```
# Naplózás

---

## Naplózás lekérdezése és beállítása

* `/loggers`
* `/logfile`

```plaintext
### Get logger
GET http://localhost:8080/actuator/loggers/training.employees

### Set logger
POST http://localhost:8080/actuator/loggers/training.employees
Content-Type: application/json

{
  "configuredLevel": "INFO"
}
```

---

# Metrics

---

## Metrics

* `/metrics` végponton
* [Micrometer](https://micrometer.io/) - application metrics facade (mint az SLF4J a naplózáshoz)
* Több, mint 15 monitoring eszközhöz való csatlakozás <br /> (Elastic, Ganglia, Graphite, New Relic, Prometheus, stb.)

# Audit events

---

## Audit events

* Pl. bejelentkezéssel kapcsolatos események
* Saját események vehetőek fel
* Kell egy `AuditEventRepository` implementáció, <br />beépített: `InMemoryAuditEventRepository`
* Megjelenik az `/auditevents` endpoint

```java
applicationEventPublisher.publishEvent(
  new AuditApplicationEvent("anonymous",
    "employee_has_been_created",
      Map.of("name", command.getName())));
```

---

# Tracing

```
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin
```

## Tracing

http://localhost:9411

```java
@Observed(name = "list.employees", contextualName = "list.employees", lowCardinalityKeyValues = {"framework", "spring"})
```

* `pom.xml`: `spring-boot-starter-aop`, `net.ttddyy.observation:datasource-micrometer-spring-boot`

```xml
<dependency>
    <groupId>net.ttddyy.observation</groupId>
    <artifactId>datasource-micrometer-spring-boot</artifactId>
    <version>1.0.2</version>
</dependency>
```

# Continuous Delivery

---

## Continuous Delivery

* A DevOps központi eleme
* Elvek és gyakorlatok összessége
* Értékes és robosztus szoftver kiadására
* Rövid ciklusokban
* Funkcionalitás apró darabokban kerül hozzáadásra
* Bármelyik pillanatban (potenciálisan) szállítható
  * Amiről az üzlet dönthet
* Alapja: Jez Humble, Dave Farley: Continuous Delivery (Addison-Wesley, 2010)
* Technológiai és szervezeti vonatkozásai
* Daniel Bryant, Abraham Marín-Pérez: Continuous Delivery in Java (O'Reilly, 2018)

---

## Pipeline

* Eszköze az automatizált, ismételhető, megbízható build pipeline
  mely a változtatás szállítását végzi
* Tipikus feladatok
	* Fordítás
  * Unit tesztek
	* Csomagolás (package), manapság gyakran konténer image-be
	* Publikálás (repo-kba - deploy)
  * Tesztelés: automata és manuális
    * Statikus kódellenőrzés/tesztelés
    * Funkcionális tesztek, lsd. teszt piramis (unit, integrációs, end-to-end), tesztlefedettség, stb.
    * Nem-funkcionális követelmények tesztelése (teljesítmény és biztonsági tesztek)
	* Telepítés különböző környezetekbe, beleértve az adatbázis migrációkat is (deploy)

---

## Gyors visszajelzések előnyei

* Gyors visszajelzés
	* Üzleti oldalról
	* Technológiai oldalról
* Minél koraibb visszacsatolás, annál kisebb a hibajavítás költsége
* Hibajavításkor nem nagyon régi kódhoz kell visszanyúlni
* Csökken a context switch költsége

---

## Ellenpélda

* Megbízhatatlan, körülményes, manuális telepítési folyamat
* Kiadás elodázása
* Big bang release
* Lassú visszajelzés

---

## Metrikák

* Milyen gyakran történik szállítás?
* Mennyi idő alatt kerül ki egy módosítás az élesbe?
* Mennyi a sikertelen kiadások aránya?
* Hiba esetén általában mennyi a helyreállítás ideje?

[DevOps Research and Assessment](https://dora.dev/)

---

## Continuous Integration

* Elkészült kód naponta többszöri integrálása
* Verziókövető rendszerbe való feltöltés, fordítás, 
  esetleg a tesztesetek futtatása
* A CD ennél több, ugyanis az pluszként elvárja, hogy az alkalmazás
  bármikor szállítható
* CI/CD nehezen értelmezhető, de széleskörben elterjedt

<img src="images/ci-test.png" style="width: 50%;" class="imgcenter" />

---

## Continuous Deployment

* Ha a módosítás után végigfut a pipeline, beleértve az automata teszteket is, akkor
  szállítható, azaz azonnal igénybe vehetik a felhasználók

<img src="images/ci-cd.png" style="width: 50%;" class="imgcenter" />

Forrás: [Atlassian: Continuous integration vs. delivery vs. deployment](https://www.atlassian.com/continuous-delivery/principles/continuous-integration-vs-delivery-vs-deployment)


---

# Pipeline és legjobb gyakorlatok

---

## Pipeline

<p><img src="images/pipeline-01.png" style="width: 55%;"  /></p>
<p><img src="images/pipeline-01b.png" style="width: 35%;"  /></p>

Forrás: [Jez Humble: Deployment pipeline anti-patterns](https://continuousdelivery.com/2010/09/deployment-pipeline-anti-patterns/)

---

## Basic pipeline


<img src="images/basic-pipeline.png" style="width: 50%;"  />

Forrás: Jez Humble, Dave Farley: Continuous Delivery (Addison-Wesley, 2010)

---

## Egy artifact megy végig

* Egy artifact megy végig: _single source of truth_
  * Amennyiben más megy ki teszt és éles környezetbe, lehet, hogy a hiba csak az egyik környezetben fog megjelenni
  * Így szoktak hivatkozni a verziókövető rendszerre is

---

## Azonos környezetek és telepítés

* Works on my machine eliminálása
* Környezetek közötti különbségek minimalizálása
* Tesztelve lesznek a telepítő szkriptek
* A konfiguráció a környezet és nem az alkalmazás része

---

## Smoke teszt a különböző <br /> környezeteken

* Elindult-e az alkalmazás az adott környezeten
* Elérhetőek-e a függőségei, pl. adatbázis, message broker, stb.

---

## Minden változtatás indítsa <br /> a pipeline-t

* Csak így deríthető melyik változtatás okozta a hibát

---

## Hiba a pipeline-ban

* Ha az egyik stage hibára fut, akkor álljon meg a pipeline

---

# Konkrét pipeline és eszközök

---

## Legjobb gyakorlat

* Érdemes a pipeline stage-eit egyesével, iteratív módon fejleszteni
* Egy stage a CD eszköztől leválasztva is külön futtatható legyen
  * Könnyebb fejlesztés és tesztelés
  * Platformfüggetlenség

---

## Konkrét pipeline és eszközök

<img src="images/stages-and-tools.drawio.png" style="width: 50%;"  />
