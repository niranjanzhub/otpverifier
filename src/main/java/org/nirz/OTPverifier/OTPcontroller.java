package org.nirz.OTPverifier;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
@CrossOrigin
@RestController
public class OTPcontroller {

	@Autowired
    OTPservice otpService;
    
    @PostMapping("/generate")
    public ResponseEntity<String> generateOTP(@RequestParam String email, @RequestParam String phone , HttpServletRequest request) {
                return otpService.generateOTP(email, phone, request);
    }


    
    @GetMapping("/generate/validate")
    public boolean validateOTP(@RequestParam String email, @RequestParam String otp) {
        return otpService.validateOTP(email, otp);
      
    }
	
	

    
 

	
}
