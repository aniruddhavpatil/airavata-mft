/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.transport.gdrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;

//import com.google.cloud.storage.Storage;

import com.google.api.services.storage.Storage;


import com.google.api.services.storage.Storage.Objects.Insert;

import com.google.gson.JsonObject;
import org.apache.airavata.mft.secret.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.apache.airavata.mft.resource.service.GDriveResource;
import org.apache.airavata.mft.resource.service.GDriveResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;



public class GDriveSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(GDriveSender.class);

    private GDriveResource gcsResource;
    private Storage storage;
    private JsonObject jsonObject;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.gcsResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gcsSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        //Path of the credentials json is connectionString
//        storage = StorageOptions.newBuilder()
//                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(gcsSecret.getConnectionString())))
//                .build()
//                .getService();
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(gcsSecret.getConnectionString()));
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        storage=new Storage.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting GCS Sender stream for transfer {}", context.getTransferId());
        logger.info("Content length for transfer {} {}", context.getTransferId(), context.getMetadata().getResourceSize());
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(context.getMetadata().getResourceSize());
//        s3Client.putObject(this.s3Resource.getBucketName(), this.s3Resource.getResourcePath(), context.getStreamBuffer().getInputStream(), metadata);
        InputStreamContent contentStream = new InputStreamContent(
                "text/plain", context.getStreamBuffer().getInputStream());
        StorageObject objectMetadata = new StorageObject()
                // Set the destination object name
                .setName("PikaTest.txt")
                // Set the access control list to publicly read-only
                .setAcl(Arrays.asList(new ObjectAccessControl().setEntity("allUsers").setRole("READER")));

        Insert insertRequest = storage.objects().insert(
                gcsResource.getBucketName(), objectMetadata,contentStream);

        insertRequest.execute();

        logger.info("Completed GCS Sender stream for transfer {}", context.getTransferId());
    }
}
