package com.bookparser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class CounterParty {

    private final Multimap<String, RowEntry> buys = HashMultimap.create();
    private final Multimap<String, RowEntry> sells = HashMultimap.create();

    private final Map<String, BigDecimal> totalBuys = new HashMap<>();
    private final Map<String, BigDecimal> totalSells = new HashMap<>();


    private final Set<String> currencyPairs = new TreeSet<>();

    private final List<CurrencyPair> ccyData = new ArrayList<>();

    public void inputBuy(RowEntry rowEntry){
        currencyPairs.add(rowEntry.getCurrencyPair());
        buys.put(rowEntry.getCurrencyPair(), rowEntry);
    }

    public void inputSell(RowEntry rowEntry){
        currencyPairs.add(rowEntry.getCurrencyPair());
        sells.put(rowEntry.getCurrencyPair(), rowEntry);
    }

    public static boolean isSwapOrFwd(String tradeId){
        return tradeId.contains("FW") || tradeId.contains("F1");
    }

    public List<CurrencyPair> getCcyData() {
        return ccyData;
    }

    public static Comparator<RowEntry> comparator = (o1, o2) -> {

        if(isSwapOrFwd(o1.getTradeId()) && !isSwapOrFwd(o2.getTradeId())){
            return -1;
        } else if(!isSwapOrFwd(o1.getTradeId()) && isSwapOrFwd(o2.getTradeId())){
            return 1;
        }

        return Integer.compare(o1.getNum(), o2.getNum());
    };

    //testing
//    public static void main(String[] args) {
//
//        List<RowEntry> enties = dummyEntries("1","2","3","1","2","3","1","2","3","1","2","3","1","2","3","1","2","3","1","2","3.F1","3.FW","4");
////        List<RowEntry> enties = dummyEntries("3","2","1");
//        enties.sort(comparator);
//        System.out.println(enties);
//
//    }
//
//    static List<RowEntry> dummyEntries(String... tradeIds){
//        List<RowEntry> enties = new ArrayList<>();
//
//        for (String tradeId : tradeIds) {
//            enties.add(dummyRow(tradeId));
//        }
//        return enties;
//    }
//
//    static RowEntry dummyRow(String tradeId){
//        return new RowEntry(null, tradeId,null, null, new BigDecimal(1), null);
//    }

    public void calculateTotals() {

        for(String currencyPair: currencyPairs){

            BigDecimal totalBuy = new BigDecimal("0");
            BigDecimal totalSell = new BigDecimal("0");

            List<RowEntry> buyEntries = new ArrayList<>(buys.get(currencyPair));
            if(buyEntries != null){
                for(RowEntry buyEntry: buyEntries){
                    totalBuy = totalBuy.add(buyEntry.getConAmount());
                }
            }
            buyEntries.sort(comparator);

            List<RowEntry> sellEntries = new ArrayList<>(sells.get(currencyPair));
            if(sellEntries != null){
                    for(RowEntry sellEntry: sellEntries){
                        totalSell = totalSell.add(sellEntry.getConAmount());
                    }
            }
            sellEntries.sort(comparator);

            ccyData.add(new CurrencyPair(currencyPair, totalBuy, totalSell, buyEntries, sellEntries));

        }

    }

    public void adjustRows() {

        for (CurrencyPair data : ccyData) {
            BigDecimal totals = data.getTotalBuys().subtract(data.getTotalSells());

            if(totals.compareTo(BigDecimal.ZERO)==0){
//                data.mirror();
                data.zeroDownAll();
                data.setTotallyNegated(true);
            }else{
                Boolean isBuyGreaterThanSell = totals.compareTo(BigDecimal.ZERO)==1;

                if(isBuyGreaterThanSell){
                    data.setBuyGreaterThanSell(true);
                    // strike off buys until the total sell is consumed
                    BigDecimal deplete = data.getTotalSells();
                    for (RowEntry buyEntry : data.getBuys()) {
                        if(deplete.compareTo(BigDecimal.ZERO)==1){

                            // the important bit
                            if(deplete.compareTo(buyEntry.getConAmount())==0){
                                deplete = BigDecimal.ZERO;
                                buyEntry.adjustToZero();
                            }else if(deplete.compareTo(buyEntry.getConAmount())==-1){
                                BigDecimal adjusted = buyEntry.getConAmount().subtract(deplete);
                                deplete = BigDecimal.ZERO;
                                buyEntry.setAdjustedConAmount(adjusted);
                            }else if(deplete.compareTo(buyEntry.getConAmount())==1){
                                deplete = deplete.subtract(buyEntry.getConAmount());
                                buyEntry.adjustToZero();
                            }

                        }else{
                            buyEntry.mirror();
                        }
                    }

                    for (RowEntry sellEntry : data.getSells()) {
                        sellEntry.adjustToZero();
                    }

                }else{
                    // strike off sells until the total buy is consumeed
                    BigDecimal deplete = data.getTotalBuys();
                    for (RowEntry sellEntry : data.getSells()) {
                        if(deplete.compareTo(BigDecimal.ZERO)==1){

                            // the important bit
                            if(deplete.compareTo(sellEntry.getConAmount())==0){
                                deplete = BigDecimal.ZERO;
                                sellEntry.adjustToZero();
                            }else if(deplete.compareTo(sellEntry.getConAmount())==-1){
                                BigDecimal adjusted = sellEntry.getConAmount().subtract(deplete);
                                deplete = BigDecimal.ZERO;
                                sellEntry.setAdjustedConAmount(adjusted);
                            }else if(deplete.compareTo(sellEntry.getConAmount())==1){
                                deplete = deplete.subtract(sellEntry.getConAmount());
                                sellEntry.adjustToZero();
                            }


                        }else{
                            sellEntry.mirror();
                        }

                        System.out.println("deplete is: " +  deplete);

                    }

                    for (RowEntry buyEntry : data.getBuys()) {
                        buyEntry.adjustToZero();
                    }

                }

            }

        }
    }

    public void calculateWeightedAverages() {
        ccyData.forEach(data -> {

            if(!data.isTotallyNegated())
                data.calculateWeightedAverage();
        });
    }

    @Override
    public String toString() {
        return "CounterParty{" +
                "ccyData=" + ccyData +
                '}';
    }


}
