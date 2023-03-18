package org.example.service;

import org.example.dao.DAO;
import org.example.dto.Game;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ServiceX {
 public static DAO dao=new DAO();

 public static int startGame(){
     String answer = generateNumber();
     int ret=dao.insertRound();
     dao.createGame(ret,0,0,Integer.parseInt(answer));
     return ret;
 }

    public static String makeGuess(String guess) {
        Game game=dao.getLastGame();
        int round=game.getRoundId();
        int gId=game.getGameId();
        String answer= String.valueOf(game.getAnswer());
        dao.createGame(round,gId+1,Integer.parseInt(guess),Integer.parseInt(answer));
        String result = calculateResult(guess, answer);
        if (result.equals("e:4:p:0")) {
            dao.updateRound(round);
        }
        return result;
    }

    public static String generateNumber() {
        Random random = new Random();
        List<Integer> digits = new ArrayList<>();
        int digit;

        do {
            digit = random.nextInt(10);
            if (!digits.contains(digit)) {
                digits.add(digit);
            }
        } while (digits.size() < 4);

        String number = "";
        for (int i = 0; i < 4; i++) {
            number += digits.get(i);
        }

        return number;
    }
    public static String calculateResult(String guess, String answer) {
        int exactMatches = 0;
        int partialMatches = 0;

        for (int i = 0; i < guess.length(); i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                exactMatches++;
            } else if (answer.contains(String.valueOf(guess.charAt(i)))) {
                partialMatches++;
            }
        }

        return "e:" + exactMatches + ":p:" + partialMatches;
    }

    public static String roundinfo(int id) {
     return dao.roundinfo(id);
    }

    public static String RoundWithGame(int id) {
    return dao.RoundWithGame(id);
    }

    public static String getAllRoundsAsString() {
     return dao.getAllRoundsAsString();
    }
}

//    public void playGame() {
//        String answer = generateNumber();
//        boolean inProgress = true;
//        int gameId=1;
//        System.out.println("Starting new game...");
//        int round=dao.insertRound();
//
//
//
//        while (inProgress) {
//            System.out.println("Enter your guess " + answer + " ");
//            Scanner sc=new Scanner(System.in);
//            String guess=sc.nextLine();
//        /*
//        if (!isValidGuess(guess)) {
//            System.out.println("Invalid guess. Please try again.");
//            continue;
//        }
//        */
//            dao.createGame(round,gameId,Integer.parseInt(guess),Integer.parseInt(answer));
//            String result = calculateResult(guess, answer);
//            System.out.println("Result: " + result);
//            gameId++;
//            if (result.equals("e:4:p:0")) {
//                inProgress = false;
//                System.out.println("Congratulations, you won!");
//                dao.updateRound(round);
//            }
//        }
//    }
//
