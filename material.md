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