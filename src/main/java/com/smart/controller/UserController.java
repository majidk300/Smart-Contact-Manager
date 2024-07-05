package com.smart.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRespository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRespository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private MyOrderRepository myOrderRepository;

	// Method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		// get the user using username(email)

		User user = this.userRepository.getUserByUserName(userName);

		System.out.println("USER : " + user);

		model.addAttribute("user", user);

	}

	// Home Dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// Open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {

		model.addAttribute("title", "Add Contact");

		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result2, Model model,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		try {

			if (result2.hasErrors()) {
				System.out.println("Error" + result2.toString());
				model.addAttribute("contact", contact);
				return "normal/add_contact_form";

			}

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);

			// Processing and uploading file...
			if (file.isEmpty()) {

				System.out.println("File is Emty");
				contact.setImage("profile.png");

			} else {
				// file the file to folder and update to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img/").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");
			}

			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("Data " + contact);

			System.out.println("Data : " + contact);

			System.out.println("Added to database");

			// message success....
			session.setAttribute("message", new Message("Your content is added !! Add more...", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR " + e.getMessage());

			// Error message
			session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));

		}

		return "normal/add_contact_form";

	}

	// show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "Show User Contacts");

		// contact list

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 7);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		System.out.println("Cid: " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		// checking user and contact user have same userid
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_details";
	}

	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		Contact contact = this.contactRepository.findById(cId).get();

		if (contact == null) {
			session.setAttribute("message", new Message("Contact not found!", "danger"));
			return "redirect:/user/show-contacts/0";
		}

		// checking user
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		try {
			if (user.getId() == contact.getUser().getId()) {

				System.out.println("Contact " + contact.getcId());

				// Removing Image
				String profileImage = contact.getImage();

				if (profileImage != null && !profileImage.equals("profile.png")) {

					File deleteFile = new ClassPathResource("static/img/").getFile();
					Path path = Paths.get(deleteFile.getAbsolutePath() + File.separator + profileImage);

					if (Files.exists(path)) {
						Files.delete(path);
						System.out.println("Image is deleted");
					} else {
						System.out.println("Image file not found, skipping deletion");
					}

				}

				// deleting contacts
				User users = this.userRepository.getUserByUserName(principal.getName());
				users.getContacts().remove(contact);

				this.userRepository.save(user);

				System.out.println("DELETED");

				session.setAttribute("message", new Message("Contact deleted successfully...", "success"));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR MESSAGE" + e.getMessage());
			session.setAttribute("message", new Message("Error deleting contact: " + e.getMessage(), "danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	// open uodate from handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, Model model,
			@RequestParam("profileImage") MultipartFile file, HttpSession session, Principal principal) {

		try {

			// old contact details
			Contact oldcontactDetails = this.contactRepository.findById(contact.getcId()).get();

			// Image
			if (!file.isEmpty()) {
				// file work
				// image rewrite

				String profileImage = oldcontactDetails.getImage();

				if (profileImage != null && !profileImage.equals("profile.png")) {

					File deleteFile = new ClassPathResource("static/img/").getFile();
					Path path = Paths.get(deleteFile.getAbsolutePath() + File.separator + profileImage);

					if (Files.exists(path)) {
						Files.delete(path);
						System.out.println("Image is deleted");
					} else {
						System.out.println("Image file not found, skipping deletion");
					}

				}

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

				System.out.println("Image is Updated");

			} else {
				contact.setImage(oldcontactDetails.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated...", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error Message for Update " + e.getMessage());
		}

		return "redirect:/user/" + contact.getcId() + "/contact";

	}

	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Your Profile");
		return "normal/profile";

	}

	// open setting handler
	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		System.out.println("OLD PASSWORD : " + oldPassword);
		System.out.println("NEW PASSWORD : " + newPassword);

		String userName = principal.getName();

		User currentUser = this.userRepository.getUserByUserName(userName);

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

			// change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);

			session.setAttribute("message", new Message("Your password is successfully changed", "success"));

		} else {
			session.setAttribute("message", new Message("Please enter correct old password", "danger"));

		}

		return "redirect:/user/index";

	}

	// creating order create for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
		System.out.println("Hey  order function executed !!");
		System.out.println(data);

		int amt = Integer.parseInt(data.get("amount").toString());

		var client = new RazorpayClient("rzp_test_2s4oCvzXkujAeg", "ssoc7aR5OrsOuSrGZACy4KmO");

		JSONObject ob = new JSONObject();

		ob.put("amount", amt * 100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");

		// creating new order
		Order order = client.orders.create(ob);
		System.out.println(order);

		//save the order in database
		MyOrder myOrder = new MyOrder();

		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));

		this.myOrderRepository.save(myOrder);

		return order.toString();
	}

	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data)
	{

		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());

		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());

		this.myOrderRepository.save(myOrder);

		System.out.println(data);

		return ResponseEntity.ok(Map.of("msg","updated"));
	}

}
