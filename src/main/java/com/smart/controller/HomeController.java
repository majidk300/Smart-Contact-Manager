package com.smart.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRespository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRespository userRepository;

//	This is Home Controller
	@RequestMapping("/")
	public String home(Model m) {

		m.addAttribute("title", "Home -  Smart Contact Manager");

		return "home";

	}

//	This is About Controller
	@RequestMapping("/about")
	public String about(Model m) {

		m.addAttribute("title", "About -  Smart Contact Manager");

		return "about";

	}

//	This is Signup Controller
	@RequestMapping("/signup")
	public String signup(Model m) {

		m.addAttribute("title", "Register -  Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";

	}

//	This is for register user
	@RequestMapping(value="/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1 , @RequestParam(value="agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
		
		
		try {
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR " + result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			
			if(!agreement)
			{
				System.out.println("You have not agreed terms and condition");
				throw new Exception("You have not agreed terms and condition");
			}
			
		
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agreement"+agreement);
			System.out.println("USER"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Successfully Registered !!","alert-success"));
			
			return "signup";
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
			
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
		
			return "signup";
		}
		
		
		
	}
	
	 //handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title", "Login -  Smart Contact Manager");
		return "login";
	}

}
