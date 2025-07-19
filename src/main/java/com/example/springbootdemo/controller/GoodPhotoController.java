package com.example.springbootdemo.controller;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import com.example.springbootdemo.model.GoodPhoto;
import com.example.springbootdemo.service.GoodPhotoService;





@Controller
public class GoodPhotoController {
	
	private static final List<String> contentTypes = Arrays.asList("image/png","image/jpeg", "image/gif");
	
	@Autowired
	private GoodPhotoService gpService;
	
	@GetMapping("/photos/add")
	public String addPhotos() {
		return "goodphoto/uploadPhotoView";
	}
	
	@PostMapping("/photos/addPost")
	public String addPhotoPost(
			@RequestParam("name") String photoName, 
			@RequestParam("file") MultipartFile photoFile,
			Model model) throws IOException {
		
		
		String fileContentType = photoFile.getContentType();
		
		System.out.println("fileContentType: " + fileContentType);
		
		if(contentTypes.contains(fileContentType)) {
			gpService.saveGoodPhoto(photoName,  photoFile.getBytes());
			
			model.addAttribute("uploadOK", "上傳OK");
		}else {
			model.addAttribute("contentTypeError", "檔案形式不正確，請使用 png, jpeg, gif");
		}
		
		return "goodphoto/uploadPhotoView";
	}
	
	@GetMapping("/photos/download")
	public ResponseEntity<byte[]> downloadPhotos(@RequestParam Integer id) {
		Optional<GoodPhoto> photoOptional = gpService.findGoodPhotoById(id);
		
		if(photoOptional.isPresent()) {
			GoodPhoto goodphoto = photoOptional.get();
			byte[] photobyte = goodphoto.getPhotoFile();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);		
			                                // 要傳回的資料, header , http status code
			return new ResponseEntity<byte[]>(photobyte, headers, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/photos/show")
	public String showPhotos(Model model) {
		
		List<GoodPhoto> goodPhotoList = gpService.findAllGoodPhoto();
		
		model.addAttribute("goodPhotoList", goodPhotoList);
		
		return "goodphoto/showPhotosView";
	}
	
	
	


}
