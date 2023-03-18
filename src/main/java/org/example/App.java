package org.example;

import org.example.dao.DAO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        DAO dao=new DAO();
        dao.createTables();
        SpringApplication.run(App.class, args);

        }
    }
