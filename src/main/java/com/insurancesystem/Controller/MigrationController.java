package com.insurancesystem.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/update-usernames")
    public ResponseEntity<Map<String, Object>> updateUsernames() {
        String updateSql = "UPDATE clients SET username = SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1) WHERE username IS NULL";

        int updatedRows = jdbcTemplate.update(updateSql);

        // Get the updated records
        String selectSql = "SELECT id, email, username, full_name FROM clients ORDER BY created_at";
        var users = jdbcTemplate.queryForList(selectSql);

        Map<String, Object> response = new HashMap<>();
        response.put("updatedRows", updatedRows);
        response.put("message", "Successfully updated " + updatedRows + " user records");
        response.put("users", users);

        return ResponseEntity.ok(response);
    }
}
