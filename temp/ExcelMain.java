package com.sea.excel;

import java.io.*;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelMain {
	
	// implementation 'org.apache.poi:poi:5.2.3'
	public static void main(String[] args) throws EncryptedDocumentException, IOException{
	    // Excelファイルを作成
	    Workbook outputWorkbook = new XSSFWorkbook();
	 
	    // シートを作成
	    Sheet outputSheet = outputWorkbook.createSheet("user_data");
	 
	    // 行を作成
	    Row outputRow = outputSheet.createRow(0);
	 
	    // セルを作成
	    Cell outputCell_name = outputRow.createCell(0);
	    Cell outputCell_sex = outputRow.createCell(1);
	    Cell outputCell_age = outputRow.createCell(2);
	 
	    // セルに値を設定
	    outputCell_name.setCellValue("田中太郎");
	    outputCell_sex.setCellValue("男性");
	    outputCell_age.setCellValue("26歳");
	 
	    // 出力用のストリームを用意
	    FileOutputStream out = new FileOutputStream("User.xlsx");
	 
	    // ファイルへ出力
	    outputWorkbook.write(out);
	  }
}
