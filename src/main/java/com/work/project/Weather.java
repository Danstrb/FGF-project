package com.work.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Weather {
    public Weather(String dateTime, double temperature) {
        this.dateTime = dateTime;
        this.temperature = temperature;
    }

    @SequenceGenerator (
            name = "temperature_sequence",
            sequenceName = "temperature_sequence",
            allocationSize = 1 )
    @GeneratedValue (
            strategy = GenerationType.SEQUENCE,
            generator = "temperature_sequence"
    )

    @Id
    private Long id;
    private String dateTime; //TODO must check with input whether the input is in given format - dd.MM.yyyy, HH:mm
    private double temperature;
}
