package com.bineeta.grpc.client;

import java.io.IOException;
import java.util.stream.Collectors;

import com.bineeta.grpc.client.logic.LogicService;
import com.bineeta.grpc.client.storage.StorageFileNotFoundException;
import com.bineeta.grpc.client.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class FileUploadController {

	private final StorageService storageService;
	private final LogicService logicService;

	@Autowired
	public FileUploadController(StorageService storageService, LogicService logicService) {
		this.storageService = storageService;
		this.logicService = logicService;

	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {
		System.out.println("listUploadedFiles model"+model);
		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));
		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		System.out.println("serveFile filename"+filename);
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		System.out.println("handleFileUpload file"+file);
		storageService.store(file);
		boolean isValid = logicService.isValid(file);
		if(isValid){
			System.out.println("handleFileUpload correct"+file);
			String result = logicService.getResults(file);
			redirectAttributes.addFlashAttribute("message",
					"Matrix Multiplication Complete!");
			//redirectAttributes.addFlashAttribute("message","Final result:"+result);
			storageService.deleteFile(file);

		}else{
			System.out.println("handleFileUpload error"+file);
			redirectAttributes.addFlashAttribute("message",
					"Ooops Error in File. Matrix-Size should be ^2 :" + file.getOriginalFilename() + "!");
			storageService.deleteFile(file);
		}
		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
