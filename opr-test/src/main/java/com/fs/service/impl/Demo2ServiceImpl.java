package com.fs.service.impl;

import org.springframework.stereotype.Service;

import com.fs.service.Demo2Service;

@Service
public class Demo2ServiceImpl implements Demo2Service {

	@Override
	public String two() {
		return "two";
	}

}
