package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.DeleteQueueRequest
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.transform.CompileStatic

import org.joda.time.LocalTime

@CompileStatic
class SqsDumper {

    ObjectMapper objectMapper = new ObjectMapper()

    private final AWSCredentials creds

    SqsDumper(AWSCredentials creds) {
        this.creds = creds
    }

    void dump() {
        AmazonSQS sqs = new AmazonSQSClient(creds)
        sqs.region = Region.getRegion(Regions.US_WEST_2)

        // Create the queue
        String queueName = "mncc-state"
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
        String queueUrl = sqs.createQueue(createQueueRequest).queueUrl
        println "Using SQS queue: ${queueUrl}"

        // get the message
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
        sqs.receiveMessage(receiveMessageRequest).messages.each{ Message message ->
            println "Received Message id=${message.messageId} md5=${message.getMD5OfBody()}"
            println "Message Body: ${message.body}"

            // Delete the message
            String messageRecieptHandle = message.receiptHandle
            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle))

        }


    }

}
