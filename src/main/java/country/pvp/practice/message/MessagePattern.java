package country.pvp.practice.message;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessagePattern {

    private final String placeholder;
    private final Object value;

    public String translate(String message) {
        return message.replace(placeholder, value.toString());
    }
}
