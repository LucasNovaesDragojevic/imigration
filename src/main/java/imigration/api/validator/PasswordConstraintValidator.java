package imigration.api.validator;

import java.util.stream.Collectors;

import org.passay.CharacterRule;
import org.passay.CzechCharacterData;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.WhitespaceRule;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private final PasswordValidator passwordValidator;

    public PasswordConstraintValidator() {
        this.passwordValidator = new PasswordValidator(
            new LengthRule(8, 24),
            new CharacterRule(CzechCharacterData.UpperCase),
            new CharacterRule(CzechCharacterData.LowerCase),
            new CharacterRule(EnglishCharacterData.Digit),
            new CharacterRule(EnglishCharacterData.Special),
            new WhitespaceRule());
    }

    @Override
    public boolean isValid(final String password, final ConstraintValidatorContext context) {
        final var ruleResult = passwordValidator.validate(new PasswordData(password));

        if (ruleResult.isValid())
            return true;

        final var messageTemplate = passwordValidator.getMessages(ruleResult).stream().collect(Collectors.joining(" "));

        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();

        return false;
    }
}
