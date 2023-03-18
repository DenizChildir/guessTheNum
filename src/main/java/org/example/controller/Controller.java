package org.example.controller;

import org.example.service.ServiceX;
import org.example.dao.DAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/")
public class Controller {
    DAO dao = new DAO();
    Long id = Long.valueOf(0);

    @GetMapping("/game")
    public String createTables() {
        return ServiceX.getAllRoundsAsString();
    }

    @PostMapping("/begin")
    public ResponseEntity<Long> begin() {
        id = Long.valueOf(ServiceX.startGame());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(id);
    }

    @PostMapping("/guess")
    public String guess(@RequestBody Map<String, String> requestBody) {
        String guess = requestBody.get("guess");
        return ServiceX.makeGuess(guess);
    }

    @GetMapping("/round/{id}")
    public String game(@PathVariable int id) {
        return ServiceX.RoundWithGame(id);
    }

    @GetMapping("/game/{id}")
    public String round(@PathVariable int id) {
        return ServiceX.roundinfo(id);
    }


}
