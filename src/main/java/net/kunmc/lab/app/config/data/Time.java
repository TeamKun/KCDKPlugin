package net.kunmc.lab.app.config.data;

public class Time {
    private Integer hours;
    private Integer minutes;
    private Integer seconds;

    public Time() {
    }

    public Time(Integer hours, Integer minutes, Integer seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    /**
     * 総秒数を計算
     */
    public int getTotalSeconds() {
        int h = hours != null ? hours : 0;
        int m = minutes != null ? minutes : 0;
        int s = seconds != null ? seconds : 0;
        return h * 3600 + m * 60 + s;
    }
}
