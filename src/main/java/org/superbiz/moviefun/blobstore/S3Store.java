package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client s3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);

        PutObjectResult putObjectResult = s3Client.putObject(
                photoStorageBucket,
                blob.name,
                blob.inputStream,
                objectMetadata);

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

        S3Object s3Object = s3Client.getObject(photoStorageBucket, name);

        Blob blob = new Blob(
                name,
                s3Object.getObjectContent(),
                s3Object.getObjectMetadata().getContentType()
        );

        return Optional.ofNullable(blob);
    }

    @Override
    public void deleteAll() {

        ObjectListing objectListing = s3Client.listObjects(photoStorageBucket);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                s3Client.deleteObject(photoStorageBucket, objIter.next().getKey());
            }

            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

    }
}
