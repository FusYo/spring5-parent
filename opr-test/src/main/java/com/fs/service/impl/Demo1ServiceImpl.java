package com.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fs.service.Demo1Service;
import com.fs.service.Demo2Service;

@Service
public class Demo1ServiceImpl implements Demo1Service {
	
	@Autowired
	private Demo2Service demo2Service;

	@Override
	public String one() {
		System.out.println("正在执行被代理方法。。。");
		return "one";
	}

}
