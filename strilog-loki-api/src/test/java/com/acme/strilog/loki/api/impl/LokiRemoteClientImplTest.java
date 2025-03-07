package com.acme.strilog.loki.api.impl;


import com.acme.strilog.loki.api.messages.LokiPushRequest;
import com.acme.strilog.loki.api.messages.LokiPushResponse;
import com.acme.strilog.loki.api.model.LokiLogLine;
import com.acme.strilog.loki.api.model.SaveLogEvent;
import com.payneteasy.startup.parameters.AStartupParameter;
import com.payneteasy.startup.parameters.StartupParametersFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LokiRemoteClientImplTest {

    private static final Logger LOG = LoggerFactory.getLogger( LokiRemoteClientImplTest.class );

    interface IConfig {
        @AStartupParameter(name = "LOKI_AUTH_USERNAME", value = "test")
        String lokiAuthUsername();

        @AStartupParameter(name = "LOKI_AUTH_PASSWORD", value = "test", maskVariable = true)
        String lokiAuthPassword();

        @AStartupParameter(name = "LOKI_PUSH_URL", value = "")
        String lokiPushUrl();
    }

    @Test
    public void test() {
        IConfig config = StartupParametersFactory.getStartupParameters(IConfig.class);
        if(config.lokiPushUrl() == null || config.lokiPushUrl().isEmpty()) {
            LOG.warn("LOKI_PUSH_URL is empty. Skipping test.");
            return;
        }
        LokiRemoteClientImpl client = new LokiRemoteClientImpl(
                config.lokiPushUrl()
                , "test"
        );

        LokiPushRequest request = LokiPushRequest.builder()
                .streamMetadata(Map.of(
                        "foo", "bar2"
                        , "service_name", "hello-2"
                ))
                .logLines(List.of(
                        LokiLogLine.builder()
                                .epoch(toNanoEpoch(System.currentTimeMillis()))
                                .message("Test message " + new Date())
                                .meta(Map.of(
                                        "level", "INFO"
                                        , "key1", "hello-3"
                                ))
                                .build()
                        , LokiLogLine.builder()
                                .epoch(toNanoEpoch(System.currentTimeMillis()))
                                .message("Test message " + new Date())
                                .meta(Map.of(
                                        "level", "INFO"
                                        , "key1", "hello-3"
                                ))
                                .build()
                ))
                .build();

        LokiPushResponse response = client.push(request);

        System.out.println("response = " + response);
    }

    private long toNanoEpoch(long epoch) {
        long nanos = System.nanoTime() % 1_000_000;
        return epoch * 1_000_000L + nanos ;
    }

}