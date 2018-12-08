package com.ifoodcase.playlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * using eureka to discover the service and jasypt to encrypt the properties files keys
 * 
 * @author Mario Beani
 *
 */
@EnableDiscoveryClient	
@SpringBootApplication
@EnableEncryptableProperties
public class PlaylistApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaylistApplication.class, args);
	}
}
