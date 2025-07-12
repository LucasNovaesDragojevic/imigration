package imigration.api.builder;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;
import imigration.api.model.enums.Country;
import imigration.api.model.enums.Step;

@Component
public class ProcessBuilder {

    private Process process;

    public ProcessBuilder process() {
        this.process = new Process();
        return this;
    }

    public ProcessBuilder withOwner(final User owner) {
        this.process.setOwner(owner);
        return this;
    }

    public ProcessBuilder withRandomStep() {
        final var randomStep = Step.values()[ThreadLocalRandom.current().nextInt(Step.values().length)];
        this.process.setStep(randomStep);
        return this;
    }

    public ProcessBuilder withRandomNationality() {
        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        this.process.setNationality(randomCountry);
        return this;
    }    

    public ProcessBuilder withRandomDateBirth() {
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        this.process.setDateBirth(randomDate);
        return this;
    }

    public ProcessBuilder withRandomPassport() {
        final var randomPassport = UUID.randomUUID().toString();
        this.process.setPassport(randomPassport);
        return this;
    }

    public Process build() {
        return this.process;
    }

    public Process buildRandomProcess(final User owner) {
        return this.process()
                   .withOwner(owner)
                   .withRandomStep()
                   .withRandomNationality()
                   .withRandomDateBirth()
                   .withRandomPassport()
                   .build();
    }
}
