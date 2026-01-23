package net.kunmc.lab.app.config.data;

public class Bossbar {
    private String mcid;

    public Bossbar() {
    }

    public Bossbar(String mcid) {
        this.mcid = mcid;
    }

    public String getMcid() {
        return mcid;
    }

    public void setMcid(String mcid) {
        this.mcid = mcid;
    }
}
