package com.algotrader.breakout;

import com.algotrader.breakout.handlers.QuoteHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QuoteRunner implements CommandLineRunner {

    private final QuoteHandler quoteHandler;

    public QuoteRunner(QuoteHandler quoteHandler) {
        this.quoteHandler = quoteHandler;
    }

    @Override
    public void run(String... args) throws Exception {
        quoteHandler.runStrategy();
    }
}
