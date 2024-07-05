package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRespository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    // generate otp of 4 digit
    Random random = new Random(1000);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // email id form open handler
    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session) {
        System.out.println("EMAIL : " + email);

        int otp = random.nextInt(999999);

        System.out.println("OTP : " + otp);

        // write code for send otp

        String subject = "OTP From SCM";
        String message = ""
                + "<div style='border:1px solid #e2e2e2; padding:20px'>"
                + "<h1>"
                + "OTP is"
                + "<b> " + otp
                + "</b>"
                + "</h1>"
                + "</div>";
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if (flag) {

            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);

            return "verify_otp";

        } else {
            session.setAttribute("message", "check your email id !!");
            return "forgot_email_form";

        }

    }

    // varify otp
    @PostMapping("/varify-otp")
    public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {

        int myOtp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");

        if (myOtp == otp) {

            User user = this.userRespository.getUserByUserName(email);

            if (user == null) {

                // send error message
                session.setAttribute("message", "User doest not exist !!");
                return "forgot_email_form";

            } else {

                // send change password form


            }

            // password change form
            return "password_change_form";
        } else {

            session.setAttribute("message", "You have entered wrong otp");
            return "verify_otp";
        }

    }

    // change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {

        String email = (String) session.getAttribute("email");

        User user = this.userRespository.getUserByUserName(email);

        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));

        this.userRespository.save(user);

        return "redirect:/signin?change=password change successfully...";

        
    }

}
