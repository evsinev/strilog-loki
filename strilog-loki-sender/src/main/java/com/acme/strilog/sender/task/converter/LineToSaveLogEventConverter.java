package com.acme.strilog.sender.task.converter;

import com.acme.strilog.loki.api.model.LokiLogLine;
import com.google.gson.Gson;
import com.acme.strilog.sender.event.LogEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class LineToSaveLogEventConverter implements ILineToItemConverter<LokiLogLine> {

    private static final int INFO_LEVEL = 6;

    private final Gson gson = new Gson();

    @Override
    public LokiLogLine convertToItem(String aLine) {
        LogEvent event = gson.fromJson(aLine, LogEvent.class);
        return mapEvent(event);
    }

    private LokiLogLine mapEvent(LogEvent aEvent) {
        return LokiLogLine.builder()
                .epoch(toNanoEpoch(aEvent.getEpoch()))
                .message(toMessage(aEvent))
                .meta(createMeta(aEvent))
                .build();
    }

    private Map<String, String> createMeta(LogEvent aEvent) {
        MetaMap map = new MetaMap()
          .put("level"             , aEvent.getLevel())
          .put("template"          , aEvent.getTemplate())
          .put("class"             , aEvent.getClazz())
          .put("thread"            , aEvent.getThread())
          .put("app_instance"      , aEvent.getAppInstance())
          .put("app_name"          , aEvent.getAppName())
          .put("exception_line"    , aEvent.getExceptionLine())
          .put("exception_message" , aEvent.getExceptionMessage());

        if (aEvent.getMdc() != null) {
            for (Map.Entry<String, String> entry : aEvent.getMdc().entrySet()) {
                map.put("mdc_" + entry.getKey(), entry.getValue());
            }
        }

        List<String> args = aEvent.getArgs();
        if (args != null) {
            for (int i = 0; i< args.size(); i++) {
                map.put("arg_" + i, args.get(i));
            }
        }

        if (aEvent.getKv() != null) {
            for (Map.Entry<String, String> entry : aEvent.getKv().entrySet()) {
                map.put("kv_" + entry.getKey(), entry.getValue());
            }
        }

        return map.toMap();
    }

    private long toNanoEpoch(long epoch) {
        long nanos = System.nanoTime() % 1_000_000;
        return epoch * 1_000_000L + nanos ;
    }

    private String toMessage(LogEvent aEvent) {
        StringBuilder sb = new StringBuilder();

        sb.append(aEvent.getThread());
        sb.append(' ');
        sb.append(aEvent.getLevel());
        sb.append(' ');
        sb.append(aEvent.getClazz());
        sb.append(' ');

        if (aEvent.getArgs() != null && !aEvent.getArgs().isEmpty()) {
            sb.append(arrayFormat(aEvent.getTemplate(), aEvent.getArgs().toArray(new String[0])).getMessage());
        } else {
            sb.append(aEvent.getTemplate());
        }

        if (aEvent.getExceptionMessage() != null) {
            sb.append(' ');
            sb.append(aEvent.getExceptionMessage());
        }

        if (aEvent.getStacktrace() != null) {
            sb.append('\n');
            sb.append(aEvent.getStacktrace());
        }

        return sb.toString();
    }

}
