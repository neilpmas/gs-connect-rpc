package com.example.greeting;

import com.example.greeting.v1.GreetingRequest;
import com.example.greeting.v1.GreetingResponse;
import com.example.greeting.v1.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

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
