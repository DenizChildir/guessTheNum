package example;

import org.example.service.ServiceX;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {
    @Test
    public void testGenerateNumber() {
        String number = ServiceX.generateNumber();
        // check that the number has 4 digits
        assertEquals(4, number.length());

        // check that the digits are unique
        for (int i = 0; i < number.length(); i++) {
            char digit = number.charAt(i);
            assertFalse(number.substring(i + 1).contains(String.valueOf(digit)));
        }
    }

    @Test
    public void testStartGame() {
        int roundId = ServiceX.startGame();
        // check that the round ID is positive
        assertTrue(roundId > 0);
    }

    @Test
    public void testMakeGuess() {
        int roundId = ServiceX.startGame();
        String answer = String.valueOf(ServiceX.dao.getLastGame().getAnswer());

        // make a correct guess
        String result1 = ServiceX.makeGuess(answer);
        assertEquals("e:4:p:0", result1);

        // make an incorrect guess
        String result2 = ServiceX.makeGuess("1234");
        assertNotEquals("e:4:p:0", result2);
    }

    @Test
    public void testCalculateResult() {
        String result1 = ServiceX.calculateResult("1234", "1234");
        assertEquals("e:4:p:0", result1);

        String result2 = ServiceX.calculateResult("5678", "1234");
        assertEquals("e:0:p:0", result2);

        String result3 = ServiceX.calculateResult("1243", "1234");
        assertEquals("e:2:p:2", result3);
    }
}
