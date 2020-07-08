package com.fs.service.impl;

import org.springframework.stereotype.Service;

import com.fs.service.Demo1Service;

@Service
public class Demo1ServiceImpl implements Demo1Service {

	@Override
	public String one() {
		return "one";
	}

}
