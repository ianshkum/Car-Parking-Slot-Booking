package com.Park.controllers;

import com.Park.config.SmsSender;
import com.Park.controllers.dto.UserRegistrationDto;
import com.Park.entities.User;
import com.Park.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Random;
class Generator {
    public static String generateRandomPassword(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                +"lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
@Controller
@RequestMapping("/registration")
public class UserRegisterController {

    @Autowired
    private EmailController emailService;

    @Autowired
    SmsSender sms;

    @Autowired
    private UserService userService;

    @ModelAttribute("user")
    public UserRegistrationDto userRegistrationDto() {

        return new UserRegistrationDto();
    }
    Generator obj=new Generator();
    public String Otp= obj.generateRandomPassword(8);

    @GetMapping
    public String showRegistrationForm(Model theModel) {

        UserRegistrationDto usertDto = new UserRegistrationDto();
        if(!theModel.containsAttribute("user")){
            theModel.addAttribute("user", usertDto);
        }
        return "registration";
    }
    @PostMapping("/admin")
    public String registerWWorkerAccount(@ModelAttribute("user") @Valid UserRegistrationDto user,
                                         BindingResult result, RedirectAttributes attr,Model model) {



        User existing = userService.findByUsername(user.getUserName());
        if (existing != null) {

            return "/ADM/registerworker";
        }
        if (result.hasErrors()) {

            attr.addFlashAttribute("org.springframework.validation.BindingResult.user",
                    result);
            attr.addFlashAttribute("user", user);
            return "/ADM/registerworker";
        }
        userService.save(user);
        return "redirect:/admin";



    }
    @PostMapping
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto user,@RequestParam(value="otp" , defaultValue = "")String otp,
                                      BindingResult result, RedirectAttributes attr,Model model) {


        System.out.println(otp);
        System.out.println(Otp);
        User existing = userService.findByUsername(user.getUserName());
        if (existing != null) {

            return "errorRegistrationPage";
        }
        if (result.hasErrors()) {

            attr.addFlashAttribute("org.springframework.validation.BindingResult.user",
                    result);
            attr.addFlashAttribute("user", user);
            return "redirect:/registration";
        }
        if (!Otp.equalsIgnoreCase(otp)) {
            System.out.println(otp);
            System.out.println(Otp);
            emailService.sendOtp(user.getEmail(), user.getUserName(), Otp);

            return "registration";
        }
        System.out.println(otp);
        System.out.println(Otp);

        if (otp.equalsIgnoreCase(Otp)) {
            System.out.println(otp);
            System.out.println(Otp);
            try {

                emailService.sendHelloEmail(user.getEmail(), user.getUserName());

            } catch (Exception e) {
                e.printStackTrace();
            }
            userService.save(user);

            sms.sendSms("Welcome to ParkingPro ", user.getPhone());

            return "redirect:/login";



        }
        else {
            return "redirect:/registration";
        }



    }

}