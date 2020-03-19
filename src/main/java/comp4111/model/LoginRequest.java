package comp4111.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LoginRequest {

    @JsonProperty("Username")
    @NotNull
    private final String username;
    @JsonProperty("Password")
    @NotNull
    private final String password;

    @JsonCreator
    public LoginRequest(
            @NotNull @JsonProperty("Username") String username,
            @NotNull @JsonProperty("Password") String password) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @NotNull
    public String getPassword() {
        return password;
    }
}
