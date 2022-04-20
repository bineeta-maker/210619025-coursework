package com.bineeta.grpc.client;

import java.io.IOException;
import java.util.stream.Collectors;

import com.bineeta.grpc.client.logic.MultiplicationService;
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
	private final MultiplicationService multiplicationService;

	@Autowired
	public FileUploadController(StorageService storageService, MultiplicationService multiplicationService) {
		this.storageService = storageService;
		this.multiplicationService = multiplicationService;

	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {
		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));
		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("deadline") Double deadline,
			RedirectAttributes redirectAttributes) {
		storageService.store(file);
		boolean isValid = multiplicationService.isValid(file);
		// This system can take both sync and async call. boolean set to test out both.
		boolean synchronousCall = Boolean.TRUE;
		if(isValid){
			multiplicationService.getResults(file,deadline,synchronousCall);
			redirectAttributes.addFlashAttribute("message",
					"Matrix Multiplication Complete!");
			storageService.deleteFile(file);

		}else{
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
