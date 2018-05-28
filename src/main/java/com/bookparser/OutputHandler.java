package com.bookparser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.css.Counter;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class OutputHandler {


    int currentRow = 0;
    Sheet sheet;

    int weightCurrentRow = 0;
    Sheet weightedSheet;

    String fileName;
    XSSFWorkbook wb;

    public void createNewWorkBook() {




        wb = new XSSFWorkbook();

        sheet = wb.createSheet("generated");
        weightedSheet = wb.createSheet("weighted");


    }

    public void finish(String origFileName){

        fileName = new File(origFileName).getParent() + "/"+ JOptionPane.showInputDialog("enter name of file") + ".xls";
        try(FileOutputStream out = new FileOutputStream(fileName)) {
            wb.write(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outputRows(CounterParty counterParty){

        for (CurrencyPair currencyPair : counterParty.getCcyData()) {
            for (RowEntry rowEntry : currencyPair.getSells()) {
                outputRowWithAdjusted(rowEntry);
            }

            for (RowEntry rowEntry : currencyPair.getBuys()) {
                outputRowWithAdjusted(rowEntry);
            }

            if(!currencyPair.isTotallyNegated()){
                if(currencyPair.isBuyGreaterThanSell()){
                    outputWeightedRow(currencyPair.getBuys().get(0), currencyPair);
                }else{
                    outputWeightedRow(currencyPair.getSells().get(0), currencyPair);
                }
            }
        }
    }

    int headerCell;


    public void copyHeaders(Sheet fromSheet){
        copyHeaders(fromSheet, sheet, "ADJUSTED CON AMOUNT");
        copyHeaders(fromSheet, weightedSheet, "WEIGHTED AVERAGE");
    }

    private void copyHeaders(Sheet fromSheet, Sheet toSheet, String extra){
        Row headers = fromSheet.getRow(0);

        Row toRow = toSheet.createRow(0);

        for(headerCell=0; headerCell<headers.getPhysicalNumberOfCells(); headerCell++){
            Cell from = headers.getCell(headerCell);
            Cell to = toRow.createCell(headerCell, from.getCellTypeEnum());
            to.setCellValue(from.getStringCellValue());
        }

        Cell adjustedHeader = toRow.createCell(headerCell, CellType.STRING);
        adjustedHeader.setCellValue(extra);
    }


    private void outputRowWithAdjusted(RowEntry rowEntry){
        if(BigDecimal.ZERO.compareTo(rowEntry.getAdjustedConAmount()) <0){
            currentRow++;

            Row outputRow = sheet.createRow(currentRow);
            for(int i=0; i<rowEntry.getRow().getPhysicalNumberOfCells(); i++){

                    Cell from = rowEntry.getRow().getCell(i);


                    if(from != null){
                        Cell to = outputRow.createCell(i, from.getCellTypeEnum());
                        switch (from.getCellTypeEnum()){
                            case STRING: to.setCellValue(from.getStringCellValue()); break;
                            case NUMERIC: setCellStyle(from, to); to.setCellValue(from.getNumericCellValue()); break;
                            default:
                                System.out.println("unrecognised type: " + from.getCellTypeEnum());
                        }
                    }else{
                        Cell to = outputRow.createCell(i, CellType.STRING);
                        to.setCellValue("");
                    }

            }
            Cell adjustedCell = outputRow.createCell(headerCell, CellType.NUMERIC);
            adjustedCell.setCellValue(rowEntry.getAdjustedConAmountPretty().toPlainString());
        }
    }

    private void outputWeightedRow(RowEntry rowEntry, CurrencyPair currencyPair){

        weightCurrentRow++;

        Row outputRow = weightedSheet.createRow(weightCurrentRow);
        for(int i=0; i<rowEntry.getRow().getPhysicalNumberOfCells(); i++){


            Cell from = rowEntry.getRow().getCell(i);

            if(from != null){
                Cell to = outputRow.createCell(i, from.getCellTypeEnum());

                if(i==RowConstants.CON_AMOUNT){
                    to.setCellValue(currencyPair.getTotalAdjustedConAmount().doubleValue());
                }
                else if(i==RowConstants.RATE){
                    to.setCellValue(currencyPair.getWeightedAverage().doubleValue());
                }
                else if(i==RowConstants.COU_AMOUNT){
                    BigDecimal adjustedCouAmount = currencyPair.getTotalAdjustedConAmount().multiply(currencyPair.getWeightedAverage());
                    adjustedCouAmount = adjustedCouAmount.setScale(2, RoundingMode.UP);
                    to.setCellValue(adjustedCouAmount.doubleValue());
                }
                else if(i==RowConstants.TRADE_NUMBER){
                    // blank out
                }
                else{
                    switch (from.getCellTypeEnum()){
                        case STRING: to.setCellValue(from.getStringCellValue()); break;
                        case NUMERIC: setCellStyle(from, to); to.setCellValue(from.getNumericCellValue()); break;
                        default:
                            System.out.println("unrecognised type: " + from.getCellTypeEnum());
                    }

                }
            }else{
                Cell to = outputRow.createCell(i, CellType.STRING);
                to.setCellValue("");
            }


        }
        Cell adjustedCell = outputRow.createCell(headerCell, CellType.NUMERIC);
        adjustedCell.setCellValue(currencyPair.getWeightedAverage().toPlainString());
    }


    private CellStyle style = null;
    private void setCellStyle(Cell from, Cell to){


//        if(style == null){
            CellStyle copyStyle =  wb.createCellStyle();
            from.getCellStyle().cloneStyleFrom(copyStyle);

//            style = copyStyle;

//        }

        to.setCellStyle(copyStyle);
    }



}
