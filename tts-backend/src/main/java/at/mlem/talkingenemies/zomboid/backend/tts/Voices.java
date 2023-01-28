package at.mlem.talkingenemies.zomboid.backend.tts;

public enum Voices {

    GER_M_NORM(0.0, 1.0, "de-DE", "de-DE-Standard-B"),
    GER_F_NORM(0.0, 1.0, "de-DE", "de-DE-Standard-A"),
    GER_F_LOW(-9.6, 1.0, "de-DE", "de-DE-Standard-A"),
    USA_M_LOW(-6.4, 1.0, "en-US", "de-DE-Standard-B"),
    IND_M_NORM(0, 1.0, "en-IN", "en-IN-Standard-B"),
    GER_M_LOW(-14.0, 1.0, "de-DE", "de-DE-Standard-B");

    private final double pitch;
    private final double speakingRate;
    private final String languageCode;
    private final String name;

    Voices(double pitch, double speakingRate, String languageCode, String name) {

        this.pitch = pitch;
        this.speakingRate = speakingRate;
        this.languageCode = languageCode;
        this.name = name;
    }

    public double getPitch() {
        return pitch;
    }

    public double getSpeakingRate() {
        return speakingRate;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getName() {
        return name;
    }
}
