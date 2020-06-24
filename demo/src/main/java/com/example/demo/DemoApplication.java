package com.example.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.demo.model.AwsService;
import com.example.demo.repositories.AwsServiceRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    @Autowired
    private AwsServiceRepository awsServiceRepository;
    String defaultBucketName = "sample-bucket-jagan";
    String defaultBaseFolder = "sample";


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public void uploadFile(String name, byte[] content) {
        File file = new File("/tmp/" + name);
        file.canWrite();
        file.canRead();
        FileOutputStream iofs = null;
        try {
            iofs = new FileOutputStream(file);
            iofs.write(content);
            amazonS3.putObject(defaultBucketName, defaultBaseFolder + "/" + file.getName(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getFile(String key) {
        S3Object obj = amazonS3.getObject(defaultBucketName, defaultBaseFolder + "/" + key);
        S3ObjectInputStream stream = obj.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(stream);
            obj.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(String... args) throws Exception {
        //uploadFile("jagan", "jagan".getBytes());
        //getFile("jagan");
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT);
        CreateTableRequest tableRequest = dynamoDBMapper
                .generateCreateTableRequest(AwsService.class);

        tableRequest.setProvisionedThroughput(
                new ProvisionedThroughput(1L, 1L));

        TableUtils.createTableIfNotExists(amazonDynamoDB, tableRequest);
        AwsService awsService = new AwsService();

        awsService.setServiceName("AWS DynamoDB");
        awsService.setServiceHomePageUrl("https://aws.amazon.com/dynamodb/?nc2=h_m1");

        awsServiceRepository.save(awsService);

        log.info("Saved AwsService object: " + new Gson().toJson(awsService));

        String awsServiceId = awsService.getId();

        log.info("AWS Service ID: " + awsServiceId);

    }

    @GetMapping(value = "hi")
    public String get() {

        Optional<List<AwsService>> awsServiceQueried = awsServiceRepository.findByServiceName("AWS DynamoDB");

        awsServiceQueried.get();
        log.info("Queried object: " + new Gson().toJson(awsServiceQueried.get().get(0)));

        Iterable<AwsService> awsServices = awsServiceRepository.findAll();

        for (AwsService awsServiceObject : awsServices) {
            log.info("List object: " + new Gson().toJson(awsServiceObject));
        }
        return new Gson().toJson(awsServiceQueried.get().get(0));
    }

}