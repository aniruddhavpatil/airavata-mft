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
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;

import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.*;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.*;

import java.io.FileInputStream;
import java.util.Collection;

public class GDriveMetadataCollector implements MetadataCollector {

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("GDrive Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        GDriveResource gcsResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gcsSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        //Path of the credentials json is connectionString
//        Storage storage = (Storage) StorageOptions.newBuilder()
//                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(gcsSecret.getConnectionString())))
//                .build()
//                .getService();
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(gcsSecret.getConnectionString()),transport,jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        Storage storage=new Storage.Builder(transport, jsonFactory, credential).build();

        ResourceMetadata metadata = new ResourceMetadata();
        StorageObject gcsMetadata = storage.objects().get(gcsResource.getBucketName(),"PikaTest.txt").execute();
        metadata.setResourceSize(gcsMetadata.getSize().longValue());
        metadata.setMd5sum(gcsMetadata.getEtag());
        metadata.setUpdateTime(gcsMetadata.getTimeStorageClassUpdated().getValue());
        metadata.setCreatedTime(gcsMetadata.getTimeCreated().getValue());
        return metadata;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        GDriveResource gcsResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gcsSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        //Path of the credentials json is connectionString
//        Storage storage = (Storage) StorageOptions.newBuilder()
//                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(gcsSecret.getConnectionString())))
//                .build()
//                .getService();
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(gcsSecret.getConnectionString()), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        Storage storage = new Storage.Builder(transport, jsonFactory, credential).build();
        return !storage.objects().get(gcsResource.getBucketName(), "PikaTest.txt").execute().isEmpty();

    }
}
