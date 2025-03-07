package com.acme.strilog.loki.api.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class LokiLogLine {
    long                epoch;
    String              message;
    Map<String, String> meta;
}
