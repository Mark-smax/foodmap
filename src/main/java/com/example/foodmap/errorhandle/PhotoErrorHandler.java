package com.example.foodmap.errorhandle;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice //錯誤處理Controller
public class PhotoErrorHandler {
	
	@ExceptionHandler(value=MaxUploadSizeExceededException.class)
	public String photosUploadSizeErrorHandler(Model model) {
		model.addAttribute("uploadSizeError","檔案太大請重新上傳");
		return "goodphoto/uploadPhotoView";
	}

}
