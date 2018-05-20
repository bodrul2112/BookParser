package com.bookparser;

import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.util.Objects;

public class RowEntry {

    private final Row row;

    private final String tradeId;
    private final int num;
    private final String currencyPair;
    private final String buySell;

    private final BigDecimal conAmount;
    private final BigDecimal rate;
    private BigDecimal adjustedConAmount;

    public RowEntry(Row row, String tradeId, String currencyPair, String buySell, BigDecimal conAmount, BigDecimal rate) {
        this.row = row;
        this.tradeId = tradeId;
        this.currencyPair = currencyPair;
        this.buySell = buySell;
        this.conAmount = conAmount.abs();
        this.rate = rate;

        num = Integer.parseInt(tradeId.split("[.]")[0].trim());

    }


    public Row getRow() {
        return row;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public String getBuySell() {
        return buySell;
    }

    public BigDecimal getConAmount() {
        return conAmount;
    }

    public BigDecimal getAdjustedConAmount() {
        return adjustedConAmount;
    }

    public BigDecimal getAdjustedConAmountPretty() {
        if("sell".equalsIgnoreCase(buySell)){
            return adjustedConAmount.negate();
        }
        return adjustedConAmount;
    }

    public int getNum() {
        return num;
    }

    public void setAdjustedConAmount(BigDecimal adjustedConAmount) {
        this.adjustedConAmount = adjustedConAmount;
    }

    public void mirror(){
        adjustedConAmount = conAmount;
    }

    public void adjustToZero(){
        adjustedConAmount = BigDecimal.ZERO;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "RowEntry{" +
                "tradeId='" + tradeId + '\'' +
                ", buySell='" + buySell + '\'' +
                ", conAmount=" + conAmount +
                ", adjustedConAmount=" + adjustedConAmount +
                '}';
    }
}
