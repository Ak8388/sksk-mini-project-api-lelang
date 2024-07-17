package jawa.sinaukoding.sk.controller;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import jawa.sinaukoding.sk.service.FileSystemStorageService;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private final FileSystemStorageService storageService;

    public FileUploadController(FileSystemStorageService fileSystemStorageService){

        this.storageService = fileSystemStorageService;

    }
    
    @GetMapping({"","/"})
    public String ListUploadFile(Model model) throws IOException {
        
        model.addAttribute("files", storageService.LoadAll()
        .map(path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class, "serveFile", path.getFileName()
        .toString()).build().toUri().toString()).collect(Collectors.toList()));

        return "UploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename){
        Resource file = storageService.loadAsResource(filename);

        if(file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachnent: filename=\""+file.getFilename()+"\"").body(file);
    }


}
