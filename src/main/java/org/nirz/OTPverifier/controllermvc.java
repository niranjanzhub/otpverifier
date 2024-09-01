package org.nirz.OTPverifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@CrossOrigin
@RequestMapping("/otp")
public class controllermvc {

    @Autowired
    private OTPservice otpService;

    @GetMapping("/generate")
    public String showGenerateForm() {
        return "generate";
    }

    @PostMapping("/generate")
    public RedirectView generateOTP(@RequestParam String email, @RequestParam String phone, HttpServletRequest request) {
        String responseMessage = otpService.generateOTP(email, phone, request).getBody();
        return new RedirectView("/otp/validate?message=" + responseMessage);
    }

    @GetMapping("/validate")
    public ModelAndView showValidateForm(@RequestParam(required = false) String message) {
        return new ModelAndView("validate").addObject("message", message);
    }

    @PostMapping("/validate")
    public ModelAndView validateOTP(@RequestParam String email, @RequestParam String otp) {
        String message = otpService.validateOTP(email, otp) ? "OTP  Verified Succesfully..!" : "Invalid OTP or Expired.";
        return new ModelAndView("validate").addObject("message", message);
    }
}
