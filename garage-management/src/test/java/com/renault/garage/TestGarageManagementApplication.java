package com.renault.garage;

import org.springframework.boot.SpringApplication;

public class TestGarageManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(GarageManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
