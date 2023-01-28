package at.mlem.talkingenemies.zomboid;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ColorParserTest {

    @Test
    void colorZero() {
        Color color = ColorParser.parseFromHex("#000000");
        assertThat(color)
                .extracting(Color::r, Color::g, Color::b).isEqualTo(List.of(0.0F, 0.0F, 0.0F));
    }
    @Test
    void colorMax() {
        Color color = ColorParser.parseFromHex("#FFFFFF");
        assertThat(color)
                .extracting(Color::r, Color::g, Color::b).isEqualTo(List.of(1.0F, 1.0F, 1.0F));
    }
}