package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DeregisterImageRequest
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.DescribeImagesResult
import com.amazonaws.services.ec2.model.Filter
import com.amazonaws.services.ec2.model.Image

class AwsPlayground {

    void run() {

        String IMAGE_NAME = 'com.peoplenet.vehicle-entity-service-0.0.7-SNAPSHOT'

        String accessKey = System.getenv('AWS_ACCESS_KEY')
        String secretKey = System.getenv('AWS_SECRET_KEY')

        AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey)

        AmazonEC2 ec2 = new AmazonEC2Client(creds)


        Map<String, String> imageIds = [:]

        Filter filter = new Filter('name', [IMAGE_NAME])
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withFilters(filter)
        DescribeImagesResult describeImagesResult =  ec2.describeImages(describeImagesRequest)
        describeImagesResult.images.each{ Image image ->
            imageIds[image.imageId] = image.name
        }

        imageIds.each{ String imageId, String name ->

            print "Deleting '${imageId}' (${name})..."
            DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest().withImageId(imageId)
            ec2.deregisterImage(deregisterImageRequest)
            println 'done.'
        }


        println 'doing stuff'
    }

}
