# Microservice alkalmazás felépítése Spring Boot keretrendszerrel Docker környezetben

Ez a repo tartalmazza a bemutatott példaprogramokat.

Follow up:

* A `@ConfigurationProperties` annotációval ellátott osztályra (`HelloProperties`) a `@Validated` annotációt kell tenni, hogy legyen validáció.
* A `EmployeesControllerTest` osztályban a `findEmployeeById()` metódusban a paraméter ellenőrzésére figyelni kell

A következő kódrészlet nem működik:

```java
@Test
void findEmployeeById() {
    employeesController.findEmployeeById(10);

    verify(employeesService).findEmployeeById(argThat(i -> i == 10));
}
```

Ez kivételt dob:

```
java.lang.NullPointerException: Cannot invoke "java.lang.Long.longValue()" because the return value of "org.mockito.Mockito.argThat(org.mockito.ArgumentMatcher)" is null
```

A baj az, hogy a paraméter primitív típusú. Ekkor az `argThat()` metódus nem használható. Ekkor a primitív típusnak megfelelő metódust kell használni, itt `longThat()`.

Azaz a teszteset helyesen:

```java
@Test
void findEmployeeById() {
    employeesController.findEmployeeById(10);

    verify(employeesService).findEmployeeById(longThat(i -> i == 10));
}
```