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
class KinesisDumper {

    static final String REGION = 'us-east-1'
    static final String STREAM = 'yolo'
    static final String APPLICATION_NAME = 'kinesis-dumper'

    AWSCredentialsProvider credProvider

    KinesisDumper(AWSCredentialsProvider credProvider) {
        this.credProvider = credProvider
    }

    void dump() {
        // Receive message
        IRecordProcessorFactory recordProcessorFactory = new KinesisProcessorFactory()
        Worker worker = new Worker(recordProcessorFactory, configureConsumer())
        worker.run()

    }

    static class KinesisDataProcessor implements IRecordProcessor {

        String shardId

        @Override
        void initialize(String shardId) {
            this.shardId = shardId
        }

        @Override
        void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
            records.each{ Record record ->
                log.info "Processing record=${record.partitionKey} from shard=${shardId}"
                String data = new String(record.data.array(),'UTF-8')

                checkpointer.checkpoint(record)
                println "data returned: ${data}"
            }
        }

        @Override
        void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
            log.warn "Shutting down. ${reason}"
            checkpointer.checkpoint()
        }
    }

    static class KinesisProcessorFactory implements IRecordProcessorFactory {
        @Override
        IRecordProcessor createProcessor() {
            return new KinesisDataProcessor()
        }
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
