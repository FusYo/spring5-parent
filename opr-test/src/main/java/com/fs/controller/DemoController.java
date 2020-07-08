package com.fs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fs.service.Demo1Service;
import com.fs.service.Demo2Service;

@RestController
@RequestMapping("/demo")
public class DemoController {
	
//	@Autowired
//	private Demo1Service demo1Service;
//	@Autowired
//	private Demo2Service demo2Service;


	@RequestMapping("/hello")
	public String hello() {
		return "hello spring yuanma";
	}
}
