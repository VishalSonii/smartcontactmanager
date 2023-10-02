package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;


//	MEthod for Adding Common data to Response
	@ModelAttribute
	public void addCommonData(Model model , Principal principal){
		String userName = principal.getName();
//		System.out.println("USERNAME = " + userName);

		User user = userRepository.getUserByUserName(userName);
//		System.out.println("USER  =  " + user);

		model.addAttribute("user" , user);

	}

//	DASHBOARD HOME
	@RequestMapping("/index")
	public String dashboard(Model model , Principal principal) {
		model.addAttribute("title" , "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model){
		model.addAttribute("title" , "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}


	@PostMapping("/process-contact")    // PHle post mapping thi yha par !!!

//	@GetMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact ,
								 @RequestParam("profileImage") MultipartFile multipartFile ,
								 Principal principal , HttpSession session){

		try {
			String name = principal.getName();

			User user = this.userRepository.getUserByUserName(name);

			if(!multipartFile.isEmpty()){
				contact.setImage(multipartFile.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());

				Files.copy(multipartFile.getInputStream() , path , StandardCopyOption.REPLACE_EXISTING);

				System.out.println("IMAGE IS UPLOADED");
			}else {
//				File is empty here write any message
				System.out.println("FIle is EMPTY !!");
				contact.setImage("contact.png");
			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);			//This will save the data into DB

//			System.out.println("DATA  " + contact);
			System.out.println("DATA  Added to database");

			session.setAttribute("message" , new Message("Contact added , add more !! ", "success"));

		}catch (Exception e){
			System.out.println("Error  "+e.getMessage()  );
			e.printStackTrace();
			session.setAttribute("message" , new Message("Something went wrong, try again !! ", "danger"));

		}

		return  "normal/add_contact_form";
	}


//	SHOW CONTACTS HANDLER


	@GetMapping("/show-contacts/{page}")
	public  String showContacts(@PathVariable("page")Integer page ,   Model m , Principal principal){
		m.addAttribute("title" , "Show User Contacts");

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page , 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId() , pageable);

//		List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());

		m.addAttribute("contacts" , contacts);

//		NEW
		m.addAttribute("currentPage" , page);
		m.addAttribute("totalPages" , contacts.getTotalPages());

		return "normal/show_contacts";
	}

//	Showing Particular Contact details
	@RequestMapping("/{cId}/contact")
	public  String  showContactDetails(@PathVariable("cId") Integer cId , Model model , Principal principal){
		System.out.println("CID  = " +cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

//		THIS IS FOR ACCESSING ONLY THOSE CONTACTS WHO HAVE ADDED BY THE ADMIN FOR PARTICALAR ADMIN
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

//		To match that they have the sameid as per login
		if(user.getId() == contact.getUser().getId()){
		model.addAttribute("contact" , contact);
		model.addAttribute("title" , contact.getName());
		}

		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId , Model model , HttpSession session){

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		contact.setUser(null);

		this.contactRepository.delete(contact);

		session.setAttribute("message" , new Message("Contact Deleted Successfully " , "success"));

		return  "redirect:/user/show-contacts/0";
	}


//	OPEN UPDATE form Handler
	@PostMapping("/update-contact/{cid}")
	public  String updateForm(@PathVariable("cid") Integer cid , Model m){
		m.addAttribute("title" , "Update Contact");

		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact" , contact);

		return "normal/update_form";
	}

	@PostMapping("/process-update")
	public  String updateHandler(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file ,
								 Model m , HttpSession session , Principal principal){

		try{

			Contact oldcontactdetail = this.contactRepository.findById(contact.getcId()).get();

			if(!file.isEmpty()){		//IF IMAGE IS UPDATED
//				file Work Rewrite

//				DELETE OLD PHOTO
				File deletefile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deletefile , oldcontactdetail.getImage());
				file1.delete();


//				UPDATE NEW PHOTO
				File savefile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream() , path , StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			}else{
//				IF IMAGE IS NOT UPDATED
				contact.setImage(oldcontactdetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message" , new Message("Your Contact has been Updated...." , "success"));

		}catch (Exception e){
			e.printStackTrace();
		}


		return "redirect:/user/"+contact.getcId()+"/contact";
	}

//	YOUR  Profile  HANDLER
	@GetMapping("/profile")
	public String yourProfile(Model m){
		m.addAttribute("title" , "Profile Page");
		return "normal/profile";
	}

//	OPEN SETTINGS HANDLER

	@GetMapping("/settings")
	public String openSettings(Model m){
		m.addAttribute("title" , "Setting Page");
		return "normal/settings";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword , @RequestParam("newPassword") String newPassword ,
								 Principal principal , HttpSession session) {
		System.out.println("OldPassword " + oldPassword);
		System.out.println("NewPassword " + newPassword);

		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);

		System.out.println(currentUser.getPassword());

		if(this.bCryptPasswordEncoder.matches(oldPassword , currentUser.getPassword())){
//			Change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message" , new Message("Your Password is successfully Updated...." , "success"));

		}else{
//			Password dont match
			session.setAttribute("message" , new Message("Please enter Correct OLD Password." , "danger"));
			return  "redirect:/user/settings";
		}

		return  "redirect:/user/index";
	}

}
