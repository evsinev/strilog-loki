package com.acme.strilog.sender;


import com.acme.strilog.loki.api.impl.LokiRemoteClientImpl;
import com.acme.strilog.loki.api.model.LokiLogLine;
import com.acme.strilog.sender.config.ConfigLoader;
import com.acme.strilog.sender.config.TSenderConfig;
import com.acme.strilog.sender.config.TSenderDir;
import com.acme.strilog.sender.offset.impl.OffsetStoreImpl;
import com.acme.strilog.sender.task.batch.BatchItemSizeCalculatorImpl;
import com.acme.strilog.sender.task.batch.BatchSender;
import com.acme.strilog.sender.task.batch.BatchSenderClientImpl;
import com.acme.strilog.sender.task.converter.LineToSaveLogEventConverter;
import com.acme.strilog.sender.task.file.FileSenderImpl;
import com.acme.strilog.sender.task.dir.DirSenderTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.acme.strilog.sender.config.BasicAuths.createBasicAuthValue;
import static com.payneteasy.startup.parameters.StartupParametersFactory.getStartupParameters;

public class SenderApplication {

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        IStartupConfig config = getStartupParameters(IStartupConfig.class);

        TSenderConfig senderConfig = new ConfigLoader().loadConfig(config.senderConfigFile());

        //noinspection resource
        ExecutorService executor = Executors.newFixedThreadPool(senderConfig.getDirs().size());

        BatchItemSizeCalculatorImpl batchItemSizeCalculator = new BatchItemSizeCalculatorImpl();

        Logger log = LoggerFactory.getLogger( SenderApplication.class );

        for (TSenderDir dirConfig : senderConfig.getDirs()) {
            log.info("Registering dir {} ...", dirConfig);

            File dir = new File(dirConfig.getPath());

            BatchSenderClientImpl batchSenderClient = new BatchSenderClientImpl(
                    new LokiRemoteClientImpl(
                            config.lokiPushUrl()
                            , createBasicAuthValue(config.lokiAuthUsername(), config.lokiAuthPassword())
                    )
                    , dirConfig.getStream()
            );

            BatchSender<LokiLogLine> batchSender = new BatchSender<>(
                      new LineToSaveLogEventConverter()
                    , batchSenderClient
                    , batchItemSizeCalculator
                    , config.maxBatchItems()
                    , config.maxBatchSize()
                    , config.batchErrorSleep()
            );

            executor.execute(new DirSenderTask(
                    dirConfig
                    , new FileSenderImpl(
                        new OffsetStoreImpl(dir)
                        , batchSender
                    )
                    , config.dirSleep()
                    , config.dirDetectOldFiles()
            ));
        }


        executor.shutdown();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown ...");
            executor.shutdownNow();
        }));
    }
}
