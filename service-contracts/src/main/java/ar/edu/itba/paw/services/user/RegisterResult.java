package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.User;

import java.util.Optional;

/**
 * Outcome of {@link UserService#tryRegister} (register or upgrade provisional user).
 */
public final class RegisterResult {

    public enum Outcome {
        /** New account: caller should send verification email and redirect to login. */
        CREATED_PENDING_VERIFICATION,
        /** Provisional account upgraded: caller can auto-login. */
        UPGRADED,
        /** Email already registered and not provisional. */
        DUPLICATE_EMAIL
    }

    private final Outcome outcome;
    private final User user;

    private RegisterResult(final Outcome outcome, final User user) {
        this.outcome = outcome;
        this.user = user;
    }

    public static RegisterResult createdPendingVerification(final User user) {
        return new RegisterResult(Outcome.CREATED_PENDING_VERIFICATION, user);
    }

    public static RegisterResult upgraded(final User user) {
        return new RegisterResult(Outcome.UPGRADED, user);
    }

    public static RegisterResult duplicateEmail() {
        return new RegisterResult(Outcome.DUPLICATE_EMAIL, null);
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }
}
