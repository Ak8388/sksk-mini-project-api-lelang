package jawa.sinaukoding.sk.controller;

import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

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
        
        model.addAttribute("files", storageService.LoadAll().map(path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString()).build().toUri().toString()).collect(Collectors.toList()));

        return "UploadForm";

    }




}
