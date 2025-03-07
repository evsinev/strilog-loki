package com.acme.strilog.loki.api.impl;

import com.acme.strilog.loki.api.ILokiRemoteService;
import com.acme.strilog.loki.api.model.LokiPushResponseType;
import com.google.gson.Gson;
import com.acme.strilog.loki.api.messages.LokiPushRequest;
import com.acme.strilog.loki.api.messages.LokiPushResponse;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.acme.strilog.loki.api.impl.LokiPushMessageMapper.mapToGsonTree;
import static java.net.http.HttpClient.Redirect.NEVER;
import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;

public class LokiRemoteClientImpl implements ILokiRemoteService {

    private static final Logger LOG = LoggerFactory.getLogger(LokiRemoteClientImpl.class);

    private final Gson gson = LOG.isTraceEnabled()
            ? new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
            : new GsonBuilder().disableHtmlEscaping().create();

    private final HttpClient httpClient;
    private final URI    uri;
    private final String basicAuth;

    public LokiRemoteClientImpl(String aUrl, String aBasicAuth) {
        uri        = URI.create(aUrl);
        basicAuth  = aBasicAuth;
        httpClient = HttpClient.newBuilder()
                .version(HTTP_1_1)
                .connectTimeout(ofSeconds(20))
                .followRedirects(NEVER)
                .build();
    }

    @Override
    public LokiPushResponse push(LokiPushRequest aLogs) {
        String json  = gson.toJson(mapToGsonTree(aLogs));

        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending json {}", json);
        }

        byte[] bytes = json.getBytes(UTF_8);

        LOG.debug("Sending {} bytes to {} ...", bytes.length, uri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri     ( uri           )
                .timeout ( ofSeconds(30) )
                .header  ( "Content-Type", "application/json; charset=utf-8")
                .header  ( "Authorization", basicAuth)
                .POST    ( ofByteArray(bytes) )
                .build();

        HttpResponse<String> response = sendHttpRequest(request);

        if (response.statusCode() == 204) {
            LOG.debug("Success response");
            return LokiPushResponse.builder()
                    .type(LokiPushResponseType.SUCCESS)
                    .build();
        }

        LOG.warn("Bad response {} : {}", response.statusCode(), response.body());
        LOG.debug("Json was {}", json);

        return LokiPushResponse.builder()
                .type(LokiPushResponseType.ERROR_BAD_REQUEST)
                .errorMessage(response.body())
                .build();
    }

    private HttpResponse<String> sendHttpRequest(HttpRequest request) {
        try {
            return httpClient.send(request, ofString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot send request to " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while sending to " + uri, e);
        }
    }
}
