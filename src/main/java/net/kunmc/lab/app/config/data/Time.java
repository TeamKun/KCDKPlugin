package net.kunmc.lab.app.config.data;

public class Time {
    private Integer hour;
    private Integer minutes;
    private Integer second;

    public Time() {
    }

    public Time(Integer hour, Integer minutes, Integer second) {
        this.hour = hour;
        this.minutes = minutes;
        this.second = second;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    /**
     * 総秒数を計算
     */
    public int getTotalSeconds() {
        int h = hour != null ? hour : 0;
        int m = minutes != null ? minutes : 0;
        int s = second != null ? second : 0;
        return h * 3600 + m * 60 + s;
    }
}
