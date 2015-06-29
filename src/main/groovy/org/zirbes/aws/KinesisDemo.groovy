package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.kinesis.producer.Configuration
import com.amazonaws.kinesis.producer.KinesisProducer
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.amazonaws.services.kinesis.clientlibrary.proxies.KinesisProxyFactory
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j

import java.nio.ByteBuffer
import java.time.LocalDateTime

@Slf4j
class KinesisDemo {

    static final String REGION = 'us-east-1'
    static final String STREAM = 'happyhour'
    static final String APPLICATION_NAME = 'kinesis-demo'
    ObjectMapper objectMapper = new ObjectMapper()

    AWSCredentialsProvider credProvider

    KinesisDemo(AWSCredentialsProvider credProvider) {
        this.credProvider = credProvider
    }

    void sendMessageRoundTrip() {

        // Build message
        HappyHour hh = new HappyHour(name: 'Vino & Vinyl', time: LocalDateTime.now().toString(), place: "Grumpy's NE")
        String payload = objectMapper.writeValueAsString(hh)
        String now = LocalDateTime.now().toString()
        ByteBuffer data = ByteBuffer.wrap(payload.getBytes('UTF-8'))

        // Send message
        KinesisProducer producer = new KinesisProducer(configureProducer())
        log.info "publishing record to kinesis"
        producer.addUserRecord(STREAM, now, data)

        // Receive message
        IRecordProcessorFactory recordProcessorFactory = new HappyHourProcessorFactory()
        Worker worker = new Worker(recordProcessorFactory, configureConsumer())
        worker.run()

    }

    static class HappyHourProcessor implements IRecordProcessor {

        String shardId
        ObjectMapper objectMapper = new ObjectMapper()

        @Override
        void initialize(String shardId) {
            this.shardId = shardId
        }

        @Override
        void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
            records.each{ Record record ->
                log.info "Processing record ${record.partitionKey}"
                String data = new String(record.data.array(),'UTF-8')
                HappyHour happyHour = objectMapper.readValue(data, HappyHour)
                println " * HappyHour: ${happyHour.name}, ${happyHour.time}, ${happyHour.place}"

                checkpointer.checkpoint(record)
                println " ✅ check pointed."
            }
        }

        @Override
        void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
            log.warn "Shutting down. ${reason}"
            checkpointer.checkpoint()
        }
    }

    static class HappyHourProcessorFactory implements IRecordProcessorFactory {

        String shardId

        @Override
        IRecordProcessor createProcessor() {
            return new HappyHourProcessor()
        }
    }

    protected Configuration configureProducer() {
        Configuration config = new Configuration()
        config.region = REGION
        config.maxConnections = 1
        config.requestTimeout = 5000
        config.recordMaxBufferedTime = 10000
        return config
    }

    protected KinesisClientLibConfiguration configureConsumer() {
        return new KinesisClientLibConfiguration(
                APPLICATION_NAME,
                STREAM,
                credProvider,
                workerId
        ).withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)
    }

    protected String getWorkerId() {
        String workerId = InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
    }

}
