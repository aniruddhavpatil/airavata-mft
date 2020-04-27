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

package org.apache.airavata.mft.transport.onedrive;

import com.onedrive.storage.blob.BlobClient;
import com.onedrive.storage.blob.BlobContainerClient;
import com.onedrive.storage.blob.BlobServiceClient;
import com.onedrive.storage.blob.BlobServiceClientBuilder;
import com.onedrive.storage.blob.specialized.BlobInputStream;
import com.onedrive.storage.blob.specialized.BlockBlobClient;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.OneDriveResource;
import org.apache.airavata.mft.resource.service.OneDriveResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.OneDriveSecret;
import org.apache.airavata.mft.secret.service.OneDriveSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class OneDriveReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveReceiver.class);

    private boolean initialized = false;
    private OneDriveResource onedriveResource;
    BlobContainerClient containerClient;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        this.initialized = true;

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.onedriveResource = resourceClient.getOneDriveResource(OneDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        OneDriveSecret onedriveSecret = secretClient.getOneDriveSecret(OneDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(onedriveSecret.getConnectionString()).buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(onedriveResource.getContainer());
    }

    @Override
    public void destroy() {

    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("OneDrive Receiver is not initialized");
        }
    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting onedrive receive for remote server for transfer {}", context.getTransferId());
        checkInitialized();
        BlobClient blobClient = containerClient.getBlobClient(onedriveResource.getBlobName());
        BlobInputStream blobInputStream = blobClient.openInputStream();

        OutputStream streamOs = context.getStreamBuffer().getOutputStream();

        long fileSize = context.getMetadata().getResourceSize();

        byte[] buf = new byte[1024];
        while (true) {
            int bufSize = 0;

            if (buf.length < fileSize) {
                bufSize = buf.length;
            } else {
                bufSize = (int) fileSize;
            }
            bufSize = blobInputStream.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            streamOs.write(buf, 0, bufSize);
            streamOs.flush();

            fileSize -= bufSize;
            if (fileSize == 0L)
                break;
        }

        streamOs.close();
        logger.info("Completed onedrive receive for remote server for transfer {}", context.getTransferId());
    }
}
