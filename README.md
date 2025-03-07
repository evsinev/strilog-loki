## Overview

The app sends log messages from strilog-encoder to grafana loki

* strilog-encoder https://github.com/evsinev/strilog-encoder

### How it works

* we write logs to a json file from our strilog-encoder plus file roller every minute a new file
* as soon as two files or more appear in the directory, we take the one that was previous
* convert from one message format to a message for loki
* we send several messages in batch
* since we don’t care about the delay here, we have 1 minute of messages in our queue
* if there no more than 1 file for more than 3 minutes, then we send one last file
* the log file is then deleted after successful sending

## Env variables

```shell
BATCH_ERROR_SLEEP            = PT1S
BATCH_MAX_BYTES              = 1000000
BATCH_MAX_ITEMS              = 1000
CONFIG_FILE                  = ./config.yaml
DIR_DETECT_OLD_FILES         = PT3M
DIR_SLEEP_BETWEEN_LIST_FILES = PT1S
LOKI_AUTH_USERNAME           = username
LOKI_AUTH_PASSWORD           = password
LOKI_PUSH_URL                = http://localhost:3100/loki/api/v1/push
```
## Config example

./config.yaml

```yaml

dirs:
  - path: /opt/app-1/json-logs
    stream:
      service_name: app-name
      service_kind: test
      service_host: host-1
      service_instance: app-name-test-1

  - path: /opt/app-2-1/json-logs
    stream:
      service_name: app-name
      service_kind: production
      service_host: host-1
      service_instance: app-name-production-1
```
    
## How to configure logback.xml for your app

```xml
<appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <encoder class="com.payneteasy.strilog.encoder.json.JsonEncoder" />

    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>/var/log/app-1-json/json-%d{yyyy-MM-dd-HH-mm}-%3i.json</fileNamePattern>
        <maxHistory>30</maxHistory>
        <maxFileSize>10MB</maxFileSize>
        <totalSizeCap>3GB</totalSizeCap>
    </rollingPolicy>

</appender>
```
