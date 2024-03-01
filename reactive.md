class: inverse, center, middle

# Reaktív programozás Spring keretrendszerrel

---

class: inverse, center, middle

# Reaktív Kiáltvány

---

## Reaktív Kiáltvány 

* Változó igényeknek való megfelelés
  * Elvárás az ezredmásodperces válaszidő, 100%-os rendelkezésre állás, petabyte méretű adatok kezelése, cloud futtatókörnyezet
  * Alkalmazások hibatűrőbbek, öngyógyítóak, flexibilisebbek
  * Reactive Manifesto szerint ezeket az igényeket a reaktív rendszerek képesek kielégíteni, a következő tulajdonságokkal
* [The Reactive Manifesto](https://www.reactivemanifesto.org/)
  * Reszponzivitás (Responsive): az alkalmazásnak minden esetben gyors választ kell adnia
  * Ellenállóképesség (Resilient): az alkalmazásnak reszponzívaknak kell maradnia hiba esetén is
  * Elaszticitás (Elastic): reszponzivitás nagy terhelés esetén is
  * Üzenetvezéreltség (Message-driven): rendszerek elemei aszinkron, nem blokkoló módon, üzenetekkel kommunikálnak

---

## Mit nem várhatunk?

* Klasszikus CRUD alkalmazásoknál, normál terhelés mellett nem tapasztalunk teljesítményjavulást
* Nagy terhelés és/vagy hibák megjelenése esetén mutatkozik meg az előnye

---

class: inverse, center, middle

# Reaktív rendszerek akadályozó tényezői Javaban

---

## Akadályozó tényezők

* A reaktív rendszerek fejlesztésének számos akadályozó tényezője van Javaban

---

## Akadályozó tényezők: <br /> Szinkron IO

* CPU arra vár, hogy beérkezzen az adat (pl. fájlrendszer, teljes http request, adatbázis result set)
* A megoldás létezik Java 1.4 óta: Java NIO (New IO), másnéven Non-blocking IO
* Operációs rendszer lehetőségeit használja ki (Linux, Windows)
* Nem várja meg az olvasás eredményét, hanem egy callback, mely visszahívásra kerül, ha előállt az adat
* CPU-t szabadít fel
* `java.nio.Buffer`, pl. `ByteBuffer` - olyan pufferterület, mely konkrétan az operációs rendszerhez kötött,
  így pl. fájl olvasáskor nem kell az operációs rendszer memóriájából a JVM memóriájába átmásolni, így CPU-t takarít meg
* `java.nio.channels.Channel`, pl. `AsynchronousFileChannel` - `Buffer` írás és olvasás, tipikusan fájl és socket felé vagy felől

---

## AsynchronousFileChannel

```java
public abstract Future<Integer> read​(ByteBuffer dst,
                                     long position)

public abstract <A> void read​(ByteBuffer dst,
                              long position,
                              A attachment,
                              CompletionHandler<Integer,? super A> handler)
```

Az `Attachment` a context tárolására való, ez köthető az aktuális olvasáshoz,
ebben lehet információt átadni a `CompletionHandler` példánynak 

---

## NIO státusza

* Különösen hatékony, ha pl. fájlt kell kiszolgálni http-n, hiszen nem kell a JVM memóriába beolvasni
* Nem használjuk, túl alacsony szintű
* Kevés eszköz támogatja
* Bizonyos eszközök támogatják, pl. a Netty (NIO client server framework, hálózati alkalmazások fejlesztésére)
  * Szakít a klasszikus szinkron Servlet API hagyományokkal

---

## Akadályozó tényezők: <br /> Szálkezelés

* IO-ra várás blokkolja a szálakat
* Skálázás
  * Futtatás több szálon
  * Horizontális skálázás
* Problémák
  * 1 MB stack/thread (1000 szál esetén?)
  * Context switch, hiszen nincs annyi magos processzorunk

---

## Akadályozó tényezők: <br /> Kollekciók

* Magas absztrakciós szinten gondolkozunk, tipikusan entitások kollekciói
* Kollekciók esetén be kell gyűjteni az összes elemet
* Az `Iterator` és a `Stream` már jó előrelépés, azonban a _pull_-központúak

---

## Akadályozó tényezők: <br /> Push túlterhelés

* Amennyiben a termelő a saját ütemében állítja elő az adatot,
túlterhelheti a fogyasztót
* Ez hálózati protokolloknál ismert jelenség, megoldása a flow control, vagy push back
* Több mechanizmus is van (lásd [Wikipedia](https://en.wikipedia.org/wiki/Flow_control_(data))

---

## Akadályozó tényezők: <br /> Keretrendszer támogatás

* Hiánya
* Pl. Spring, Hibernate

---

class: inverse, center, middle

# Reaktív programozás

---

## Reaktív programozás

* Programozási paradigma, implementációs technika
* A rendszer az adatelemek folyamára reagál
* Aszinkron, nem blokkoló végrehajtás
* Back pressure: reaktív nevezéktanban mechanizmus arra, hogy a termelő ne árassza el a fogyasztót
* Non-blocking back pressure: fogyasztó kéri a következő x elemet, amit fel tud dolgozni
* Nem kivétel alapú hibakezelés, nem akasztja meg az adatfolyamot, tipikusan egy callback
* Tipikus felhasználási területei
  * Külső rendszerek hívása
  * Jelentős mennyiségű üzenet párhuzamos feldolgozása
  * Táblázatkezelés: egy cellát módosítva változik több cella tartalma

---

## Funkcionális reaktív programozás

* Funkcionális stílusban kivitelezett reaktív programozás
* Alapja a mellékhatás és állapot nélküli függvények
* Deklaratív
* Sok boilerplate kód eliminálása
* Könnyebb karbantarthatóság, jobb kódminőség
* Defacto standard megoldások
* Callback-hell ellen
* Újrafelhasználható operátorok

---

## Kitekintés: felhasználói <br /> felület (mobil) fejlesztés

* Android környezetben az RxJava elterjedt
* Szálkezelés megvalósításának egyszerűsítésére
    * Szálkezelés: felhasználói interakció, hálózat, szenzorok (pl. GPS), stb.
* Callback hell ellenszere
* Legjobb gyakorlatok (pl. a felhasználó közbülső interakcióját nem kell figyelembe venni 
	- karakterenkénti szűrés, keresés - Throttling)

---

## Reaktív megvalósítások <br /> Javaban

* Eclipse Vert.x
* Akka
* RxJava
* Project Reactor

---

## Reaktív megoldások együttműködésére

* [Reactive Streams specifikáció](https://www.reactive-streams.org/)
* Observer design pattern
* `Observer` interfész Java 9 óta deprecated
* Non-blocking back pressure, rejtett, ha magas szintű API-kat használunk

---

## Java 9 Flow API

<img src="images/java-9-flow-api.png" width="600" alt="Java 9 Flow API UML class diagram" />

---

class: inverse, center, middle

# Project Reactor

---

## Project Reactor

* Reactive Streamsre épülő reactive library
* Spring közeli
* Teljes ecosystem, nem kell magunk implementálni:
  * Reactor Netty: interprocess communication, HTTP, TCP, UDP kliens/szerver, Netty-re építve
  * Reactor Kafka: Kafka integráció
  * Reactor RabbitMQ: RabbitMQ integráció

---

## pom.xml

```xml
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-core</artifactId>
  <version>3.5.1</version>
</dependency>
```

---

## Típusos adatfolyamok

* `Mono<T>`: nulla vagy egy elem
* `Flux<T>`: n elem
* Implementálják a `Publisher` interfészt

---

## Flux létrehozása és <br /> felíratkozás

```java
Flux.just(new Employee("John Doe", 1970),
      new Employee("Jack Doe", 1980),
      new Employee("Jane Doe", 1990))
    .subscribe(System.out::println);
```

---

## Block

```java
Employee employee = Mono.just(new Employee("John Doe", 1970))
    .block();

Employee employee = Flux.just(new Employee("John Doe", 1970),
    new Employee("Jack Doe", 1980),
    new Employee("Jane Doe", 1990))
  .blockFirst(); // .blockLast()    

List<Employee> employees = Flux.just(new Employee("John Doe", 1970),
      new Employee("Jack Doe", 1980),
      new Employee("Jane Doe", 1990))
    .collectList().block();
```

---

## Operátorok

<img src="images/flux.svg" width="600" />

[API dokumentáció](https://projectreactor.io/docs/core/release/api/)

---

## Marble diagram

<img src="images/mapForFlux.svg" width="600" />

---

## Közbülső operátorok

```java
Flux.just(
      new Employee("John Doe", 1970),
      new Employee("Jack Doe", 1980),
      new Employee("Jane Doe", 1990))
    .filter(e -> e.getYearOfBirth() <= 1980)
    .map(Employee::getName)
    .map(String::toUpperCase)
    .sort()
    .skip(1)
    .subscribe(System.out::println);
```

---

## Ugyanez Java 8 <br /> Stream API-val

```java
List.of( new Employee("John Doe", 1970),
      new Employee("Jack Doe", 1980),
      new Employee("Jane Doe", 1990))
    .stream()
    .filter(e -> e.getYearOfBirth() <= 1980)
    .map(Employee::getName)
    .map(String::toUpperCase)
    .sorted()
    .skip(1)
    .forEach(System.out::println);
```

---

## Mono és Flux közötti konvertálás

```java
Mono<Employee> employee = Flux.just(new Employee("John Doe", 1970))
  .single();

Flux<Employee> employees = Mono.just(new Employee("John Doe", 1970))
    .flux();
```

---

class: inverse, center, middle

# Hibakezelés

---

## Hiba

```java
Mono.just(new Employee("John Doe", 1970))
                .map(e -> e.getAgeAt(1960))
                .subscribe(System.out::println);
```

```
[ERROR] (main) Operator called default onErrorDropped - reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalArgumentException: Birth after year 1960
reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalArgumentException: Birth after year 1960
Caused by: java.lang.IllegalArgumentException: Birth after year 1960
	at training.empapp.Employee.getAgeAt(Employee.java:17)
```

---

## Hibakezelés

```java
Mono.just(new Employee("John Doe", 1970))
    .map(e -> e.getAgeAt(1960))
    .doOnError(System.out::println)
    .onErrorResume(t -> Mono.just(-1))
    .subscribe(System.out::println);
```

```java
Mono.just(new Employee("John Doe", 1970))
    .map(e -> e.getAgeAt(1960))
    .onErrorReturn(-1)
    .subscribe(System.out::println);
```

---

class: inverse, center, middle

# Tesztelés

---

## pom.xml

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <version>3.5.1</version>
</dependency>
```

---

## Tesztelése

```java
Flux<String> employees = Flux.just(
      new Employee("John Doe", 1970),
      new Employee("Jack Doe", 1980),
      new Employee("Jane Doe", 1990))
    .filter(e -> e.getYearOfBirth() <= 1980)
    .map(Employee::getName)
    .map(String::toUpperCase)
    .sort()
    .skip(1);

StepVerifier.create(employees)
        .expectNext("JOHN DOE")
        .verifyComplete();
```

* `expectNextCount()`

```java
StepVerifier.create(employees)
                .expectNextMatches(name -> name.toLowerCase().contains("john"))
                .verifyComplete();
```

---

class: inverse, center, middle

# Keretrendszerek integrálása

---

## pom.xml

```xml
<dependency>
  <groupId>io.reactivex.rxjava3</groupId>
  <artifactId>rxjava</artifactId>
  <version>3.1.5</version>
</dependency>
```

---

## RxJava megvalósítás

```java
var employees = List.of(
        new Employee("John Doe", 1970),
        new Employee("Jack Doe", 1980),
        new Employee("Jane Doe", 1990)
        );

Flowable.fromIterable(employees)
    .filter(employee -> employee.getYearOfBirth() <= 1980)
    .map(Employee::getName)
    .sort()
    .skip(1);
```

---

## Keretrendszerek <br /> integrálása

```java
var employees = List.of(
        new Employee("John Doe", 1970),
        new Employee("Jack Doe", 1980),
        new Employee("Jane Doe", 1990)
        );

Flux.from(Flowable.fromIterable(employees)
        .filter(employee -> employee.getYearOfBirth() <= 1980)
        .map(Employee::getName))
        .sort()
    	.skip(1);
```

---

## Klasszikus Servlet API

<img src="images/ThreadPool.png" width="600" />

---

## Spring WebFlux

* Spring MVC alternatíva
* Spring MVC tapasztalataira építve
* Hasonló megközelítés, egymás mellett élő, független implementációk
* Reactive HTTP API-ra építve (Servlet API helyett)
* Default web konténer: Netty
* Router functions
* WebClient: non-blocking, reactive HTTP kliens
* Jól használható Websocket és SSE esetén

---

## Spring WebFlux

<img src="images/Non-blocking-request-processing.png" alt="Non-blocking request processing" width="900" />

---

## Controller

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    @GetMapping
    public Flux<EmployeeDto> listEmployees() {
        return employeeService.listEmployees();
    }

    @PostMapping
    public Mono<EmployeeDto> createEmployee(@RequestBody Mono<CreateEmployeeCommand> command) {
        return employeeService.createEmployee(command);
    }
}
```

---

## RouterFunction

```java
@Configuration
public class CityController {

    private CityService cityService;

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(RequestPredicates.GET("/api/cities/{name}"), this::findByName);
    }

    public Mono<ServerResponse> findByName(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(cityService.findByName(request.pathVariable("name")), City.class);
    }

}
```

---

## Service réteg

```java
@Service
public class EmployeeService {

    private EmployeeRepository employeeRepository;

    public Mono<EmployeeDto> createEmployee(Mono<CreateEmployeeCommand> command) {
        return command
                .map(this::toEmployee)
                .flatMap(employeeRepository::save)
                .map(this::toEmployeeDto);
    }

    public Flux<EmployeeDto> listEmployees() {
        return employeeRepository
                .findAll()
                .map(this::toEmployeeDto);
    }

}
```

---

## Klasszikus RDBMS réteg

* JDBC
* Szinkron, blokkoló

---

## Reactive RDBMS

* R2DBC nyílt specifikáció
* Project Reactor és RxJava támogatás
* Implementációk: H2, MySQL/MariaDB, Oracle, Microsoft SQL Server
* Klasszikus funkciók: tranzakciókezelés, batch, LOB, unchecked és típusos kivételek
* Tudja használni pl. a Spring Data R2DBC vagy JOOQ

```java
ConnectionFactory connectionFactory = ConnectionFactories
  .get("r2dbc:h2:mem:///testdb");

Mono.from(connectionFactory.create())
  .flatMapMany(connection -> connection
    .createStatement("SELECT firstname FROM PERSON WHERE age > $1")
    .bind("$1", 42)
    .execute())
  .flatMap(result -> result
    .map((row, rowMetadata) -> row.get("firstname", String.class)))
  .doOnNext(System.out::println)
  .subscribe();
```
---

## Spring Data R2DBC <br /> Repository

Repo:

```java
public interface CityRepository extends ReactiveCrudRepository<City, Long> {

}
```

Service:

```java
public Mono<City> findByName(String name) {
  return cityRepository.findByName(name);
}
```

---

## Tranzakciókezelés

* `inTransaction(...)` metódusok
* Ha mégis a klasszikus utat választanánk
* Imperatív megoldás a `ThreadLocal`-hoz köti
* `ReactiveTransactionManager`
* Itt is támogatott a deklaratív és programozott
* Deklaratív: `@Transactional` annotáció
* Programozott: `TransactionalOperator`

[Reactive Transactions with Spring](https://spring.io/blog/2019/05/16/reactive-transactions-with-spring)

---

## TransactionalOperator <br /> példa

```java
TransactionalOperator rxtx = TransactionalOperator.create(tm);

Mono<Void> atomicOperation = db.execute()
  .sql("INSERT INTO person (name, age) VALUES('joe', 'Joe')")
  .fetch().rowsUpdated()
  .then(db.execute()
    .sql("INSERT INTO contacts (name) VALUES('Joe')")
    .then())
  .as(rxtx::transactional);
```

---

class: inverse, center, middle

# MongoDB

---

## MongoDB elindítása

```shell
docker run -d -p27017:27017 --name employees-mongo mongo
```
---

## Alkalmazás előkészítése

`application.properties` fájlban:

```properties
spring.data.mongodb.database = employees
```

`pom.xml` függőség

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```


---

## Dao réteg - MongoDB

```java
public interface EmployeeRepository extends ReactiveMongoRepository<Employee, String> {

}
```

---

## Konzol

```shell
docker exec -it employees-mongo mongosh employees
```

```javascript
db.employees.find()

db.employees.insertOne({"name": "John Doe"})

db.employees.findOne({'_id': ObjectId('60780cf974bc5648cf220a96')})

db.employees.deleteOne({'_id': ObjectId('60780cf974bc5648cf220a96')})
```

---

class: inverse, center, middle

# Reactive WebClient

---

## Klasszikus integráció

* Szinkron blokkoló HTTP protokoll

---

## Reactive WebClient

```java
private Mono<String> findTemperature(String name) {
    return WebClient.create("https://www.idokep.hu/idojaras/".concat(name))
            .get()
            .retrieve()
            .bodyToMono(String.class)
            .flatMapMany(s -> Flux.fromStream(new Scanner(s).findAll("\"homerseklet\">([^<]*)<")))
            .map(m -> m.group(1))
            .next();
}
```

---

class: inverse, center, middle

# RabbitMQ

---

## RabbitMQ

* Message broker
* AMQP szabványos protokoll
* A legtöbb programozási nyelven kliens
* Erlang
* VMware

---

## RabbitMQ futtatása Docker konténerben

```shell
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:3-management
```

`http://localhost:15672/`, `guest` / `guest`

```shell
docker exec rabbitmq rabbitmqctl status
```

---

## Függőség

```xml
<dependency>
    <groupId>io.projectreactor.rabbitmq</groupId>
    <artifactId>reactor-rabbitmq</artifactId>
  </dependency>
```

---

## RSocket

* Reactive Streams semantics between client-server, and server-server communication
* Binary protocol for use on byte stream transports such as TCP, WebSockets (reactor-netty), akka, Aeron (UDP)
* Különböző kommunikációs modellek [Introduction to RSocket](https://www.baeldung.com/rsocket)
  * Request/Response
  * Fire-and-Forget
  * Request/Stream (egy kérés, több válasz)
  * Channel (kétirányú adatfolyam)
* Java, Kotlin, JavaScript, Go, .NET, C++
* Natív Spring támogatás
* Spring server: [Getting Started With RSocket: Spring Boot Server](https://spring.io/blog/2020/03/02/getting-started-with-rsocket-spring-boot-server)
* Spring client: [Getting Started With RSocket: Spring Boot Client](https://spring.io/blog/2020/03/09/getting-started-with-rsocket-spring-boot-client)

---

## Ajánlott irodalom

* Craig Walls: Spring in Action (5. edition)
* Josh Long: Reactive Spring
* [GOTO 2019 • Reactive Spring • Josh Long](https://www.youtube.com/watch?v=1F10gr2pbvQ)