package com.acme.strilog.loki.api.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class SaveLogEvent {
    long    time;
    String  program;
    Integer facility;
    Integer severity;
    String  message;
}
