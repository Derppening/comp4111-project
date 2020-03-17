package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class LoginResult {

    @JsonProperty("Token")
    @NotNull
    private final String token;

    public LoginResult(@NotNull @JsonProperty("Token") String token) {
        this.token = token;
    }
}
