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

package org.apache.airavata.mft.onedrive.transport;

import com.microsoft.graph.auth.enums.NationalCloud;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;

import java.util.*;

public class OneDriveMetadataCollector implements MetadataCollector{
    ArrayList<String> scopes = new ArrayList<String>();
    scopes.add("https://graph.microsoft.com/User.ReadBasic.All");
//            "https://graph.microsoft.com/Files.ReadWrite",
//            "https://graph.microsoft.com/Files.Read",
//            "https://graph.microsoft.com/Files.ReadWrite"

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        ClientCredentialProvider authProvider = new ClientCredentialProvider(
                System.getenv("ONEDRIVE_CLIENT_ID"),
                Collections.singletonList(SCOPES),
                System.getenv("ONEDRIVE_CLIENT_SECRET"),
                System.getenv("ONEDRIVE_TENANT_GUID"),
                NationalCloud.Global);
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
