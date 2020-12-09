# YetAnotherNettyRPC

> A remote process call (RPC) framework based on Netty and Zookeeper.

- **Netty** for networking.
- **Zookeeper** as service registration center.
- **Spring Framework** as Ioc context provider.
- **FastJson** as default serializer.

## Features

- Service registration.
- Service auto discovery / update through Zookeeper.
- Call methods through interfaces *with transparency* as if the whole progress happens at local environment. Implemented by introducing *proxy* objects which do the actual Rpc call for us.
- Exception is thrown at local instead of remote side.
- Complex parameter type transimission is supported.
- `FastJson` is introduced as the default serializer. Serializer can be customized using `Serializer` interface.
- Auto-routing to remote server with `LoadBalancer` introduced. They can be customized as well.
- Heart-beat keep alive between Rpc server and Rpc client.

## Boot up the tutorial

There's already a show case provided within the project.

1. Boot up Zookeeper server on port 12181.
2. Open `com.galaxyzeta.test.ServerAppContext.java` and boot it.
3. Open `com.galaxyzeta.test.ClientAppContext.java` and boot it.

What you will see:

1. `RpcServer` begins to push services to Zookeeper.
2. `RpcClient` does service fetching from Zookeeper.
3. Demo RPC calls was made between `RpcServer` and `RpcClient`.
4. `RpcClient` is shutdown because `close()` is invoked.

## How 2 use

If you are going to write your own test cases, please read the following stuff:

### Step 1 Create some services

1. Create interfaces for Rpc calls.

```java
public interface MyService {
	String echo(String data);
}
```

2. Add service implementations.

   The `@RpcServiceComponent` indicates a service to be called in Rpc. It will be registered into Spring Ioc container.

```java
@RpcServiceComponent(interfaceClass = MyService.class, version = "1.0")
public class MyServiceImpl implements MyService {
	public String echo(String data) {
		return data;
	}
}
```

### Step 2 Start up server

1. Before booting the server, make sure Zookeeper is installed and running.

2. Configure the server through `rpc.properties`.

```properties
zookeeper.address = localhost
zookeeper.port = 12181
zookeeper.sessionTimeout = 5000
zookeeper.connectionTimeout = 5000

server.address = localhost
server.port = 18080

```

3. A spring xml configuration is needed. Just use the one I provided in `com/galaxyzeta/resources`.

3. Boot up the server. You can start up a server using application context like this:

```java
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-server.xml");
RpcServer server = (RpcServer)context.getBean("rpcServer");
server.start();
```

### Step 3 Start up client and make rpc call

1. Prepare a class that contains all the services to be used in Rpc call. In this example, the class is named `Foo`.

```java
public class Foo {
	
	@RpcAutowired(version = "1.0")
	private HelloService helloService;

	public HelloService getHelloService() {
		return helloService;
	}
}
```
`@RpcAutowired` is a bit similar to `@Autowired` in Spring framework, which automatically injects an instance into the given field when the application is started.

2. Boot up the client like this:

```java
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
```

3. Do remote progress call just like calling a normal local method.

```java
Foo foo = (Foo) context.getBean("foo");
foo.getHelloService().hello();
```

## Like this project? Give me a star plz!