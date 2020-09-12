package com.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fs.service.Demo1Service;
import com.fs.service.Demo2Service;

@Service
public class Demo2ServiceImpl implements Demo2Service {
	
	@Autowired
	private Demo1Service demo1Service;

	@Override
	public String two() {
		return "two";
	}

}
