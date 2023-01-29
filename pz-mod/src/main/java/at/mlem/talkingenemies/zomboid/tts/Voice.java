package at.mlem.talkingenemies.zomboid.tts;

public enum Voice {

    GER_M_NORM,
    GER_F_NORM,
    GER_F_LOW,
    USA_M_LOW,
    IND_M_NORM,
    GER_M_LOW,
    UNDEFINED;

    public static Voice findVoice(String part) {
        try{
            return Voice.valueOf(part);
        } catch (IllegalArgumentException e) {
            return Voice.UNDEFINED;
        }
    }

    public static boolean isVoice(String textPart) {
        try{
            Voice.valueOf(textPart);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
