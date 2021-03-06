package com.powerapi.Entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Methods {
    /**
     * Name of the test
     */
    private String name;
    /**
     * Average energy of this test
     */
    private double energy;
    private long duration;
    private List<Iteration> iterations;

    public Methods(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

}
