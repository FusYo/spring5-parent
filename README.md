# spring5-parent
spring5源码分析，maven构建管理

注意事项：
1.需要额外添加外部jar的模块如下：
  spring-core:  spring-cglib-repack-3.2.5、spring-objenesis-repack-2.6
  spring-beans:  spring-cglib-repack-3.2.5
  spring-aop:  spring-cglib-repack-3.2.5、spring-objenesis-repack-2.6
  spring-context:  spring-cglib-repack-3.2.5、spring-objenesis-repack-2.6
  spring-webmvc:  spring-objenesis-repack-2.6
  
2.个别模块因依赖问题，有些无关紧要的代码注释掉了
  
