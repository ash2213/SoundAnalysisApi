package dat.dtos;

public class PitchPointDTO {
    private final double timeInSeconds;
    private final double pitch;

    public PitchPointDTO(double timeInSeconds, double pitch) {
        this.timeInSeconds = timeInSeconds;
        this.pitch = pitch;
    }

    public double getTimeInSeconds() {
        return timeInSeconds;
    }

    public double getPitch() {
        return pitch;
    }
}

