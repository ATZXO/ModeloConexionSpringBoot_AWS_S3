package com.atz.cloud.aws_s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    @Value("${spring.destination.folder}")
    private String destinationFolder;
    private final S3Presigner s3Presigner;

    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bucketBuilder -> bucketBuilder.bucket(bucketName));
        return "Ubicacion: " + response.location();
    }

    @Override
    public String bucketExists(String bucketName) {
        try {
            this.s3Client.headBucket(headBucketRequest -> headBucketRequest.bucket(bucketName));
            return "El bucket existe";

        }catch (Exception e) {
            return "No existe el bucket";
        }
    }

    @Override
    public List<String> listBuckets() {
        ListBucketsResponse listBuckets = this.s3Client.listBuckets();
        if(listBuckets.hasBuckets()){
            return listBuckets.buckets().stream().map(bucket -> bucket.name()).toList(); //Se pude simplificar a (Bucket::name)
        }
        return List.of();
    }

    @Override
    public Boolean uploadFile(String bucketName, String key, Path filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder() //Request para subir el archivo
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectResponse response = this.s3Client.putObject(putObjectRequest, filePath); //Subimos el archivo

        return response.sdkHttpResponse().isSuccessful();
    }

    @Override
    public void downloadFile(String bucketName, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder() //Request para descargar el archivo
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> responseBytes = this.s3Client.getObjectAsBytes(getObjectRequest); //Descargamos el archivo

        String fileName;
        if(key.contains("/")){ //Si el key tiene una ruta, solo obtenemos el nombre del archivo
            fileName = key.substring(key.lastIndexOf('/'));
        }else {
            fileName = key;
        }

        String destinationFolder = Paths.get(this.destinationFolder, fileName).toString(); //Path completo donde se guardara el archivo

        File file = new File(destinationFolder);
        file.getParentFile().mkdirs(); //Creamos las carpetas si no existen

        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){ //Guardamos el archivo en la carpeta destino
            fileOutputStream.write(responseBytes.asByteArray());
        }catch (IOException e){
            throw new IOException(e.getCause());
        }

    }

    @Override
    public String presignedUploadUrl(String bucketName, String key, Duration duration) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder() //Request para subir el archivo
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder() //Request para firmar la URL
                .signatureDuration(duration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutRequest = this.s3Presigner.presignPutObject(presignRequest); //Generamos la URL firmada

        return presignedPutRequest.url().toString();
    }

    @Override
    public String presigedDownloadUrl(String bucketName, String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder() //Request para descargar el archivo
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder() //Request para firmar la URL
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetRequest = this.s3Presigner.presignGetObject(presignRequest); //Generamos la URL firmada

        return presignedGetRequest.url().toString();
    }
}
