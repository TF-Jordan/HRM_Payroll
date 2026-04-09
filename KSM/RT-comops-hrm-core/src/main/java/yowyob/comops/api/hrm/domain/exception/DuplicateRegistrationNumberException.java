package yowyob.comops.api.hrm.domain.exception;

public class DuplicateRegistrationNumberException extends RuntimeException {

    public DuplicateRegistrationNumberException(String registrationNumber) {
        super("Registration number already exists: " + registrationNumber);
    }
}
