package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
@Repository
public class DAO {

    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String user = "root";
    private final String pass = "uuuu";
    private final String dbName = "mydatabase";
    private final String url = "jdbc:mysql://localhost:3306/mydatabase";
    DataSource dataSource = DataSourceFactory.createDataSource();
//    @Autowired
//   private JdbcTemplate jdbcTemplate;

    private final JdbcTemplate jdbcTemplate=new JdbcTemplate(dataSource);

    public void createTables() {

        String createDbSql = "CREATE DATABASE IF NOT EXISTS " + dbName;
        jdbcTemplate.execute(createDbSql);

        jdbcTemplate.execute("USE " + dbName);

        String roundTableSql = "CREATE TABLE IF NOT EXISTS `round` (\n" +
                "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `in_progress` INT(1) NOT NULL DEFAULT '0',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ");";
        jdbcTemplate.execute(roundTableSql);

        String gameTableSql = "CREATE TABLE IF NOT EXISTS `game` (\n" +
                "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `round_id` INT(11) NOT NULL,\n" +
                "  `game_id` INT(11) NOT NULL,\n" +
                "  `answer` INT(11) NOT NULL,\n" +
                "  `time` DATETIME NOT NULL,\n" +
                "  `guess` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  CONSTRAINT `fk_game_round` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE CASCADE\n" +
                ");";
        jdbcTemplate.execute(gameTableSql);

        System.out.println("Tables created successfully.");
    }

    public int insertRound() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO `round` (`in_progress`) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, 1);
            return ps;
        }, keyHolder);

        int roundId = keyHolder.getKey().intValue();
        System.out.println("New round inserted with ID: " + roundId);

        return roundId;
    }

    public void updateRound(int roundId) {
        int numRowsUpdated = jdbcTemplate.update("UPDATE `round` SET `in_progress` = 0 WHERE `id` = ?", roundId);
        System.out.println(numRowsUpdated + " round(s) updated successfully.");
    }

    public void createGame(int roundId, int gameId, int guess, int answer) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update("INSERT INTO `game` (`round_id`, `game_id`, `answer`, `guess`, `time`) VALUES (?, ?, ?, ?, ?)", roundId, gameId, answer, guess, currentTime);

        System.out.println("New game created successfully.");
    }

    public LinkedList<Round> listAllRounds() {
        String roundSql = "SELECT * FROM `round`";
        return jdbcTemplate.query(roundSql, new ResultSetExtractor<LinkedList<Round>>() {
            @Override
            public LinkedList<Round> extractData(ResultSet rs) throws SQLException {
                LinkedList<Round> rounds = new LinkedList<>();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    int inProgress = rs.getInt("in_progress");

                   
                    Round round = new Round(id, inProgress);


                    LinkedList<Game> games = getGamesForRound(id);


                    round.setGameList(games);


                    rounds.add(round);
                }

                return rounds;
            }
        });
    }

    public Round getRoundWithGames(int roundId) {
        String roundSql = "SELECT * FROM `round` WHERE `id` = ?;";
        String gameSql = "SELECT * FROM `game` WHERE `round_id` = ?;";
        Round round;

        try {
            round = jdbcTemplate.queryForObject(roundSql, new Object[]{roundId}, new RoundDAO.RoundRowMapper());

            Round finalRound = round;
            jdbcTemplate.query(gameSql, new Object[]{roundId}, new ResultSetExtractor<List<Game>>() {
                @Override
                public List<Game> extractData(ResultSet gameRs) throws SQLException, DataAccessException {
                    LinkedList<Game> games = new LinkedList<>();
                    while (gameRs.next()) {
                        int gameId = gameRs.getInt("game_id");
                        int answer = gameRs.getInt("answer");
                        Timestamp time = gameRs.getTimestamp("time");
                        int guess = gameRs.getInt("guess");

                        // Create a new Game object and add it to the list
                        Game game = new Game(gameRs.getInt("id"), roundId, gameId, answer, time, guess);
                        games.add(game);
                    }


                    finalRound.setGameList(games);
                    return games;
                }
            });

        } catch (DataAccessException e) {
            e.printStackTrace();
            round = null;
        }

        return round;
    }


    public String getAllRoundsAsString() {
        StringBuilder sb = new StringBuilder();

        LinkedList<Round> rounds = listAllRounds();
        for (Round round : rounds) {
            sb.append("Round ID: ").append(round.getId()).append("\n");
            sb.append("In progress: ").append(round.getInProgress()).append("\n");
            sb.append("Games:\n");

            LinkedList<Game> games = round.getGameList();
            for (Game game : games) {
                if (round.getInProgress() == 1) {
                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
                    sb.append("  Answer: ****\n");
                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
                    sb.append("  Time: ").append(game.getTime()).append("\n");
                } else {
                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
                    sb.append("  Answer: ").append(game.getAnswer()).append("\n");
                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
                    sb.append("  Time: ").append(game.getTime()).append("\n");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }
    public Game getLastGame() {
        String sql = "SELECT * FROM game ORDER BY id DESC LIMIT 1";

        return jdbcTemplate.queryForObject(sql, new RowMapper<Game>() {
            @Override
            public Game mapRow(ResultSet rs, int rowNum) throws SQLException {
                int id = rs.getInt("id");
                int roundId = rs.getInt("round_id");
                int gameId = rs.getInt("game_id");
                int answer = rs.getInt("answer");
                LocalDateTime time = rs.getTimestamp("time").toLocalDateTime();
                int guess = rs.getInt("guess");
                return new Game(id, roundId, gameId, answer, Timestamp.valueOf(time), guess);
            }
        });
    }

    public String RoundWithGame(int roundId) {
        StringBuilder sb = new StringBuilder();
        Round round = getRoundWithGames(roundId);
        if (round != null) {
            sb.append("Round ID: ").append(round.getId()).append("\n");
            sb.append("In progress: ").append(round.getInProgress()).append("\n");

            LinkedList<Game> games = round.getGameList();
            for (Game game : games) {
                if (round.getInProgress() == 1) {
                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
                    sb.append("  Answer: ****\n");
                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
                    sb.append("  Time: ").append(game.getTime()).append("\n");
                } else {
                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
                    sb.append("  Answer: ").append(game.getAnswer()).append("\n");
                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
                    sb.append("  Time: ").append(game.getTime()).append("\n");
                }
            }
        } else {
            sb.append("Round not found.");
        }
        return sb.toString();
    }
    private LinkedList<Game> getGamesForRound(int roundId) {
        String query = "SELECT * FROM game WHERE round_id = ?";
        RowMapper<Game> rowMapper = new RowMapper<Game>() {
            @Override
            public Game mapRow(ResultSet rs, int rowNum) throws SQLException {
                int gameId = rs.getInt("game_id");
                int answer = rs.getInt("answer");
                Timestamp time = rs.getTimestamp("time");
                int guess = rs.getInt("guess");
                return new Game(0, roundId, gameId, answer, time, guess);
            }
        };

        return new LinkedList<>(jdbcTemplate.query(query, rowMapper, roundId));
    }

    public String roundsToString() {
        LinkedList<Round> rounds = listAllRounds();
        StringBuilder sb = new StringBuilder();
        for (Round round : rounds) {
            Game game=round.gameList.get(0);
            if (round.getInProgress() == 1) {
                sb.append("Round ").append(round.getId()).append(":").append("\n");
                sb.append("In Progress: ").append(round.getInProgress()).append("\n");
                sb.append("Answer: ****").append("\n");
            } else {
                sb.append("Round ").append(round.getId()).append(":").append("\n");
                sb.append("In Progress: ").append(round.getInProgress()).append("\n");
                sb.append("Answer: ").append("\n").append(game.getAnswer()).append("\n");
            }

        }
        return sb.toString();
    }
    public String roundinfo(int roundId) {
        StringBuilder sb = new StringBuilder();
        Round round = getRoundWithGames(roundId);
        if (round != null) {
            sb.append("Round ID: ").append(round.getId()).append("\n");
            sb.append("In progress: ").append(round.getInProgress()).append("\n");

            Game game = round.getGameList().get(0);
            if(round.getInProgress()==1) {
                sb.append("  Answer: ****").append("\n");
            }else {
                sb.append("  Answer: ").append(game.getAnswer()).append("\n");
            }
        } else {
            sb.append("Round not found.");
        }
        return sb.toString();
    }


    public void printRoundWithGames(int roundId) {
        Round round = getRoundWithGames(roundId);
        if (round != null) {
            System.out.println("Round ID: " + round.getId());
            System.out.println("In progress: " + round.getInProgress());

            LinkedList<Game> games = round.getGameList();
            for (Game game : games) {
                System.out.println("  Game ID: " + game.getGameId());
                System.out.println("  Answer: " + game.getAnswer());
                System.out.println("  Guess: " + game.getGuess());
                System.out.println("  Time: " + game.getTime());
            }
        } else {
            System.out.println("Round not found.");
        }
    }
    /*
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String url = "jdbc:mysql://localhost:3306/mydatabase";
    private final String user = "root";
    private final String pass = "uuuu";


    public void createTables() {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Create the round table
            String roundTableSql = "CREATE TABLE `round` (\n" +
                    "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `in_progress` INT(1) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ");";
            try (PreparedStatement pstmt = conn.prepareStatement(roundTableSql)) {
                pstmt.executeUpdate();
            }

            // Create the game table
            String gameTableSql = "CREATE TABLE `game` (\n" +
                    "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `round_id` INT(11) NOT NULL,\n" +
                    "  `game_id` INT(11) NOT NULL,\n" +
                    "  `answer` INT(11) NOT NULL,\n" +
                    "  `time` DATETIME NOT NULL,\n" +
                    "  `guess` INT(11) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  CONSTRAINT `fk_game_round` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE CASCADE\n" +
                    ");";
            try (PreparedStatement pstmt = conn.prepareStatement(gameTableSql)) {
                pstmt.executeUpdate();
            }

            System.out.println("Tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
*/
    /*
    public List<Round> getRoundsForGame(int gameId) {
        List<Round> rounds = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT DISTINCT `round_id` FROM `game` WHERE `game_id` = ? ORDER BY `time` ASC;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, gameId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int roundId = rs.getInt("round_id");
                    Round round = getRoundWithGames(roundId);
                    if (round != null) {
                        rounds.add(round);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rounds;
    }
*/
}

//       public String roundsToString(LinkedList<Round> rounds) {
//        StringBuilder sb = new StringBuilder();
//        for (Round round : rounds) {
//            sb.append("Round ").append(round.getId()).append(":").append("\n");
//            sb.append("In Progress: ").append(round.getInProgress()).append("\n");
//            sb.append("Games:").append("\n");
//            for (Game game : round.getGameList()) {
//                if (round.getInProgress() == 1) {
//                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
//                    sb.append("  Answer: ****\n");
//                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
//                    sb.append("  Time: ").append(game.getTime()).append("\n");
//                } else {
//                    sb.append("  Game ID: ").append(game.getGameId()).append("\n");
//                    sb.append("  Answer: ").append(game.getAnswer()).append("\n");
//                    sb.append("  Guess: ").append(game.getGuess()).append("\n");
//                    sb.append("  Time: ").append(game.getTime()).append("\n");
//                }
//            }
//        }
//        return sb.toString();
//    }
//
//    public String getAllRoundsAsString() {
//        StringBuilder sb = new StringBuilder();
//
//        LinkedList<Round> rounds = listAllRounds();
//        for (Round round : rounds) {
//            sb.append("Round ID: ").append(round.getId()).append("\n");
//            sb.append("In progress: ").append(round.getInProgress()).append("\n");
//            sb.append("Games:\n");
//
//            LinkedList<Game> games = round.getGameList();
//            for (Game game : games) {
//                sb.append("  Game ID: ").append(game.getGameId()).append("\n");
//                sb.append("  Answer: ").append(game.getAnswer()).append("\n");
//                sb.append("  Guess: ").append(game.getGuess()).append("\n");
//                sb.append("  Time: ").append(game.getTime()).append("\n");
//            }
//
//            sb.append("\n");
//        }
//
//        return sb.toString();
//    }



//    public void printAllRounds() {
//        LinkedList<Round> rounds = listAllRounds();
//        for (Round round : rounds) {
//            System.out.println("Round ID: " + round.getId());
//            System.out.println("In progress: " + round.getInProgress());
//            System.out.println("Games:");
//
//            LinkedList<Game> games = round.getGameList();
//            for (Game game : games) {
//                System.out.println("  Game ID: " + game.getGameId());
//                System.out.println("  Answer: " + game.getAnswer());
//                System.out.println("  Guess: " + game.getGuess());
//                System.out.println("  Time: " + game.getTime());
//            }
//
//            System.out.println();
//        }
//    }