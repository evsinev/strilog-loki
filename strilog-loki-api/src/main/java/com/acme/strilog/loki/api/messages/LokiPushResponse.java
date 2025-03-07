package com.acme.strilog.loki.api.messages;

import com.acme.strilog.loki.api.model.LokiPushResponseType;
import com.acme.strilog.loki.api.model.SaveLogsStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class LokiPushResponse {
    LokiPushResponseType type;
    String               errorMessage;
}
