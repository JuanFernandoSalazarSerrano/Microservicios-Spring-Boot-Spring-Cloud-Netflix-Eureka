package com.fsalazar.libs.msvc.commons;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


@SpringBootApplication
//no string connection when we run this springboot app
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class LibsMsvcCommonsApplication {

}
