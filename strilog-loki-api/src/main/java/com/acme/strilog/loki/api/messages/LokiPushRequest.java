package com.acme.strilog.loki.api.messages;

import com.acme.strilog.loki.api.model.LokiLogLine;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class LokiPushRequest {
    Map<String, String> streamMetadata;
    List<LokiLogLine>   logLines;
}
