package org.apache.airavata.mft.secret.server.handler;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GRpcService
public class SecretServiceHandler extends SecretServiceGrpc.SecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SecretServiceHandler.class);

    @Autowired
    private SecretBackend backend;

    @Override
    public void getSCPSecret(SCPSecretGetRequest request, StreamObserver<SCPSecret> responseObserver) {
        try {
            this.backend.getSCPSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No SCP Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });
        } catch (Exception e) {

            logger.error("Error in retrieving SCP Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving SCP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSCPSecret(SCPSecretCreateRequest request, StreamObserver<SCPSecret> responseObserver) {
        responseObserver.onNext(this.backend.createSCPSecret(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateSCPSecret(SCPSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        this.backend.updateSCPSecret(request);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSCPSecret(SCPSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        boolean res = this.backend.deleteSCPSecret(request);
        if (res) {
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to delete SCP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getS3Secret(S3SecretGetRequest request, StreamObserver<S3Secret> responseObserver) {
        try {
            this.backend.getS3Secret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No S3 Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving S3 Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getS3Secret(request, responseObserver);
    }

    @Override
    public void createS3Secret(S3SecretCreateRequest request, StreamObserver<S3Secret> responseObserver) {
        try {
            this.backend.createS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in creating S3 Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating S3 Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3Secret(S3SecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in updating S3 Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteS3Secret(S3SecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in deleting S3 Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getBoxSecret(BoxSecretGetRequest request, StreamObserver<BoxSecret> responseObserver) {
        try {
            this.backend.getBoxSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Box Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving Box Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getBoxSecret(request, responseObserver);
    }

    @Override
    public void createBoxSecret(BoxSecretCreateRequest request, StreamObserver<BoxSecret> responseObserver) {
        try {
            this.backend.createBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in creating Box Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating Box Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBoxSecret(BoxSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in updating Box Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteBoxSecret(BoxSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in deleting Box Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAzureSecret(AzureSecretGetRequest request, StreamObserver<AzureSecret> responseObserver) {
        try {
            this.backend.getAzureSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Azure Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving Azure Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving Azure Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getAzureSecret(request, responseObserver);
    }

    @Override
    public void createAzureSecret(AzureSecretCreateRequest request, StreamObserver<AzureSecret> responseObserver) {
        try {
            this.backend.createAzureSecret(request);
        } catch (Exception e) {
            logger.error("Error in creating Azure Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating Azure Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateAzureSecret(AzureSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateAzureSecret(request);
        } catch (Exception e) {
            logger.error("Error in updating Azure Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating Azure Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteAzureSecret(AzureSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteAzureSecret(request);
        } catch (Exception e) {
            logger.error("Error in deleting Azure Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting Azure Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }


    @Override
    public void getGDriveSecret(GDriveSecretGetRequest request, StreamObserver<GDriveSecret> responseObserver) {
        try {
            this.backend.getGDriveSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No GDRive Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving GDrive Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving GDrive Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getGDriveSecret(request, responseObserver);
    }

    @Override
    public void createGDriveSecret(GDriveSecretCreateRequest request, StreamObserver<GDriveSecret> responseObserver) {
        try {
            this.backend.createGDriveSecret(request);
        } catch (Exception e) {
            logger.error("Error in creating GDrive Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating GDrive Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateGDriveSecret(GDriveSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateGDriveSecret(request);
        } catch (Exception e) {
            logger.error("Error in updating GDrive Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating GDrive Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteGDriveSecret(GDriveSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteGDriveSecret(request);
        } catch (Exception e) {
            logger.error("Error in deleting GDRive Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting GDrive Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

}
