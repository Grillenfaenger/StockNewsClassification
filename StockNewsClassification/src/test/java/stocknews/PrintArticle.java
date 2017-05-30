package stocknews;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

public class PrintArticle {

	@Test
	public void printArticle() throws ParseException, IOException{
		final String inputfilepath = "input/News_filtered_DE_1.1.xls";
		List<Article> articles = XLSReader.getArticlesFromXlsFile(inputfilepath);
		List<String> artStr = new ArrayList<String>();
		
		for(Article art : articles){
			artStr.add(art.getTitle() + " " + art.getContent());
		}
		
		artStr = artStr.subList(0, 5);
		
		FileUtils.printList(artStr, "output/", "articles", ".txt");
	}
}
