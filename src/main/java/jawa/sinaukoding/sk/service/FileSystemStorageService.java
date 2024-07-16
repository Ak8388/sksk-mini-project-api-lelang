package jawa.sinaukoding.sk.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jawa.sinaukoding.sk.exception.CustomeException1;

@Service
public class FileSystemStorageService {

    private Path rootLocation;

    public FileSystemStorageService (Environment environment){
        this.rootLocation = Paths.get("/asep");
        
    }

    public void Store(MultipartFile multipartfile ){
        try {
            if (multipartfile.isEmpty()){
                throw new CustomeException1("file is empty");
            }
            Path desnitationPath = this.rootLocation.resolve(Paths.get(multipartfile.getOriginalFilename())).normalize().toAbsolutePath();
            if (!desnitationPath.getParent().equals(this.rootLocation.toAbsolutePath())){
                throw new CustomeException1("cannot store file outside current directory");
            }
            try(InputStream inputstream = multipartfile.getInputStream()){
                Files.copy(inputstream,desnitationPath,StandardCopyOption.REPLACE_EXISTING);
            }
            
        } catch (IOException e) {
            throw new CustomeException1("cuk!");
        }
    }

    public Stream<Path> LoadAll (){
        try {
            return Files.walk(this.rootLocation, 1)
            .filter(path -> !path.equals(this.rootLocation))
            .map(this.rootLocation::relativize);

        } catch (IOException e) {
            throw new CustomeException1("failed read stored files" + e.getMessage());
        }

    }

    public Path Load(String filename){
        return rootLocation.resolve(filename);
    }


}
