package cn.charlie166.word.lucene.file;

import java.nio.file.Path;
import java.nio.file.Paths;

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
	
    public static void main(String[] args) {
    	System.setProperty("log4j.configurationFile", "log4j2.xml");
    	
    	
//    	String txtDirection = App.class.getResource("/files").getPath();
//    	String txtDirection = "F:/charlie/documents/work/lucene";
//		final Path docDir = Paths.get(txtDirection);
//		ChineseIndex ci = new ChineseIndex();
//		ci.buildFromPath(docDir);
    	try {
    		ChineseSearch cs = new ChineseSearch();
    		cs.query("数据");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
