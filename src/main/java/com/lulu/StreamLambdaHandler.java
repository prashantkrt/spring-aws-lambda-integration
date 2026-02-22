package com.lulu;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.lulu.config.ApplicationContextProvider;
import com.lulu.scheduler.ScheduledJob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

//    @Override
//    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
//            throws IOException {
//        handler.proxyStream(inputStream, outputStream, context);
//    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

        byte[] bytes = input.readAllBytes();
        String eventJson = new String(bytes);

        if (eventJson.contains("aws.events")) {
            context.getLogger().log("EventBridge schedule triggered\n");

            // Get Spring bean safely
            ScheduledJob job = ApplicationContextProvider.getBean(ScheduledJob.class);
            job.handleScheduleEvent(eventJson);

            // EventBridge requires some response
            output.write("{\"status\":\"OK\"}".getBytes());
            return;
        }

        // Otherwise treat as API Gateway request
        handler.proxyStream(
                new ByteArrayInputStream(bytes),
                output,
                context
        );
    }
}