package cn.charlie166.word.lucene.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.PrintStreamInfoStream;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: ChineseIndex 
* @Description: 中文索引建立
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月20日 
*
 */
public class ChineseIndex {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	* @Title: buildFromPath 
	* @Description: 读取文件建立索引
	* @param docPath 需要索引的文件或文件夹
	 */
	public void buildFromPath(Path docPath){
		if(docPath == null || !Files.isReadable(docPath)){
			logger.error("Document directory '" + docPath.toAbsolutePath() + "' does not exist or is not readable, please check the path");
			System.exit(1);
		}
		/**建立索引路径**/
		String indexPath = "index";
		/**是创建所有还是更新原来的索引文件。根据配置所有的路径判断是否存在索引数据**/
		boolean create = true;
		LocalDateTime startTime = LocalDateTime.now();
		logger.debug("Indexing to directory 【" + indexPath + "】, start begin:"
			+ startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		try {
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			logger.debug("索引位置:" + dir.toString());
//			Analyzer analyzer = new SmartChineseAnalyzer();
			Analyzer analyzer = new JcsegAnalyzer(JcsegTaskConfig.COMPLEX_MODE);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setInfoStream(new PrintStreamInfoStream(System.out));
			if (create) {
		        /**在索引路径下创建新的索引文件，删除原来的**/
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
		        /**追加**/
		        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docPath);
			writer.close();
			LocalDateTime endTime = LocalDateTime.now();
			logger.debug("end at:" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("建立索引出现异常", e);
		}
	}
	
	/**
	* @Title: indexDocs 
	* @Description: 
	* @param writer
	* @param path
	* @throws IOException
	 */
	private void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	    	Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	    		@Override
	    		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	    			try {
	    				indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	    			} catch (IOException ignore) {
	    				// don't index files that can't be read.
	    			}
	    			return FileVisitResult.CONTINUE;
	    		}
        	});
	      } else {
	        indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	      }
	}
	
	/**
	* @Title: indexDoc 
	* @Description: 单个文件建立索引
	* @param writer
	* @param file
	* @param lastModified
	 * @throws IOException 
	 */
	public void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException{
		try (InputStream stream = Files.newInputStream(file)) {
			String s = "contents";
			/*Analyzer analyzer = writer.getAnalyzer();
			TokenStream tokenStream = analyzer.tokenStream(s, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
			OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
            TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
            tokenStream.reset();
            logger.debug("analyzing : " + file.toString());
            while (tokenStream.incrementToken()) {
                String s1 = offsetAttribute.toString();
                int i1 = offsetAttribute.startOffset();//起始偏移量
                int i2 = offsetAttribute.endOffset();//结束偏移量
                logger.debug(s1 + "[" + i1 + "," + i2 + ":" + typeAttribute.type() + "]" + " ");
            }
            tokenStream.end();
            tokenStream.close();*/
			Document doc = new Document();
			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
		    doc.add(pathField);
		    doc.add(new LongPoint("modified", lastModified));
		    doc.add(new TextField(s, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
		    doc.add(new StoredField("title", file.getFileName().toString()));
		    doc.add(new StringField("id", String.valueOf(file.toString().hashCode()), Field.Store.YES));
		    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		        logger.debug("adding " + file);
		        writer.addDocument(doc);
		    } else {
		    	logger.debug("updating " + file);
		        writer.updateDocument(new Term("path", file.toString()), doc);
		    }
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		Logger logger = LoggerFactory.getLogger(ChineseIndex.class);
		Analyzer analyzer = new JcsegAnalyzer(JcsegTaskConfig.COMPLEX_MODE);
		String s = "contents";
		String con = "花枝招展黑珍珠休息休息好想好想哈羽毛球";
		TokenStream tokenStream = analyzer.tokenStream(s, con);
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String s1 = offsetAttribute.toString();
            int i1 = offsetAttribute.startOffset();//起始偏移量
            int i2 = offsetAttribute.endOffset();//结束偏移量
            logger.debug(s1 + "[" + i1 + "," + i2 + ":" + typeAttribute.type() + "]" + " ");
        }
        tokenStream.end();
        tokenStream.close();
        String queryString = "羽毛球";
        JcsegAnalyzer queryAnalyzer = new JcsegAnalyzer(JcsegTaskConfig.SEARCH_MODE);
	    QueryParser parser = new QueryParser(s, queryAnalyzer);
	    Query query = parser.parse(queryString);
	    logger.debug("查询解析:" + query.toString());
        analyzer.close();
	}
}