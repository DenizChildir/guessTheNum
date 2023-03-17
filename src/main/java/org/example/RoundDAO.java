package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Repository
public class RoundDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL_ROUNDS_QUERY = "SELECT * FROM mydatabase.round";

    public List<Round> getAllRounds() {
        return jdbcTemplate.query(SELECT_ALL_ROUNDS_QUERY, new RoundRowMapper());
    }

    static class RoundRowMapper implements RowMapper<Round> {
        @Override
        public Round mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int id = resultSet.getInt("id");
            int inProgress = resultSet.getInt("in_progress");
            return new Round(id, inProgress);
        }
    }



    public Round getRoundWithGames(int roundId) {
        String roundSql = "SELECT * FROM `round` WHERE `id` = ?;";
        String gameSql = "SELECT * FROM `game` WHERE `round_id` = ?;";
        Round round;

        try {
            round = jdbcTemplate.queryForObject(roundSql, new Object[]{roundId}, new RoundRowMapper());

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

                    // Set the list of games to the gameList field of the Round object
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

    public LinkedList<Round> listAllRounds() {
        String roundSql = "SELECT * FROM `round`;";
        LinkedList<Round> rounds = new LinkedList<>();

        jdbcTemplate.query(roundSql, new ResultSetExtractor<LinkedList<Round>>() {
            @Override
            public LinkedList<Round> extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int inProgress = rs.getInt("in_progress");

                    // Create a new Round object and add it to the list
                    Round round = new Round(id, inProgress);

                    String gameSql = "SELECT * FROM `game` WHERE `round_id` = ?;";
                    jdbcTemplate.query(gameSql, new Object[]{id}, new ResultSetExtractor<LinkedList<Game>>() {
                        @Override
                        public LinkedList<Game> extractData(ResultSet gameRs) throws SQLException, DataAccessException {
                            // Create a linked list of games for the round
                            LinkedList<Game> games = new LinkedList<>();
                            while (gameRs.next()) {
                                int gameId = gameRs.getInt("game_id");
                                int answer = gameRs.getInt("answer");
                                Timestamp time = gameRs.getTimestamp("time");
                                int guess = gameRs.getInt("guess");

                                // Create a new Game object and add it to the list
                                Game game = new Game(id, round.getId(), gameId, answer, time, guess);
                                games.add(game);
                            }


                            round.setGameList(games);
                            return games;
                        }
                    });


                    rounds.add(round);
                }

                return rounds;
            }
        });

        return rounds;
    }

}
