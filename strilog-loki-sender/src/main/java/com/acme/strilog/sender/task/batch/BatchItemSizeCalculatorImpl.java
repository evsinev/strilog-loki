package com.acme.strilog.sender.task.batch;

import com.acme.strilog.loki.api.model.LokiLogLine;

public class BatchItemSizeCalculatorImpl implements IBatchItemSizeCalculator<LokiLogLine> {

    @Override
    public long sizeOfItem(LokiLogLine aItem) {
        return    aItem.getMessage().length() * 2L;
    }
}
