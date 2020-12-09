

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//import javax.management.Query;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import edu.stanford.nlp.simple.Sentence;

public class QueryEngine {
	String indexOutPath = "/Users/guojunwei/Downloads/lemmaIndex";
	boolean indexExists = false;
    boolean stem;
    boolean lemma;
    //String inputFilePath ="src/main/resources/input.txt";
    //static String inputFilePath = "/Users/guojunwei/Downloads/wiki-subset-20140602";
    static String inputFilePath;
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index;
    private final String docid = "docid";
    public QueryEngine(String inputFile,boolean stem, boolean lemma){
    	this.lemma = lemma;
    	this.stem = stem;
        inputFilePath =inputFile;
        buildIndex();
    }
     
    /**
     * This method builds index for the documents to the lucene framework
     */
    private void buildIndex() {
    	try {
            //String fileName = "input.txt";
    		index = FSDirectory.open(new File(indexOutPath).toPath());
        	IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w;
            w = new IndexWriter(index, config);
            String inputFilePath ="/Users/guojunwei/Downloads/wiki-subset-20140602";
            //String inputFilePath ="/Users/guojunwei/Downloads/testfolder";

            File folder = new File(inputFilePath);	
            File[] fs = folder.listFiles();
        	for (File f: fs) {
        		if (!f.isDirectory()) {
        			
        			Scanner inputScanner = new Scanner(f);
        				String content = "";
        				String docTitle = "";
        				String categories = "";
        	            while (inputScanner.hasNextLine()) {
        	            	//need to remove TPL&url
        	            	String temp = inputScanner.nextLine();
        	            	int tempLength = temp.length();
        	            	if (temp.length() > 4 && temp.startsWith("[[") && temp.endsWith("]]")) {
    	            			
    	            			if (!docTitle.equals("")) {
    	            				System.out.println("filename: " + f.getName());
    	            				addDoc(w, docTitle, categories, content);
    	            			}
    	            			content = "";
    	            			categories = "";
        	            		docTitle = temp.substring(2, tempLength - 2);
        	            		//System.out.println(docTitle);
        	            	} else if (temp.equals("")) {
        	            		continue;
        	            	} else if (temp.indexOf("CATEGORIES") == 0){
        	            		categories = temp.substring(12);		//get all the text from temp after "CATEGORIES"
        	            		//categories = categories.replaceAll("\\p{Punct}",""); 
        	            	} else if (temp.length() > 2 && temp.startsWith("=") && temp.endsWith("=")) {
        	            		while (temp.length() > 2 && temp.startsWith("=") && temp.endsWith("=")) {
        	            			temp = temp.substring(1, temp.length() - 1);
        	            		}
        	            		if (!temp.equals("See also") && !temp.equals("References") && !temp.equals("Further reading")  
        	            				&&	!temp.equals("External links") && !temp.equals("Examples")) {
        	            			content += temp;
        	            			content += " ";
        	            		}
        	            	} else {
        	            		String removeTPL = temp.replaceAll("\\[tpl\\].*?\\[/tpl\\]", "");
        	            		//String removePunctuation = removeTPL.replaceAll("\\p{Punct}",""); 
        	            		content += removeTPL;
        	            		content += " ";
        	            	}
        	            	//addDoc(w, content, docID);
        	            }
        	            inputScanner.close();
//        	            System.out.println("LASTtitle: " + docTitle);
//        	            System.out.println("LASTfilename: " + f.getName());
//            			System.out.println("LASTcategories: " + categories);
//            			System.out.println("LASTcontent: " + content);
            			System.out.println();
        	            addDoc(w, docTitle, categories, content);
        	       
        	        
        	        indexExists = true;	
                    
        			//System.out.println(f);
        		}
        	}
        	
        	w.close();
            
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
        
        indexExists = true;
    }
    
    // change regular word to lemma form
 	private String makeLemma(String lemmaWord, String originWord) {
 		Sentence sent = new Sentence(originWord.toLowerCase());
 		List<String> lemmasList = sent.lemmas();
 		for (String word: lemmasList){
 			lemmaWord += word;
 			lemmaWord += " ";
 		}
 		return lemmaWord;
 	}
 	
 	// change regular word to stem form
 	private String makeStem(String stemWord, String originWord) {
 		Sentence sent = new Sentence(originWord.toLowerCase());
 		List<String> stemList = sent.words();
 		for (String word: stemList){
 			stemWord += getStem(word);
 			stemWord += " ";
 		}
 		return stemWord;
 	}
    
    /**
     * Your indexed documents should have two fields: 
     * a tokenized and searchable text field containing the text of the document (i.e., each line without the first token), 
     * and another non-tokenized field containing the document name (the first token in each line), e.g., Doc1.
     * @param w
     * @param title
     * @param isbn
     * @throws IOException
     */
    private void addDoc(IndexWriter w, String title, String categories,String content) throws IOException {
    	  Document doc = new Document();
    	  doc.add(new StringField("title", title, Field.Store.YES));
    	  String category = "";
    	  String cont = "";
    	  categories = categories.trim();
    	  content = content.trim();
    	  if (categories.equals("")) {
    		  categories = ":";
    	  }
    	  if (content.equals("")) {
    		  content = ":";
    	  }
    	  
    	  
    	  if (stem) {
    		  category = makeStem(category, categories);
    		  cont = makeStem(cont, content);
    	  } else if (lemma) {
    		  category = makeLemma(category, categories);
    		  cont = makeLemma(cont, content);
    	  } else {
    		  category = categories.toLowerCase();
    		  cont = content.toLowerCase();
    	  }
    	  
  		  doc.add(new TextField("categories", category, Field.Store.YES));
  		  doc.add(new TextField("content", cont, Field.Store.YES)); 
  		  w.addDocument(doc);
  		  System.out.println("title: " + title);
  		  System.out.println("categories: " + category);
  		  System.out.println("content: " + cont);
  		  System.out.println();
    }
    
    
    private String getStem(String word) {
		PorterStemmer stem = new PorterStemmer();
		stem.setCurrent(word);
		stem.stem();
		return stem.getCurrent();
	}
    

    public static void main(String[] args ) throws FileNotFoundException, IOException {
    	//QueryEngine objQueryEngineStem = new QueryEngine(inputFilePath, true, false);
    	QueryEngine objQueryEngineLemma = new QueryEngine(inputFilePath, false, true);
    	//QueryEngine objQueryEngineNeither = new QueryEngine(inputFilePath, false, false);
    	String[] query13a = {"BSI", "certifies"};
        objQueryEngineLemma.runQ1_1(query13a); 
    }

    public void createIndex() {
    	
        
    }
    /**
     * find docid and length for information retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_1(String[] query) throws java.io.FileNotFoundException,java.io.IOException{
        if(!indexExists) {
            buildIndex();
        } 
        String querystr = null;
        for (int i = 0; i < query.length; i++) {
			querystr += query[i] + " ";
		} 
        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 10;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }

    /**
     * find docid and length for information AND retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_a(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String querystr = query[0] + " AND " + query[1];
//        for (int i = 0; i < query.length; i++) {
//			querystr += query[i] + " ";
//		} 
//        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 4;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }

    /**
     * find docid and length for information AND NOT retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_b(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
    	if(!indexExists) {
            buildIndex();
        }
    	String querystr = query[0] + " AND NOT " + query[1];
//        for (int i = 0; i < query.length; i++) {
//			querystr += query[i] + " ";
//		} 
//        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 4;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }
     
    

    

}
