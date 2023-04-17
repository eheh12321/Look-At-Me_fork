package com.lookatme.server.file;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.lookatme.server.exception.ErrorCode;
import com.lookatme.server.exception.ErrorLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cdnDomain;

    private final AmazonS3Client amazonS3Client;

    public String upload(MultipartFile multipartFile, FileDirectory dirName) {
        String fileType = imageTypeCheck(multipartFile);
        try {
            File uploadFile = convertMultipartFileToFile(multipartFile)
                    .orElseThrow(() -> new ErrorLogicException(ErrorCode.FILE_CONVERT_FAILED));
            return upload(uploadFile, dirName.getName(), fileType);
        } catch (IOException e) {
            throw new ErrorLogicException(ErrorCode.FILE_CONVERT_FAILED);
        }
    }

    public Set<String> readCsvFile(String fileName) {
        Set<String> set = new HashSet<>();
        String workingDirectory = System.getProperty("user.dir");
        String absoluteFilePath = String.format("%s\\%s.csv", workingDirectory, fileName);
        String line;
        // Try-with-resources (AutoClosable)
        try (
                BufferedReader reader = new BufferedReader(new FileReader(absoluteFilePath))
        ) {
            while ((line = reader.readLine()) != null) {
                set.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    public int writeCsvFile(Set<String> data, String fileName) {
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s.csv", fileName)));
        ) {
            for (String line : data) {
                writer.write(String.format("%s\n", line));
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data.size();
    }

    private String upload(File file, String dirName, String fileType) {
        String fileName = String.format("%s/%s.%s", dirName, UUID.randomUUID(), fileType);
        putS3(file, fileName);
        removeNewFile(file);

        return cdnDomain + fileName;
    }

    public Set<String> getFileNames(String prefix) {
        Set<String> fileNames = new HashSet<>();
        ObjectListing objectListing = amazonS3Client.listObjects(bucket, String.format("%s/", prefix));
        if(objectListing != null) {
            List<S3ObjectSummary> objectSummaryList = objectListing.getObjectSummaries();
            if(!objectSummaryList.isEmpty()) {
                for (S3ObjectSummary summary : objectSummaryList) {
                    fileNames.add(summary.getKey());
                }
            }
        }
        return fileNames;
    }

    private void putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    private void removeNewFile(File targetFile) {
        if (!targetFile.delete()) {
            log.error("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    // 파일 타입이 이미지 타입인지 검증 (image/png, image/jpeg)
    private String imageTypeCheck(MultipartFile file) {
        String fileType = file.getContentType();
        if (!fileType.startsWith("image")) {
            log.error(">> 파일 타입: {}", fileType);
            throw new ErrorLogicException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
        switch (fileType) {
            case "image/png":
                return "png";
            case "image/jpeg":
                return "jpg";
            default:
                throw new ErrorLogicException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }
}
