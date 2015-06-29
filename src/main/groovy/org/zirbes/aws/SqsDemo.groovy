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
class SqsDemo {

    ObjectMapper objectMapper = new ObjectMapper()

    private final AWSCredentials creds

    SqsDemo(AWSCredentials creds) {
        this.creds = creds
    }

    void sendSqsMessage() {
        AmazonSQS sqs = new AmazonSQSClient(creds)
        sqs.region = Region.getRegion(Regions.US_WEST_2)

        // Create the queue
        String queueName = "happyhour-${LocalTime.now().secondOfMinute}"
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
        String queueUrl = sqs.createQueue(createQueueRequest).queueUrl
        println "Created SQS queue: ${queueUrl}"

        // Send the message
        HappyHour hh = new HappyHour(name: 'Vino & Vinyl', time: 'Thursday', place: "Grumpy's NE")
        String payload = objectMapper.writeValueAsString(hh)
        sqs.sendMessage(new SendMessageRequest(queueUrl, payload))

        // get the message
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
        sqs.receiveMessage(receiveMessageRequest).messages.each{ Message message ->
            println "Received Message id=${message.messageId} md5=${message.getMD5OfBody()}"

            HappyHour happyHour = objectMapper.readValue(message.body, HappyHour)
            println "HappyHour: ${happyHour.name}, ${happyHour.time}, ${happyHour.place}"

            // Delete the message
            String messageRecieptHandle = message.receiptHandle
            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle))

        }

        // Delete the queue
        sqs.deleteQueue(new DeleteQueueRequest(queueUrl))

    }

}
