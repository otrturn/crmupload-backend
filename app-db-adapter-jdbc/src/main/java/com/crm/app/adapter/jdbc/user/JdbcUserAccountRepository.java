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

        var params = new MapSqlParameterSource("username", username);

        List<UserAccount> users = jdbc.query(sql, params, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            String u = rs.getString("username");
            String pw = rs.getString("password");
            String rolesStr = rs.getString("roles");
            List<String> roles = Arrays.stream(rolesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            return new UserAccount(id, u, pw, roles);
        });

        return users.stream().findFirst();
    }}
