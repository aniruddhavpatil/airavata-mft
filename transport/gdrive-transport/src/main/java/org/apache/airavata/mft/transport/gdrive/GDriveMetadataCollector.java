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
//import com.google.api.services.storage.Storage;
//import com.google.api.services.storage.StorageScopes;
//import com.google.api.services.storage.model.StorageObject;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;

import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.storage.Storage;
//import com.google.api.services.storage.StorageScopes;
//import com.google.api.services.storage.model.StorageObject;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.*;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Collection;

public class GDriveMetadataCollector implements MetadataCollector {

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;
   // private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
     //       "https://www.googleapis.com/auth/drive.install");
   private static final Logger logger = LoggerFactory.getLogger(GDriveMetadataCollector.class);

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
        GDriveResource gdriveResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gdriveSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());



        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString=gdriveSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes =  DriveScopes.all();
                    //Arrays.asList(DriveScopes.DRIVE,"https://www.googleapis.com/auth/drive");
           credential = credential.createScoped(scopes);

        }

//        Storage storage=new Storage.Builder(transport, jsonFactory, credential).build();
        Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("NsaMft").build();


        logger.info("Listing files in GDRIVEMETADATACOLLECTOR "+drive.files().list().setFields("files(id,name,modifiedTime,md5Checksum)").execute());
//
       ResourceMetadata metadata = new ResourceMetadata();

       //logger.info("BE AWARE FILE ID IS "+drive.files().get("1LKSXadWP_ZbJxbINP_Iy3BGqf8avwBH0").execute());
       logger.info("BE AWARE CHECK gdriveResource.getResourcePath()"+gdriveResource.getResourcePath());



       FileList fileList=drive.files().list().setFields("files(id,name,modifiedTime,md5Checksum,size)").execute();
       logger.info("File data is as follows##########################################");
      for (File f:fileList.getFiles()){
           if(f.getName().equalsIgnoreCase(gdriveResource.getResourcePath())){

               logger.info("!!!!!!!!!!! I GOT THE FILE!!!!!! with GDRIVE credentials"+f.getName()+ " AND "+ gdriveResource.getResourcePath());

               //String md5Sum = String.format("%032x", new BigInteger(1, Base64.getDecoder().decode(f.getMd5Checksum())));
               metadata.setMd5sum(f.getMd5Checksum());
               metadata.setUpdateTime(f.getModifiedTime().getValue());
               metadata.setResourceSize(f.getSize().longValue());
              // metadata.setCreatedTime(f.getCreatedTime().getValue());
           }
       }




      // File file=drive.files().get(gdriveResource.getResourceId()).execute();        //get the metada of file

//        StorageObject gcsMetadata = storage.objects().get(gcsResource.getBucketName(),"PikaTest.txt").execute();
      // metadata.setResourceSize(gcsMetadata.getSize().longValue());
//        metadata.setMd5sum(gcsMetadata.getEtag());
//        metadata.setUpdateTime(gcsMetadata.getTimeStorageClassUpdated().getValue());
//        metadata.setCreatedTime(gcsMetadata.getTimeCreated().getValue());
        //metadata.setResourceSize(new Long(10));

        return metadata;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        GDriveResource gdriveResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gdriveSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        //Path of the credentials json is connectionString
//        Storage storage = (Storage) StorageOptions.newBuilder()
//                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(gcsSecret.getConnectionString())))
//                .build()
//                .getService();


        logger.info("Inside GDRiveMetadata is available()");
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString=gdriveSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes =  DriveScopes.all();
            //Arrays.asList(DriveScopes.DRIVE,"https://www.googleapis.com/auth/drive");
            credential = credential.createScoped(scopes);

        }

        Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("NsaMft").build();
       // Storage storage = new Storage.Builder(transport, jsonFactory, credential).build();
        logger.info("Before getting resource");
        String id=null;
       // logger.info("Before is available return : " +drive.files().get(gdriveResource.getResourcePath()).execute());
        FileList fileList=drive.files().list().setFields("files(id,name)").execute();
        logger.info("gdriveResource.getResourcePath() " +gdriveResource.getResourcePath());
        logger.info("Listing files in GDRIVEMETADATACOLLECTOR "+drive.files().list().setFields("files(id,name)").execute());
        for (File f:fileList.getFiles()) {
            if(f.getName().equalsIgnoreCase(gdriveResource.getResourcePath())){
                logger.info("File matched in receiver"+f.getName());
                id = f.getId();
                return !drive.files().get(id).execute().isEmpty();
            }

        }

        return false;

    }
}
