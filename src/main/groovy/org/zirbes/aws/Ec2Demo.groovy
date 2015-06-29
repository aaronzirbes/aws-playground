package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DeregisterImageRequest
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.DescribeImagesResult
import com.amazonaws.services.ec2.model.Filter
import com.amazonaws.services.ec2.model.Image

import groovy.transform.CompileStatic

@CompileStatic
class Ec2Demo {

    private final AWSCredentials creds

    Ec2Demo(AWSCredentials creds) {
        this.creds = creds
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

