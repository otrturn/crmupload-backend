package com.crm.app.adapter.jdbc.user;

import com.crm.app.port.user.UserAccount;
import com.crm.app.port.user.UserAccountRepositoryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserAccountRepository implements UserAccountRepositoryPort {

    private static final String LITERAL_USERNAME = "username";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserAccountRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        String sql = """
                SELECT id, username, password, roles
                FROM app.user_account
                WHERE username = :username
                """;

        var params = new MapSqlParameterSource(LITERAL_USERNAME, username);

        List<UserAccount> users = jdbc.query(sql, params, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            String u = rs.getString(LITERAL_USERNAME);
            String pw = rs.getString("password");
            String rolesStr = rs.getString("roles");
            List<String> roles = Arrays.stream(rolesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            return new UserAccount(id, u, pw, roles);
        });

        return users.stream().findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = """
                SELECT COUNT(*)
                FROM app.user_account
                WHERE username = :username
                """;

        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource(LITERAL_USERNAME, username),
                Long.class);
        return count != null && count > 0;
    }

    @Override
    public long nextUserId() {
        String sql = "SELECT nextval('app.sequence_user_account')";
        Long next = jdbc.getJdbcOperations().queryForObject(sql, Long.class);
        if (next == null) {
            throw new IllegalStateException("Could not obtain next user_account id");
        }
        return next;
    }

    @Override
    public void insertUserAccount(UserAccount userAccount) {
        String sql = """
                INSERT INTO app.user_account (id, username, password, roles, lastlogin)
                VALUES (:id, :username, :password, :roles, NULL)
                """;

        String rolesStr = String.join(",", userAccount.roles());

        var params = new MapSqlParameterSource()
                .addValue("id", userAccount.id())
                .addValue(LITERAL_USERNAME, userAccount.username())
                .addValue("password", userAccount.passwordHash())
                .addValue("roles", rolesStr);

        jdbc.update(sql, params);
    }
}
