package org.apache.airavata.mft.transport.onedrive;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class OneDriveAuthenticationProvider implements IAuthenticationProvider{
    @Override
    public void authenticateRequest(IHttpRequest iHttpRequest) {
        iHttpRequest.addHeader("Authorization", "Bearer " + System.getenv("ONEDRIVE_ACCESS_TOKEN"));
    }
}