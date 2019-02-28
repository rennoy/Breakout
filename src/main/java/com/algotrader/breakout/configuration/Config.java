package com.algotrader.breakout.configuration;

import ch.algotrader.simulation.Simulator;
import ch.algotrader.simulation.SimulatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public Simulator simulator() {
        Simulator simulator = new SimulatorImpl();
        simulator.setCashBalance(1000000);
        return simulator;
    }
}
