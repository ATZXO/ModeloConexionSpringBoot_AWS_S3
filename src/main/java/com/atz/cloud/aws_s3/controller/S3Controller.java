package com.atz.cloud.aws_s3.controller;

import com.atz.cloud.aws_s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;
    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @PostMapping("/create-bucket")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName) {
        return ResponseEntity.ok(this.s3Service.createBucket(bucketName));
    }

    @GetMapping("/bucket-exists/{bucketName}")
    public ResponseEntity<String> bucketExists(@PathVariable String bucketName) {
        return ResponseEntity.ok(this.s3Service.bucketExists(bucketName));
    }

    @GetMapping("/list-buckets")
    public ResponseEntity<List<String>> listBuckets() {
        return ResponseEntity.ok(this.s3Service.listBuckets());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String bucketName, @RequestParam String key, @RequestPart MultipartFile file) throws IOException {
        try {
            Path staticDir = Paths.get(destinationFolder); //Path de la carpeta donde guardaremos el archivo para subirlo a S3

            if(!Files.exists(staticDir)) {
                Files.createDirectories(staticDir); //Si no existe la carpeta, la creamos
            }

            Path filePath = staticDir.resolve(Objects.requireNonNull(file.getOriginalFilename())); //Path completo agregando el nombre del archivo
            Path finalPath = Files.write(filePath, file.getBytes()); //Guardamos el archivo en la carpeta destino

            Boolean resultUpload = this.s3Service.uploadFile(bucketName, key, filePath); //Subimos el archivo a S3

            if(resultUpload) {
                Files.delete(finalPath); //Eliminamos el archivo de la carpeta destino
                return ResponseEntity.ok("Archivo subido correctamente");
            } else {
                return ResponseEntity.status(500).body("Error al subir el archivo");
            }
        }catch (IOException e){
            throw new IOException(e.getCause());
        }
    }

    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        this.s3Service.downloadFile(bucketName, key);
        return ResponseEntity.ok("Archivo descargado correctamente");
    }

    @PostMapping("/upload/presigned-url")
    public ResponseEntity<String> generatePresignedUploadUrl(@RequestParam String bucketName, @RequestParam String key, @RequestParam Long duration) {
        Duration durationToUse = Duration.ofMinutes(duration);
        return ResponseEntity.ok(this.s3Service.presignedUploadUrl(bucketName, key, durationToUse));
    }

    @PostMapping("/download/presigned-url")
    public ResponseEntity<String> generatePresignedDownloadUrl(@RequestParam String bucketName, @RequestParam String key, @RequestParam Long duration) {
        Duration durationToUse = Duration.ofMinutes(duration);
        return ResponseEntity.ok(this.s3Service.presigedDownloadUrl(bucketName, key, durationToUse));
    }

}
