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

import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OneDriveMetadataCollector implements MetadataCollector{
    private static final Logger logger = LoggerFactory.getLogger(OneDriveMetadataCollector.class);

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        ArrayList<String> scopes = new ArrayList<String>();
        scopes.add("https://graph.microsoft.com/User.ReadBasic.All");
        scopes.add("https://graph.microsoft.com/Files.ReadWrite");
        scopes.add("https://graph.microsoft.com/Files.Read");
        scopes.add("https://graph.microsoft.com/Files.ReadWrite");

        ClientCredentialProvider authenticationProvider =  new ClientCredentialProvider(
                System.getenv("ONEDRIVE_CLIENT_ID"),
                scopes,
                System.getenv("ONEDRIVE_CLIENT_SECRET"),
                System.getenv("ONEDRIVE_TENANT_GUID"),
                NationalCloud.Global);

        IGraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(authenticationProvider)
                        .buildClient();

        graphClient
                .me()
                .drive()
                .buildRequest()
                .get(new ICallback<Drive>() {
                    @Override
                    public void success(final Drive result) {
                        logger.info("Found Drive" + result.id);
                    }

                    @Override
                    public void failure(ClientException e) {
                        logger.error(e.getMessage());
                    }
                    // Handle failure case
                });
    }

    @Override
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {
        return null;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        return null;
    }
}
