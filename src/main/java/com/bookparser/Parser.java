package com.bookparser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parser {


    private static final Map<String,CounterParty> parties = new HashMap<>();

    public static void main(String[] args) throws Exception
    {
        System.out.println("start");
//        String fileName = "src/main/resources/Testdata2.xlsm";
//        String fileName = "src/main/resources/Testdata5.xlsx";
        String fileName = JOptionPane.showInputDialog("input file");
        Workbook wb = readFile(fileName);


        Sheet sheet = wb.getSheet("input");

        int numberOfRows = sheet.getPhysicalNumberOfRows();

        for(int i=1; i<numberOfRows; i++){
            Row row = sheet.getRow(i);


            if(row != null){
                String tradeNumber = row.getCell(RowConstants.TRADE_NUMBER, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String counterPartyId = row.getCell(RowConstants.COUNTER_PARTY, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
                String currencyPair = row.getCell(RowConstants.CURRENCY_PAIR, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
                String buySell = row.getCell(RowConstants.BUY_SELL, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
                BigDecimal conAmount = new BigDecimal(row.getCell(RowConstants.CON_AMOUNT, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue());
                BigDecimal rate = new BigDecimal(row.getCell(RowConstants.RATE, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue());
                RowEntry entry = new RowEntry(row, tradeNumber, currencyPair, buySell, conAmount, rate);

                CounterParty counterParty = getOrCreateCounterParty(counterPartyId);

                if("buy".equalsIgnoreCase(entry.getBuySell())){
                    counterParty.inputBuy(entry);
                }else{
                    counterParty.inputSell(entry);
                }

            }
        }


        OutputHandler outputHandler = new OutputHandler();
        outputHandler.createNewWorkBook();
        outputHandler.copyHeaders(sheet);

        parties.forEach((currencyPair, counterParty)->{
            counterParty.calculateTotals();
            counterParty.adjustRows();

            //System.out.println(counterParty);
            counterParty.calculateWeightedAverages();
            outputHandler.outputRows(counterParty);
        });

        outputHandler.finish(fileName);
        System.out.println("done");
    }

    private static CounterParty getOrCreateCounterParty(String counterPartyId) {
        if(!parties.containsKey(counterPartyId)){
            parties.put(counterPartyId, new CounterParty());
        }

        return parties.get(counterPartyId);
    }

    private static Workbook readFile(String filename) throws IOException, InvalidFormatException {

//        FileInputStream fis = new FileIn
        Workbook wb = WorkbookFactory.create(new File(filename));
        return wb;
    }
}
