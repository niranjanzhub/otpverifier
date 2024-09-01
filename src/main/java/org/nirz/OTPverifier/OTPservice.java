package org.nirz.OTPverifier;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
@Service
public class OTPservice {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;
    
    @Value("${twilio.phone.whatsapp}")
    private String fromWhatsappNumber;
    
    @Value("${otptimer}")
    private int otptimer;
	
	private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public ResponseEntity<String> generateOTP(String email, String phone, HttpServletRequest r) {
		
		 if (email == null || email.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email address.");
	        }
		String otp = String.format("%04d", new SecureRandom().nextInt(10000));
		
		sendMail(email,otp,r);
		
		if (phone.length() == 10) {
		sendSms(email, phone, otp);
		sendWhatsapp(email, phone, otp);
		}
		otpStorage.put(email, otp);
		System.out.println(otpStorage);
		
		scheduler.schedule(() -> {	otpStorage.remove(email);  System.out.println("OTP expired for email: " + email);
        }, otptimer, TimeUnit.SECONDS);
		
		
		
		
		return ResponseEntity.status(HttpStatus.OK).body("OTP generated succesfully");
	}

	
	
	
	public boolean validateOTP(String email, String otp) {
		
		return otp.equals(otpStorage.get(email));
	}
	
	
	
	private void sendMail(String email, String otp, HttpServletRequest r) {
		String link = "\"" + r.getRequestURL() + "/validate?email=" + email + "&otp=" + otp + "\"";

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(email);
			helper.setText("<h1>" + otp + "</h1> <br> <a href=" + link + ">verify</a>", true);
			
			helper.setSubject("OTP");
			javaMailSender.send(message);
		} catch (SendFailedException e) {
			e.getStackTrace();

		} catch (MessagingException e) {
			e.getStackTrace();
		}catch (Exception e) {
			e.getStackTrace();
		}

	}
	
	private void sendSms(String email, String phone, String otp) {
		Twilio.init(accountSid, authToken);

		try {
			Message message = Message.creator(new PhoneNumber("+91" + phone), // Receiver
					new PhoneNumber(fromPhoneNumber), // Sender
					"otp for your " + email + " is: " + otp).create();

			System.out.println("SMS sent successfully. SID: " + message.getSid());
		} catch (ApiException e) {

			System.err.println("Failed to send SMS: " + e.getMessage());
			// Additional logic to handle specific API errors if necessary
		} catch (TwilioException e) {
			System.err.println("Error sending SMS: " + e.getMessage());
			// Handle other Twilio errors
		} catch (Exception e) {
			System.err.println("General error occurred: " + e.getMessage());
			// Handle unexpected errors
		}
	}

	private void sendWhatsapp(String email, String phone, String otp) {
		Twilio.init(accountSid, authToken);

		try {
			Message message = Message.creator(new PhoneNumber("whatsapp:+91" + phone), // Receiver
					new PhoneNumber(fromWhatsappNumber), // Sender
					"otp for your " + email + " is: " + otp).create();

			System.out.println("WhatsApp message sent successfully. SID: " + message.getSid());
		} catch (ApiException e) {
			System.err.println("Failed to send WhatsApp message: " + e.getMessage());
			// Handle specific API errors
		} catch (TwilioException e) {
			System.err.println("Error sending WhatsApp message: " + e.getMessage());
			// Handle other Twilio errors
		} catch (Exception e) {
			System.err.println("General error occurred: " + e.getMessage());
			// Handle unexpected errors
		}
	}
	

}
