package de.uni_koeln.spinfo.stocknews.applications;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;

public class xlsxTest {
	
	private static Logger log = LoggerFactory.getLogger(xlsxTest.class);	
			

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = "input/News_filtered_DE_1.1.xlsx";
			
		// Open a document from the file system
		SpreadsheetMLPackage xlsxPkg = SpreadsheetMLPackage.load(new java.io.File(inputfilepath));		
				
		WorkbookPart workbookPart = xlsxPkg.getWorkbookPart();
		WorksheetPart sheet = workbookPart.getWorksheet(0);
		
		DataFormatter formatter = new DataFormatter();

		// Now lets print the cell content
		orderContent(sheet,formatter);
//		extractTags(sheet, formatter);
	}
	
	private static void displayContent(WorksheetPart sheet, DataFormatter formatter) {
		
		Worksheet ws = sheet.getJaxbElement();
		SheetData data = ws.getSheetData();
		
		for (Row r : data.getRow() ) {
			System.out.println("row " + r.getR() );			
			
			for (Cell c : r.getC() ) {

//	            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
//	            System.out.print(cellRef.formatAsString());
//	            System.out.print(" - ");

	            // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
	            String text = formatter.formatCellValue(c);
	            System.out.println(c.getR() + " contains " + text);

	            }
		}
		
	}
	
	
	private static void extractTags(WorksheetPart sheet, DataFormatter formatter) throws IOException, ParseException {
		
		Worksheet ws = sheet.getJaxbElement();
		SheetData data = ws.getSheetData();
		
		
		List<Row> rows = data.getRow();
		
		String index;
		String seconds;
		String timestamp;
		String timeStr;
		String headline;
		String text;
		List<Article> articles = new ArrayList<Article>();
		Set<String> tags = new TreeSet<String>();
		
		for(int i = 1; i < rows.size(); i++){
			Row r = rows.get(i);
			List<Cell> cellList = r.getC();
			
			index = formatter.formatCellValue(cellList.get(2));
			seconds = formatter.formatCellValue(cellList.get(1));
			timestamp = formatter.formatCellValue(cellList.get(5));
			System.out.println(timestamp.substring(0, timestamp.length()-2)+ seconds);
			timeStr = timestamp.substring(0, timestamp.length()-2)+ seconds;
			headline = formatter.formatCellValue(cellList.get(6));
			text = formatter.formatCellValue(cellList.get(7));
			
			Article newArticle = new Article(index, timeStr,headline,text);
			
			Pattern pattern = Pattern.compile("(<)((.*?))(>)");
//			Pattern pRound = Pattern.compile("\\((.*?)\\)");
//			Pattern pSquare = Pattern.compile("\\[(.*?)\\]");
					
			
			Matcher matcher = pattern.matcher(text);
			while(matcher.find()){
				tags.add(matcher.group(2).trim());
			}
		}
		System.out.println("Gefundene Tags: " + tags.size());
//		FileUtils.printSet(tags, "output//", "tags2");


		
	}
	
private static void orderContent(WorksheetPart sheet, DataFormatter formatter) throws IOException, ParseException {
		
		Worksheet ws = sheet.getJaxbElement();
		SheetData data = ws.getSheetData();
		
		
		List<Row> rows = data.getRow();
		
		String index;
		String story_date_time;
		String seconds;
		String timeStr;
		String headline;
		String text;
		List<Article> articles = new ArrayList<Article>();
		Map<String,List<Article>> filteredNews = new HashMap<String,List<Article>>();
		filteredNews.put("notag", new ArrayList<Article>());
		TreeSet<String> allTags = new TreeSet<String>();
		
		System.out.println("rows : " + rows.size());
		
		for(int i = 1; i < rows.size(); i++){
			Row r = rows.get(i);
			List<Cell> cellList = r.getC();
			
			index = formatter.formatCellValue(cellList.get(2));
			seconds = formatter.formatCellValue(cellList.get(1));
			story_date_time = formatter.formatCellValue(cellList.get(5));
			headline = formatter.formatCellValue(cellList.get(6));
			text = formatter.formatCellValue(cellList.get(7));
			
			timeStr = story_date_time.substring(0, story_date_time.length()-2)+ seconds;
			
			Article newArticle = new Article(index, timeStr,headline,text);
			
			Set<String> tags = new TreeSet<String>();
			
			Pattern pattern = Pattern.compile("(<)((.*?))(>)");
			Matcher matcher = pattern.matcher(text);
			while(matcher.find()){
				tags.add(matcher.group(2).trim());
				allTags.add(matcher.group(2).trim());
			}
			
			for(String tag : tags){
				if(filteredNews.containsKey(tag)){
					List<Article> current = filteredNews.get(tag);
					current.add(newArticle);
					filteredNews.put(tag, current);
				} else {
					List<Article> current = new ArrayList<Article>();
					current.add(newArticle);
					filteredNews.put(tag, current);
				}
			} if(tags.isEmpty()) {
				List<Article> current = filteredNews.get("notag");
				current.add(newArticle);
				filteredNews.put("notag", current);
			}
			
		}
		System.out.println(filteredNews.size() + ", erwartet werden 1688");
		System.out.println("notag : " + filteredNews.get("notag").size());
		
		for(String key : filteredNews.keySet()){
			System.out.println(key + ": " + filteredNews.get(key).size());
		}
		
		
		
		for(String tag : allTags){
			Collections.sort(filteredNews.get(tag));
			System.out.println("\n\n\n"+ tag + ": ");
			for(Article art : filteredNews.get(tag)){
				System.out.println(art);
			}
		}

		
	}

private static Cell createCell(String content) {

	Cell cell = Context.getsmlObjectFactory().createCell();
	
	CTXstringWhitespace ctx = Context.getsmlObjectFactory().createCTXstringWhitespace();
	ctx.setValue(content);
	
	CTRst ctrst = new CTRst();
	ctrst.setT(ctx);

	cell.setT(STCellType.INLINE_STR);
	cell.setIs(ctrst); // add ctrst as inline string
	
	return cell;
	
}

}


