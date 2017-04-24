package cn.charlie166.word.lucene.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: FileUtils 
* @Description: 文件工具类
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月24日 
*
 */
public class FileUtils {

	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	* @Title: readFile 
	* @Description: 从指定路径读取文件内容
	* @param filepath 文件路径
	* @return 文件内容
	 */
	public static String readFile(String filepath){
		if(filepath == null || "".equals(filepath.trim())){
			logger.debug("文件路径不能为空");
			return null;
		}
		File file = new File(filepath);
		if(file.exists() && file.isFile()){
            try {
            	BufferedReader reader = new BufferedReader(new InputStreamReader(  
                        new FileInputStream(file), "UTF-8"));  
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {  
                    str.append(line + "\r\n");  
                }  
                reader.close(); 
				return str.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.debug("文件不存在");
		}
		return null;
	}
}