package com.smart.controller;

import com.smart.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;


@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;


    Random random = new Random(1000);
//    Email Id form open handler
        @RequestMapping("/forgot")
        public  String  openEmailForm(){
            return "forgot_email_form";
        }

    @PostMapping("/send-otp")
    public  String  sendOTP(@RequestParam("email") String email , HttpSession session){

        System.out.println("EMail  "  + email);

        int otp = random.nextInt(99999);

        System.out.println("OTP  =   "+otp);

//        Write Code for  send otp  to mail

        String subject = "OTP From SCM";

        String message="<H1> OTP = "+otp+" </H1>";

        String to = email;

        boolean flag = this.emailService.sendEmail(subject , message , to);

        if(flag){
            return "verify_otp";

        }else{
            session.setAttribute("message" , "Check Your Email ID");
            return "forgot_email_form";
        }
    }

}
