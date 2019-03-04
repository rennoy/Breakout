package com.algotrader.breakout.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Financial pricing data block
 */
@Data
@AllArgsConstructor
public class Quote {

    private Date dateTime;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;

    /**
     * toString override
     * @return a formatted string
     */
    @Override
    public String toString() {
        return  dateTime.toString()
                + " OPEN:" + open.toString()
                + " HIGH:" + high.toString()
                + " LOW:" + low.toString()
                + " CLOSE:" + close.toString();
    }
}
