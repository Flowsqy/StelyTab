package fr.flowsqy.stelytab.team;

import org.bukkit.ChatColor;

import java.util.Objects;

public class Name {

    private String id;
    private ChatColor color;
    private String prefix;
    private String suffix;

    public Name(String id) {
        this.id = id;
    }

    public Name(String id, ChatColor color, String prefix, String suffix) {
        this.id = id;
        this.color = color;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return Objects.equals(id, name.id) && color == name.color && Objects.equals(prefix, name.prefix) && Objects.equals(suffix, name.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, color, prefix, suffix);
    }

    @Override
    public String toString() {
        return "Name{" +
                "id='" + id + '\'' +
                ", color=" + color +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }
}
