# Getting Started: Serving a gRPC Service over the Connect Protocol

This guide walks you through making an existing gRPC service callable directly
from a browser `fetch()` — or a plain `curl` command — by adding a single Spring
Boot starter dependency, with no changes to the service itself.

## What You Will Build

You will start with a plain Spring Boot WebFlux application that exposes a gRPC
`GreetingService`. As written, the only way to call it is with a native gRPC
client. You will then add the `connectrpc-spring-boot-starter` dependency and,
without touching a line of service code, call the very same service over HTTP
with a JSON body:

```
POST /connect/greeting.v1.GreetingService/Greet
{"name": "World"}
```

and get back:

```json
{"message": "Hello, World!"}
```

This is the [Connect protocol](https://connectrpc.com). Unlike gRPC-Web, it is
wire-compatible with ordinary HTTP requests, so a browser can call your gRPC
service directly — no proxy, no bridge, no separate REST controller.

## What You Need

- About 15 minutes
- JDK 24
- A local clone of
  [`connectrpc-spring-boot-starter`](https://github.com/neilpmas/connectrpc-spring-boot-starter)

## Publish the Starter Locally

The starter is not on Maven Central yet, so publish it to your local Maven
repository once before starting (a temporary step until the library is
published):

```bash
cd /path/to/connectrpc-spring-boot-starter
./gradlew publishToMavenLocal
```

## Starting Out

The `initial/` directory contains a complete, runnable WebFlux application. Its
service is defined by `src/main/proto/greeting.proto`:

```protobuf
syntax = "proto3";

package greeting.v1;

option java_package = "com.example.greeting.v1";
option java_multiple_files = true;

message GreetingRequest {
  string name = 1;
}

message GreetingResponse {
  string message = 1;
}

service GreetingService {
  rpc Greet(GreetingRequest) returns (GreetingResponse);
}
```

The implementation is an ordinary gRPC `BindableService`, registered as a Spring
bean with `@Component`:

```java
@Component
public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        responseObserver.onNext(GreetingResponse.newBuilder()
            .setMessage("Hello, " + request.getName() + "!")
            .build());
        responseObserver.onCompleted();
    }
}
```

You can build it right now:

```bash
cd initial
./gradlew build
```

It compiles and starts — but there is no HTTP endpoint. The service can only be
reached with a native gRPC client, which a browser cannot speak.

## Add the Starter Dependency

Add one line to `build.gradle`. Make sure `mavenLocal()` is listed first so the
starter you just published is found:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'dev.neilmason:connectrpc-spring-boot-starter:0.1.0-SNAPSHOT'
    // ... existing dependencies
}
```

That is the entire change. The `complete/` directory has this already applied —
the proto and the service implementation are byte-for-byte identical to
`initial/`.

## Call the Service over HTTP

Build and run the application:

```bash
cd complete
./gradlew bootRun
```

Then call the gRPC service over the Connect protocol with a plain JSON body:

```bash
curl -X POST http://localhost:8080/connect/greeting.v1.GreetingService/Greet \
  -H "Content-Type: application/json" \
  -d '{"name": "World"}'
```

The response:

```json
{
  "message": "Hello, World!"
}
```

## What Just Happened

When the starter is on the classpath of a reactive Spring Boot application, its
autoconfiguration activates automatically. It discovers every `BindableService`
bean in the context — your `GreetingServiceImpl` — and exposes each RPC at
`POST /connect/{package}.{Service}/{Method}`, content-negotiating between
`application/json` and `application/proto`.

No `@ComponentScan`, no manual bean wiring, no changes to the proto or the
service. The gRPC service you already had is now reachable from any HTTP client,
including a browser:

```js
const res = await fetch("/connect/greeting.v1.GreetingService/Greet", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ name: "World" }),
});
console.log(await res.json()); // { message: "Hello, World!" }
```

## Summary

You took a plain gRPC service, added one dependency, and made it callable over
HTTP with a JSON body — no code changes and no gRPC-Web bridge. Because the
Connect protocol rides on ordinary HTTP, the same service is now reachable
directly from a browser `fetch()`.
