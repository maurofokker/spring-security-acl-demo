package com.maurofokker.security.acl.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.maurofokker.security")
@EnableJpaRepositories("com.maurofokker.security")
@EntityScan("com.maurofokker.security.acl.model")
public class SpringSecurityAclDemoApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Class[] { SpringSecurityAclDemoApplication.class, SecurityConfiguration.class, WebMvcConfiguration.class, MethodSecurityConfiguration.class }, args);
    }

}
