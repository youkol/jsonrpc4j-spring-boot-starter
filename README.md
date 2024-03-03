# jsonrpc4j-spring-boot-starter

Spring boot starter for jsonrpc4j.

## jsonrpc4j

* jsonrpc4j Github: <https://github.com/briandilley/jsonrpc4j>

## 1. Usage

### 1.1. Maven

```xml
<dependency>
    <groupId>com.youkol.support.jsonrpc4j</groupId>
    <artifactId>jsonrpc4j-spring-boot-starter</artifactId>
    <version>${jsonrpc4j-spring-boot.version}</version>
</dependency>
```

### 1.2. Spring Boot application.properties/yaml

```yaml
# Default value for jsonrpc4j-spring-boot-starter
youkol:
  jsonrpc4j:
    enabled: true
    server:
      enabled: true
      backwards-compatible: true
      rethrow-exceptions: false
      allow-extra-params: false
      allow-less-params: false
      should-log-invocation-errors: true
      parallel-enabled: false
      parallel-batch-processing-timeout: 30s
      content-type: "application/json-rpc"
      welcome:
        enabled: true
      servlet:
        enabled: true
        path: "/jsonrpc"
        load-on-startup: -1
    client:
      base-url:
      enabled: false
      scan-package:
      content-type:
```

### 1.3. Use Servlet Mode (`JsonRpcMultiServer`)

#### 1.3.1. Java Code

> @JsonRpcMultiServiceName: If you want to use multiple services, you can use this annotation for special the service name.
>
> `JsonRpcBaseService` is the base class for all services. You must extends it on your interface, it will be auto injected by `JsonRpcMultiServer.addService`

```java
@JsonRpcMultiServiceName("User")
public interface UserService extends JsonRpcBaseService {
    User createUser(@JsonRpcParam(value="userName") String userName,
                    @JsonRpcParam(value="firstName") String firstName,
                    @JsonRpcParam(value="password") String password);
    User createUser(@JsonRpcParam(value="userName") String userName,
                    @JsonRpcParam(value="password") String password);
    User findUserByUserName(@JsonRpcParam(value="userName") String userName);
    int getUserCount();
}

@Service
public class UserServiceImpl implements UserService {

    public User createUser(String userName, String firstName, String password) {
        User user = new User();
        user.setUserName(userName);
        user.setFirstName(firstName);
        user.setPassword(password);
        database.saveUser(user);
        return user;
    }

    public User createUser(String userName, String password) {
        return this.createUser(userName, null, password);
    }

    public User findUserByUserName(String userName) {
        return database.findUserByUserName(userName);
    }

    public int getUserCount() {
        return database.getUserCount();
    }

}
```

#### 1.3.2. JSON request for RPC call

```json
POST http://localhost:8080/jsonrpc
body:
{
    "jsonrpc":"2.0",
    "method": "User.createUser",
    "params": {
        "userName": "",
        "firstName": "",
        "password": ""
    },
    "id": "1"
}
```

### 1.4. Use jsonrpc4j annotation (`AutoJsonRpcServiceImplExporter`)

* @JsonRpcService
* @AutoJsonRpcServiceImpl
* @JsonRpcParam
* @JsonRpcMethod

#### 1.4.1. Java Code for Annotations

```java
@JsonRpcService("/jsonrpc/user")
public interface UserService {
    // @JsonRpcMethod is optional, if you need custom method names, you can use it.
    // @JsonRpcMethod("User.create")
    User createUser(@JsonRpcParam(value="userName") String userName,
                    @JsonRpcParam(value="firstName") String firstName,
                    @JsonRpcParam(value="password") String password);
    // @JsonRpcMethod("User.create")
    User createUser(@JsonRpcParam(value="userName") String userName,
                    @JsonRpcParam(value="password") String password);
    // @JsonRpcMethod("User.findUserByUserName")
    User findUserByUserName(@JsonRpcParam(value="userName") String userName);
    // @JsonRpcMethod("User.getUserCount")
    int getUserCount();
}

@Service
@AutoJsonRpcServiceImpl
public class UserServiceImpl implements UserService {

    public User createUser(String userName, String firstName, String password) {
        User user = new User();
        user.setUserName(userName);
        user.setFirstName(firstName);
        user.setPassword(password);
        database.saveUser(user);
        return user;
    }

    public User createUser(String userName, String password) {
        return this.createUser(userName, null, password);
    }

    public User findUserByUserName(String userName) {
        return database.findUserByUserName(userName);
    }

    public int getUserCount() {
        return database.getUserCount();
    }

}
```

#### 1.4.2. JSON request

```json
POST http://localhost:8080/jsonrpc/user
body:
{
    "jsonrpc":"2.0",
    "method": "User.createUser",
    // "method": "User.create", # for @JsonRpcMethod
    "params": {
        "userName": "",
        "firstName": "",
        "password": ""
    },
    "id": "1"
}
```

### 1.5. Parallel batch an Array filled with Request objects

The `ExecutorService` utilizes `ThreadPoolExecutor` as its default implementation.
It is configured through `ThreadPoolTaskExecutor` from `TaskExecutionAutoConfiguration`.
As a result, the `ExecutorService` and `@Async` share a thread pool.

You can customize an `ExecutorService` to replace it.
