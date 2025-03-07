package com.acme.strilog.sender.task.batch;

import com.acme.strilog.loki.api.ILokiRemoteService;
import com.acme.strilog.loki.api.messages.LokiPushRequest;
import com.acme.strilog.loki.api.messages.LokiPushResponse;
import com.acme.strilog.loki.api.model.LokiLogLine;
import com.acme.strilog.loki.api.model.LokiPushResponseType;

import java.util.List;
import java.util.Map;

public class BatchSenderClientImpl implements IBatchSenderClient<LokiLogLine> {

    private final ILokiRemoteService lokiRemoteClient;
    private final Map<String, String> streamMeta;

    public BatchSenderClientImpl(ILokiRemoteService lokiRemoteClient, Map<String, String> streamMeta) {
        this.lokiRemoteClient = lokiRemoteClient;
        this.streamMeta       = streamMeta;
    }

    @Override
    public void sendItems(List<LokiLogLine> aItems) {
        LokiPushResponse response;
        try {
            response = lokiRemoteClient.push(LokiPushRequest.builder()
                    .streamMetadata(streamMeta)
                    .logLines(aItems)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot send batch for stream " + streamMeta, e);
        }

        if (response.getType() != LokiPushResponseType.SUCCESS) {
            throw new IllegalStateException("Cannot save logs " + response);
        }

    }
}
