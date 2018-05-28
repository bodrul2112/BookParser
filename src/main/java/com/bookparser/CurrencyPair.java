package com.bookparser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CurrencyPair {

    private String currencyPair;
    private final BigDecimal totalBuys;
    private final BigDecimal totalSells;

    private boolean isBuyGreaterThanSell = false;
    private boolean isTotallyNegated = false;

    private final List<RowEntry> buys;
    private final List<RowEntry> sells;

    private BigDecimal weightedAverage = new BigDecimal("0");
    private BigDecimal totalAdjustedConAmount = new BigDecimal("0");

    public CurrencyPair(String currencyPair, BigDecimal totalBuys, BigDecimal totalSells, List<RowEntry> buys, List<RowEntry> sells) {
        this.currencyPair = currencyPair;
        this.totalBuys = totalBuys;
        this.totalSells = totalSells;
        this.buys = buys;
        this.sells = sells;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public BigDecimal getTotalBuys() {
        return totalBuys;
    }

    public BigDecimal getTotalSells() {
        return totalSells;
    }

    public List<RowEntry> getBuys() {
        return buys;
    }

    public List<RowEntry> getSells() {
        return sells;
    }

    public void setBuyGreaterThanSell(boolean buyGreaterThanSell) {
        isBuyGreaterThanSell = buyGreaterThanSell;
    }

    public boolean isBuyGreaterThanSell() {
        return isBuyGreaterThanSell;
    }

    public boolean isTotallyNegated() {
        return isTotallyNegated;
    }

    public void setTotallyNegated(boolean totallyNegated) {
        isTotallyNegated = totallyNegated;
    }

    public void mirror(){

            for (RowEntry rowEntry : buys) {
                rowEntry.mirror();
            }

            for (RowEntry rowEntry : sells) {
                rowEntry.mirror();
            }
    }

    public void calculateWeightedAverage(){



        if(isBuyGreaterThanSell){

            BigDecimal accumulateRate = new BigDecimal("0");
            BigDecimal accumulateAdjustedConAmount = new BigDecimal("0");

            for(int i=0; i<buys.size(); i++){

                RowEntry row = buys.get(i);

                if(row.getAdjustedConAmount().compareTo(BigDecimal.ZERO)!=0){
                    accumulateRate = accumulateRate.add(row.getRate().multiply(row.getAdjustedConAmount()));
                    accumulateAdjustedConAmount = accumulateAdjustedConAmount.add(row.getAdjustedConAmount());
                }
            }

            weightedAverage = accumulateRate.divide(accumulateAdjustedConAmount, 6, RoundingMode.UP);
            totalAdjustedConAmount = accumulateAdjustedConAmount.setScale(2, RoundingMode.UP);
//            weightedAverage = weightedAverage.setScale(6, RoundingMode.UP);

        }else {
            BigDecimal accumulateRate = new BigDecimal("0");
            BigDecimal accumulateAdjustedConAmount = new BigDecimal("0");


            for(int i=0; i<sells.size(); i++){

                RowEntry row = sells.get(i);

                if(row.getAdjustedConAmount().compareTo(BigDecimal.ZERO)!=0){
                    accumulateRate = accumulateRate.add(row.getRate().multiply(row.getAdjustedConAmount()));
                    accumulateAdjustedConAmount = accumulateAdjustedConAmount.add(row.getAdjustedConAmount());
                }
            }

            weightedAverage = accumulateRate.divide(accumulateAdjustedConAmount, 6, RoundingMode.UP);
            totalAdjustedConAmount = accumulateAdjustedConAmount.setScale(2, RoundingMode.UP).negate();
//            weightedAverage = weightedAverage.setScale(6, RoundingMode.UP);
        }
    }

    public BigDecimal getWeightedAverage() {
        return weightedAverage;
    }

    public BigDecimal getTotalAdjustedConAmount() {
        return totalAdjustedConAmount;
    }



    @Override
    public String toString() {
        return "CurrencyPair{" +
                "currencyPair='" + currencyPair + '\'' +
                ", totalBuys=" + totalBuys +
                ", totalSells=" + totalSells +
                ", buys=" + buys +
                ", sells=" + sells +
                '}';
    }

    public void zeroDownAll() {
        for(RowEntry row: buys){
            row.setAdjustedConAmount(BigDecimal.ZERO);
        }

        for(RowEntry row: sells){
            row.setAdjustedConAmount(BigDecimal.ZERO);
        }
    }
}
