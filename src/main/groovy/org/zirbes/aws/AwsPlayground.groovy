package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.sqs.model.Message
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.transform.CompileStatic

@CompileStatic
class AwsPlayground {

    ObjectMapper objectMapper = new ObjectMapper()

    AWSCredentialsProvider credProvider = new ProfileCredentialsProvider()


    void run() {
        AWSCredentials creds = credProvider.credentials
        //new Ec2Demo(creds).getAmiInfo()
        //new SqsDemo(creds).sendSqsMessage()
        //new KinesisDemo(credProvider).sendMessageRoundTrip()
        new KinesisDumper(credProvider).dump()
    }

}
