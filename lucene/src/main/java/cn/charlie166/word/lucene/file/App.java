package cn.charlie166.word.lucene.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.queryparser.classic.ParseException;

import cn.charlie166.word.lucene.db.IndexSearch;
import cn.charlie166.word.lucene.db.InfoIndex;

/**
* @ClassName: App 
* @Description:
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月19日 
*
 */
public class App {
	
    public static void main(String[] args) throws IOException, ParseException {
    	System.setProperty("log4j.configurationFile", "log4j2.xml");
    	
    	
//    	String txtDirection = App.class.getResource("/files").getPath();
//    	String txtDirection = "F:/charlie/documents/work/lucene";
//		final Path docDir = Paths.get(txtDirection);
//		ChineseIndex ci = new ChineseIndex();
//		ci.buildFromPath(docDir);
    	try {
//    		ChineseSearch cs = new ChineseSearch();
//    		cs.query("provided");
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	InfoIndex ii = new InfoIndex();
//    	ii.indexData();
		IndexSearch is = new IndexSearch();
		is.queryString("学校", 15);
    }
}
