package org.zirbes.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.sqs.model.Message
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.transform.CompileStatic

@CompileStatic
class AwsPlayground {

    ObjectMapper objectMapper = new ObjectMapper()

    AWSCredentials creds = new ProfileCredentialsProvider().credentials

    void run() {
        //new Ec2Demo(creds).getAmiInfo()
        //new SqsDemo(creds).sendSqsMessage()
    }

}
