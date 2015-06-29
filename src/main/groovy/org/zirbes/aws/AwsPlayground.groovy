package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DeregisterImageRequest
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.DescribeImagesResult
import com.amazonaws.services.ec2.model.Filter
import com.amazonaws.services.ec2.model.Image
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.DeleteQueueRequest
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper

import org.joda.time.LocalTime

class AwsPlayground {

    ObjectMapper objectMapper = new ObjectMapper()

    AWSCredentials creds = new ProfileCredentialsProvider().credentials

    void run() {
        //getAmiInfo()
        sendSqsMessage()
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
            println "Received Message id=${message.messageId} md5=${message.mD5OfBody}"

            HappyHour happyHour = objectMapper.readValue(message.body, HappyHour)
            println "HappyHour: ${happyHour.name}, ${happyHour.time}, ${happyHour.place}"

            // Delete the message
            String messageRecieptHandle = message.receiptHandle
            sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle))

        }

        // Delete the queue
        sqs.deleteQueue(new DeleteQueueRequest(queueUrl))

    }

    void getAmiInfo() {

        String IMAGE_NAME = 'my-old-server-1.2.3'
        AmazonEC2 ec2 = new AmazonEC2Client(creds)

        Map<String, String> imageIds = [:]

        Filter filter = new Filter('name', [IMAGE_NAME])
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withFilters(filter)
        DescribeImagesResult describeImagesResult =  ec2.describeImages(describeImagesRequest)
        describeImagesResult.images.each{ Image image ->
            imageIds[image.imageId] = image.name
        }

        imageIds.each{ String imageId, String name ->

            print "Not actually deleting '${imageId}' (${name})..."
            DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest().withImageId(imageId)
            // ec2.deregisterImage(deregisterImageRequest)
            println 'skipped.'
        }


        println 'doing stuff'
    }

}
