package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File toSave = new File(blob.name);
        saveToFile(blob.inputStream, toSave);

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        Blob blobToReturn = null;
        try {
            Path path = getExistingCoverPath(name);
            InputStream inputStream = Files.newInputStream(path);
            String contentType = new Tika().detect(inputStream);

            blobToReturn = new Blob(name, inputStream,contentType);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(blobToReturn);
    }

    @Override
    public void deleteAll() {

    }

    private void saveToFile(InputStream inputStream, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            byte[] imageBytes = outStream.toByteArray();

            outputStream.write(imageBytes);
        }
    }


    private File getCoverFile(String coverFileName) {
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(String coverFileName) throws URISyntaxException {
        File coverFile = getCoverFile(coverFileName);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}
