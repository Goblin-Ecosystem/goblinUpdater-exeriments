package entities;

import java.util.Objects;

public class Metric {
    private String name;
    private double coef;

    public Metric(String name, double coef) {
        this.name = name;
        this.coef = coef;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCoef() {
        return coef;
    }

    public void setCoef(double coef) {
        this.coef = coef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return name.equals(metric.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
