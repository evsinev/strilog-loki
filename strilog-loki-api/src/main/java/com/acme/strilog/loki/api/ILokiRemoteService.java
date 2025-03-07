package com.acme.strilog.loki.api;

import com.acme.strilog.loki.api.messages.LokiPushRequest;
import com.acme.strilog.loki.api.messages.LokiPushResponse;

public interface ILokiRemoteService {

    LokiPushResponse push(LokiPushRequest aLogs);

}
