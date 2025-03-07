package com.acme.strilog.loki.api.impl;

import com.acme.strilog.loki.api.messages.LokiPushRequest;
import com.acme.strilog.loki.api.model.LokiLogLine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class LokiPushMessageMapper {

    public static JsonElement mapToGsonTree(LokiPushRequest aRequest) {
        JsonObject root = new JsonObject();
        root.add("streams", toStreams(aRequest));
        return root;
    }

    private static JsonArray toStreams(LokiPushRequest aRequest) {
        JsonObject stream = new JsonObject();
        stream.add("stream", toMap(aRequest.getStreamMetadata()));
        stream.add("values", toValues(aRequest.getLogLines()));

        JsonArray streams = new JsonArray();
        streams.add(stream);
        return streams;
    }

    private static JsonArray toValues(List<LokiLogLine> aLogs) {
        JsonArray array = new JsonArray();
        for (LokiLogLine log : aLogs) {
            array.add(toLogArrayItem(log));
        }
        return array;
    }

    private static JsonArray toLogArrayItem(LokiLogLine log) {
        JsonArray arrayItem = new JsonArray();
        arrayItem.add(String.valueOf(log.getEpoch()));
        arrayItem.add(log.getMessage());
        arrayItem.add(toMap(log.getMeta()));
        return arrayItem;
    }

    private static JsonObject toMap(Map<String, String> aMeta) {
        JsonObject map = new JsonObject();
        for (Map.Entry<String, String> entry : aMeta.entrySet()) {
            map.addProperty(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
