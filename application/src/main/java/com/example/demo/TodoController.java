package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class TodoController {

	@GetMapping("/hello")
	public String getIndex() {
		return "index";
	}
}
