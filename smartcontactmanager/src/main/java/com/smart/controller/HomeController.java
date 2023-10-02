package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	UserRepository userRepository;
	
	

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title" , "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title" , "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title" , "Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
//	HANDLER for registering user
	@RequestMapping(value = "/do_register" , method= RequestMethod.POST)
	public String registerUser(@ModelAttribute("user")User user , @RequestParam(value = "agreement" ,defaultValue="false")boolean agreement , 
			Model model , HttpSession session) {
		
		try 
		{
			if(!agreement) {
				System.out.println("Not agreed T&C");
				throw new Exception("Not agreed T&C");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			
			System.out.println("Agreement  "+agreement );
			System.out.println("User  "+user);
			
			User result =  this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			model.addAttribute("message", new Message("Successfully registered !!" , "alert-success" ));
			
			session.setAttribute("message", new Message("Successfully registered !!" , "alert-success" ));
//			model.addAttribute("session",session);
			return "signup";
			
		} 
		catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went Wrong !! "+e.getMessage() , "alert-danger" ));
			return "signup";
		}
	}

// Handler for customer Login

	@GetMapping("/signin")
	public  String customLogin(Model model){
		model.addAttribute("title" , "Login Page");
		return "login";
	}


}
