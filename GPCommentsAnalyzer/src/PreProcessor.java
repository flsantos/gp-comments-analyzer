import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ptstemmer.Stemmer;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;


public class PreProcessor {

	List<String> words = new ArrayList<String>();
	File comments;
	File portugueseWords;
	
	
	Comparator<String> comparator = new Comparator<String>() {
		  public int compare(String o1, String o2) {
		    if (o1.equals(o2)) {
		      return o1.compareTo(o2);
		    }
		    return o1.compareTo(o2);
		  }
		};
	
	public PreProcessor(File comments, File portugueseWords) {
		this.comments = comments;
		this.portugueseWords = portugueseWords;
		
	
	}
	
	public PreProcessor() {
		
	}


	private void printWordsNotDictionary() {
		BufferedReader br = null;
		String sCurrentLine;
		String st;
		String[] sArray;
		 
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(portugueseWords), "UTF8"));

			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				for (int k = 0; k < sArray.length; k++) {
					
					st = sArray[k];
					st = st.toLowerCase();
					//sCurrentLine = removeAccents(sCurrentLine);
					words.add(st);
					//System.out.println(sCurrentLine);
					
				}
			}

			Collections.sort(words, comparator);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		BufferedWriter bufferedWriter = null;
        
            
		try {

		/*	//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(comments);
			
			NodeList comm = dom.getElementsByTagName("commentText");
            //Construct the BufferedWriter object
			*/
			ResultSet rs = getAllComentsFromDatabase();
			
            bufferedWriter = new BufferedWriter(new FileWriter("girias2.txt"));

            int k = 1;
			while (rs.next()) {
				System.out.println(k++);
				
				String comment = rs.getString("text");
				comment = comment.replaceAll("  ", " ");
				comment = comment.replaceAll("\t", " ");
				comment = comment.replaceAll("!", " ! ");
				comment = comment.replaceAll("\\?", " ? ");
				comment = comment.replaceAll("\\.", " \\. ");
				comment = comment.replaceAll(",", " , ");
				comment = comment.toLowerCase();
				//comment = removeAccents(comment);
				List<String> commentWords = Arrays.asList(comment.split(" "));
				for (String s : commentWords) {
					if (Collections.binarySearch(words, s, comparator) < 0) {
						//System.out.println(s);
						bufferedWriter.write(s+"\n");
						//System.out.println("-->"+comment+"<--");
					}
				}
				
			}
			
			

		}catch(IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	
	private void printStatistics() {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(comments);
			
			Integer star1ContGeral = 0;
			Integer star2ContGeral = 0;
			Integer star3ContGeral = 0;
			Integer star4ContGeral = 0;
			Integer star5ContGeral = 0;
			
			ArrayList<Integer> commentsCount = new ArrayList<Integer>();
			
			NodeList comm = dom.getElementsByTagName("app");
			for (int i=0; i<comm.getLength(); i++) {
				
				
				NodeList commNodeList = comm.item(i).getChildNodes();
				System.out.print(comm.item(i).getAttributes().getNamedItem("appId").getNodeValue() + " --- " + commNodeList.getLength()/2 + " coment�rios");
				commentsCount.add(commNodeList.getLength()/2);
				Integer star;
				Integer star1Cont = 0;
				Integer star2Cont = 0;
				Integer star3Cont = 0;
				Integer star4Cont = 0;
				Integer star5Cont = 0;
				//N�o entendi porque, mas nesse parser pra pegar o first child come�a do 1 e incrementa de 2 em 2.
				for (int j=1; j < commNodeList.getLength(); j=j+2) { 
					star = Integer.parseInt(commNodeList.item(j).getChildNodes().item(1).getTextContent());
					switch (star) {
					case 1: star1Cont++; star1ContGeral++;
						break;
					case 2: star2Cont++; star2ContGeral++;
						break;
					case 3: star3Cont++; star3ContGeral++;
						break;
					case 4: star4Cont++; star4ContGeral++;
						break;
					case 5: star5Cont++; star5ContGeral++;
						break;
					default:
						break;
					}
					
				}
				System.out.println(" --- *:" + star1Cont + " **:" + star2Cont + " ***:" + star3Cont +  " ****:" + star4Cont + " *****:" + star5Cont);
				
			}

			
			System.out.println("\n\nDistribui��o de estrelas entre todos os aplicativos:");
			System.out.println("*:" + star1ContGeral + " **:" + star2ContGeral + " ***:" + star3ContGeral +  " ****:" + star4ContGeral + " *****:" + star5ContGeral);
			
			System.out.println("\n\nN�mero de coment�rios");
			for (Integer n : commentsCount) {
				System.out.print(n+" ");
			}

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	

	private void transformXMLintoTXT() {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(comments);
			
			NodeList comm = dom.getElementsByTagName("comment");
			for (int i=0; i<comm.getLength(); i++) {

				
				System.out.println(comm.item(i).getChildNodes().item(5).getTextContent());
				
				
			}
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		
		/*String s = "";
		System.out.println(s.split(" ").length);
		System.out.println(s.replaceAll("\\.", " \\. "));*/
		PreProcessor p = new PreProcessor(new File("comments_max.xml"), new File("dict_full.txt"));
		
		//PreProcessor p = new PreProcessor();
		
		
		//p.printWordsNotDictionary();
		
		//p.printStatistics();
		
		//p.printStatistics2();
		
		//p.transformXMLintoTXT();
		
		//p.storeInMySQL();
		
		//p.getSampleFromDatabase();
		
		//p.updateCommentsDroppRepeatedLetters();
		
		//p.updateCommentsCorrect2000WrongWords();
		
		//p.countCommentsWithError();
		
		//p.printStatisticsNumberOfWords();
		
		
		//p.getNumberOfWordsFromNonOpinionComments();
		
		//p.updateCommentsRemoveStopWords();
		
		
		
		//p.createFilesForEachComment();
		
		//p.createDictforR();
		
		//p.readCSVCorpus();
		
		//p.rewriteCSVWithId();
		
		//p.getCommentsLeft1Star();
		
		//p.rewriteCSVWithoutAccents();
		
		//p.rewriteCSVWithDict2000Corrections();
		
		//p.rewriteCSVWithDict2000CorrectionsAndStemmer();
		
		//p.rewriteCSVWithDict2000CorrectionsAspell();
		
		
	}
	
	private void rewriteCSVWithDict2000CorrectionsAspell() {

		BufferedReader br = null;
		BufferedReader br1 = null;
		String sCurrentLine = null;
		String sCurrentLine1 = null;
		String[] sArray;
		String[] sArray1;
		String st = null;
		String word = null;
		String aux = null;

		try {
			
			br1 = new BufferedReader(new InputStreamReader(new FileInputStream(this.portugueseWords), "UTF8"));

			while ((sCurrentLine1 = br1.readLine()) != null) {
				//sCurrentLine1 = removeAccents(sCurrentLine1);
				sArray1 = sCurrentLine1.split(" ");
				for (int k = 0; k < sArray1.length; k++) {
					st = sArray1[k];
					st = st.toLowerCase();
					this.words.add(st);
				}
			}
			Collections.sort(this.words, this.comparator);
			

			HashMap<String, String> dict = new HashMap<String, String>();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario2000.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				word = "";
				for (int i = 1; i < sArray.length; i++)
					word = word.concat(sArray[i]);
				word = word.replace("\"", "");
				if (!word.isEmpty()) {
					dict.put(sArray[0], word);
				}
			}

			CsvWriter csvWriter = new CsvWriter("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_com_correcao_2000_aspell.csv");
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_csv_correto.csv");
			csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
			String text = "";
			String[] record = null;

			String header[] = {"class", "id", "rating", "comment"};
			csvWriter.writeRecord(header);
			csvWriter.setTextQualifier('\"');
			csvWriter.setUseTextQualifier(true);
			csvWriter.setForceQualifier(true);

			ASpellWrapper aspell = new ASpellWrapper("pt_br");
			
			String[] words = null;
			List<SpellCheckResult> list = null;
			
			int i = 0;
			csv.readHeaders();
			while (csv.readRecord()) {

				text = csv.get(3);
				text = text.replace("\t", " ");
				text = text.replace(".", " . ");
				text = text.replace(",", " , ");
				text = text.replace("?", " ? ");
				text = text.replace("!", " ! ");
				
				text = text.replace("#", "");
				text = text.replace("*", "");
				text = text.replace("&", "");
				text = text.replace("%", "");
				text = text.replace("$", "");
				text = text.replace("@", "");
				
				text = text.toLowerCase();
				text = " "+text+" ";

				for (String str : dict.keySet()) {
					aux = " "+str+" ";
					if (text.contains(aux)) {
						text = text.replace(aux, " "+dict.get(str)+" ");
					}
				}

				
				
				//Aspell correcting
				words = text.split(" ");
				text = "";
				for (String palavra : words) {
					if (Collections.binarySearch(this.words, palavra, comparator) < 0) {
						if ((!palavra.trim().isEmpty()) && (palavra.trim().toCharArray()[0] == '#'))
							palavra = palavra.substring(1);
						list = aspell.checkString(palavra.trim());
						if (list.size() > 0) {
							text = text + " " + list.get(0).getSuggestions().get(0);
						}
						else {
							text = text + " " + palavra;
						}
					}
					else {
						text = text + " " + palavra;
					}
					
				}
				
				//Remove accents
				text = Normalizer.normalize(text, Normalizer.Form.NFD);
				text = text.replaceAll("[^\\p{ASCII}]", "");
				text = text.toLowerCase();
					

				record = new String[4];
				record[0] = csv.get(0);
				record[1] = csv.get(1);
				record[2] = csv.get(2);
				record[3] = text;

				csvWriter.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
				csvWriter.writeRecord(record);
				System.out.println(i++);
				System.out.println(text);
			}				



			csvWriter.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ASpellException e) {
			e.printStackTrace();
		}

	}
	
	

	private void rewriteCSVWithDict2000CorrectionsAndStemmer() {

		BufferedReader br = null;
		String sCurrentLine = null;
		String[] sArray;
		String word = null;
		String aux = null;

		try {

			HashMap<String, String> dict = new HashMap<String, String>();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario2000.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				word = "";
				for (int i = 1; i < sArray.length; i++)
					word = word.concat(sArray[i]);
				word = word.replace("\"", "");
				if (!word.isEmpty()) {
					dict.put(sArray[0], word);
				}
			}

			CsvWriter csvWriter = new CsvWriter("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_com_correcao_2000_stemming.csv");
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_csv_correto.csv");
			csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
			String text = "";
			String[] record = null;

			String header[] = {"class", "id", "rating", "comment"};
			csvWriter.writeRecord(header);
			csvWriter.setTextQualifier('\"');
			csvWriter.setUseTextQualifier(true);
			csvWriter.setForceQualifier(true);

			Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.ORENGO);
			st.enableCaching(1000);
			
			String[] words = null;

			int i = 0;
			csv.readHeaders();
			while (csv.readRecord()) {

				text = csv.get(3);
				text = text.replace("\t", " ");
				text = text.replace(".", " . ");
				text = text.replace(",", " , ");
				text = text.replace("?", " ? ");
				text = text.replace("!", " ! ");
				text = text.toLowerCase();
				text = " "+text+" ";

				for (String str : dict.keySet()) {
					aux = " "+str+" ";
					if (text.contains(aux)) {
						text = text.replace(aux, " "+dict.get(str)+" ");
					}
				}

				//Remove accents
				text = Normalizer.normalize(text, Normalizer.Form.NFD);
				text = text.replaceAll("[^\\p{ASCII}]", "");
				text = text.toLowerCase();
				
				//Stemming
				words = text.split(" ");
				text = "";
				for (String palavra : words) {
					text = text + " " + st.wordStemming(palavra);
				}

				record = new String[4];
				record[0] = csv.get(0);
				record[1] = csv.get(1);
				record[2] = csv.get(2);
				record[3] = text;

				csvWriter.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
				csvWriter.writeRecord(record);
			}				

			System.out.println(i++);


			csvWriter.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void rewriteCSVWithDict2000Corrections() {
		
		BufferedReader br = null;
		String sCurrentLine = null;
		String[] sArray;
		String word = null;
		String aux = null;
		
		try {

			HashMap<String, String> dict = new HashMap<String, String>();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario2000.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				word = "";
				for (int i = 1; i < sArray.length; i++)
					word = word.concat(sArray[i]);
				word = word.replace("\"", "");
				if (!word.isEmpty()) {
					dict.put(sArray[0], word);
				}
			}

			CsvWriter csvWriter = new CsvWriter("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_com_correcao_2000.csv");
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_csv_correto.csv");
			csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
			String text = "";
			String[] record = null;

			String header[] = {"class", "id", "rating", "comment"};
			csvWriter.writeRecord(header);
			csvWriter.setTextQualifier('\"');
			csvWriter.setUseTextQualifier(true);
			csvWriter.setForceQualifier(true);


			int i = 0;
			csv.readHeaders();
			while (csv.readRecord()) {

				text = csv.get(3);
				text = text.replace("\t", " ");
				text = text.replace(".", " . ");
				text = text.replace(",", " , ");
				text = text.replace("?", " ? ");
				text = text.replace("!", " ! ");
				text = text.toLowerCase();
				text = " "+text+" ";
				
				for (String str : dict.keySet()) {
					aux = " "+str+" ";
					if (text.contains(aux)) {
						text = text.replace(aux, " "+dict.get(str)+" ");
					}
				}
				
				//Remove accents
				text = Normalizer.normalize(text, Normalizer.Form.NFD);
				text = text.replaceAll("[^\\p{ASCII}]", "");
				text = text.toLowerCase();

				record = new String[4];
				record[0] = csv.get(0);
				record[1] = csv.get(1);
				record[2] = csv.get(2);
				record[3] = text;

				csvWriter.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
				csvWriter.writeRecord(record);
			}				

			System.out.println(i++);


			csvWriter.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void getCommentsLeft1Star() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		


		try {		
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\Dropbox\\PIC_Ladeira\\Pre Processamento\\Corpus10K\\60-40\\rating1.csv");
			Statement stmt = null;
			ResultSet rs = null;
			String query = "";
			String[] record = null;
			int i = 0;
			String ids = "(";
			while (csv.readRecord()) {
				ids = ids + csv.get(1) + ", ";
			
			}
			ids = ids.substring(0, ids.length()-2);
			ids = ids + ")";
			
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			stmt = (Statement) con.createStatement();
			query = "select id, rating, text_original from comentarios where rating = 1 and id not in ";
			query = query + ids;
			query = query + " order by rand() limit 1000;";
			System.out.println(query);
			rs = (ResultSet) stmt.executeQuery(query);
			
			
			CsvWriter csvWriter = new CsvWriter("rating1_restantes.csv");
			
			while (rs.next()) {
			
				record = new String[3];
				record[0] = rs.getString("id");
				record[1] = rs.getString("rating");
				record[2] = rs.getString("text_original");
				
				csvWriter.writeRecord(record);
				System.out.println(i++);
			}				
			
			
				
			
			csvWriter.close();
			con.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void rewriteCSVWithoutAccents() {
		try {

		
			CsvWriter csvWriter = new CsvWriter("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_sem_acentuacao.csv");
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\UnB\\Projeto Ladeira\\data\\10mil_teste\\corpus_csv_correto.csv");
			csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
			String text = "";
			String[] record = null;
			
			String header[] = {"class", "id", "rating", "comment"};
			csvWriter.writeRecord(header);
			csvWriter.setTextQualifier('\"');
			csvWriter.setUseTextQualifier(true);
			csvWriter.setForceQualifier(true);
			
			
			int i = 0;
			csv.readHeaders();
			while (csv.readRecord()) {
			
				text = csv.get(3);
				text = Normalizer.normalize(text, Normalizer.Form.NFD);
				text = text.replaceAll("[^\\p{ASCII}]", "");
				text = text.toLowerCase();
				
				record = new String[4];
				record[0] = csv.get(0);
				record[1] = csv.get(1);
				record[2] = csv.get(2);
				record[3] = text;
				
				csvWriter.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
				csvWriter.writeRecord(record);
			}				
			
			System.out.println(i++);
					
			
			csvWriter.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void rewriteCSVWithId() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		


		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			
			
		
			CsvWriter csvWriter = new CsvWriter("rating5_com_ids.csv");
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\Dropbox\\PIC_Ladeira\\Pre Processamento\\Corpus10K\\60-40\\rating5.csv");
			Statement stmt = null;
			ResultSet rs = null;
			String query = "";
			String text = "";
			String[] record = null;
			int i = 0;
			int err = 0;
			while (csv.readRecord()) {
			
				text = csv.get(3);
				
				stmt = (Statement) con.createStatement();
				query = "select * from comentarios where rating = 5 and text_original = \"";
				query = query + text;
				query = query + "\" limit 1;";
				rs = (ResultSet) stmt.executeQuery(query);
				
				if (rs.next()) {
				
					record = new String[4];
					record[0] = csv.get(0);
					record[1] = rs.getString("id");
					record[2] = csv.get(2);
					record[3] = text;
					
					while (rs.next()) {
						err++;
					}
					csvWriter.writeRecord(record);
				}				
				
				System.out.println(i++);
					
			}
			csvWriter.close();
			con.close();
			
			System.out.println("Erro:" + err);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private void readCSVCorpus() {
		int r1 = 0;
		int r0 = 0;
		int rneg = 0;
		int err = 0;
		
		try {
			CsvReader csv = new CsvReader("C:\\Users\\fernando\\Dropbox\\PIC_Ladeira\\Pre Processamento\\Corpus10K\\60-40\\corpus_completo.csv");
			while (csv.readRecord()) {
				if (csv.get(0).equals("1")) {
					r1++;
				} else if (csv.get(0).equals("0")) {
					r0++;
				} else if (csv.get(0).equals("-1")) {
					rneg++;
				} else {
					System.out.println(csv.get(0));
					System.out.println(csv.get(1));
					System.out.println(csv.get(2));
					System.out.println(csv.get(3));
					err++;
				}
					
			}
			
			System.out.println("Classe 1: "+ r1);
			System.out.println("Classe 0: "+ r0);
			System.out.println("Classe -1: "+ rneg);
			System.out.println("Sem classe: "+ err);
			System.out.println("Total :"+ (err+rneg+r0+r1));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private void createDictforR() {
		BufferedReader br = null;
		String sCurrentLine = null;
		String[] sArray;
		String word = null;
		HashMap<String, String> dict = new HashMap<String, String>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario2000.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				word = "";
				for (int i = 1; i < sArray.length; i++)
					word = word.concat(sArray[i]);
				word = word.replace("\"", "");
				if (!word.isEmpty()) {
					dict.put(sArray[0], word);
				}
			}
			
			
			BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter("dictionaryForR.txt"));
				for (String s : dict.keySet()) {
					out.write(s+"\n");
					out.write(dict.get(s)+"\n");
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void createFilesForEachComment() {
		ResultSet rs = getAllComentsFromDatabase();
		try {
			int i = 0;
			while(rs.next()) {
				writeCommentFile(rs.getInt("rating"),rs.getInt("id"), rs.getString("text_original"));
				System.out.println(i++);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	private void writeCommentFile(int rating, int id, String comment) {
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("corpus/"+rating+"/comment"+id+".txt"));
			out.write(comment);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//This also will remove all accents from the words
	private void updateCommentsRemoveStopWords() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		BufferedReader br = null;
		String sCurrentLine = null;
		String[] sArray;
		String word = null;


		ArrayList<String> stopwords = new ArrayList<String>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("stopwords.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				stopwords.add(sCurrentLine);
			}


			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			Statement stmt2 = (Statement) con.createStatement();
			ResultSet rs;
			String query;
			query = "select * from comentarios;";
			rs = (ResultSet) stmt.executeQuery(query);
			String comentario = null;
			String aux, aux2;
			int j = 1;
			int corr = 0;
			while (rs.next()) {
				comentario = rs.getString("stemmed");
				comentario = comentario.toLowerCase();
				comentario = replaceQuote(comentario);
				comentario = removeAccents(comentario);
				comentario = comentario.replace("\\", "\\\\");
				for (String str : stopwords) {
					aux = " "+str+" ";
					if (comentario.contains(aux)) {
						comentario = comentario.replace(aux, " ");
					}
				}
				//System.out.println(comentario);
				//System.out.println();
				
				comentario = comentario.replaceAll(" [0123456789]+ ", "");
				comentario = comentario.replaceAll(" [.,?!:%#\\(\\)]+ ", "");
				comentario = comentario.replaceAll(" [abcdefghijklmnopqrstuvwxyz] ", "");
				
				Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.ORENGO);
				st.enableCaching(1000);
				comentario = StringUtils.join(st.phraseStemming(comentario), ' ');
				comentario = " " +comentario+" "; 
				
				
				
				if (!comentario.equals(rs.getString("stemmed"))) {
					query = "update comentarios set stemmed = '"+ comentario +"' where id = "+rs.getInt("id");
					stmt2.executeUpdate(query);
					corr++;
				}
				System.out.println(j++);
			}
			

			System.out.println("Corre��es: " + corr);
			con.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}


	private void getNumberOfWordsFromNonOpinionComments() {

		String sCurrentLine;
		BufferedReader br, br2;
		String ids = "";
		ResultSet rs = null;
		int numOfComments = 0;
		int sumOfWords = 0;
		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("samples_ids.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				ids = ids + sCurrentLine + ", ";
			}
			ids = ids.substring(0, ids.length()-2);
			
			br2 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("non_opinion_comment_ids.txt")), "UTF8"));
			while ((sCurrentLine = br2.readLine()) != null) {
				ids = ids.replaceAll(", "+sCurrentLine, "");
			}
			
			rs = getCommentsFromDatabase(ids);
			
			while (rs.next()) {
				numOfComments++;
			
				sumOfWords = sumOfWords + rs.getString("text").split(" ").length;
			}
			
			System.out.println("Total de coment�rios: " +numOfComments);
			System.out.println("Total de palavras: " +sumOfWords);
			System.out.println("M�dia de palavras por coment�rio: " +(sumOfWords/numOfComments));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private void printStatisticsNumberOfWords() {
		String comentario = null;
		int size, rating;
		
		int mat[][] = new int[5][5];
		float matf[][] = new float[5][5];
		int total = 0;
		try {
			ResultSet rs = getAllComentsFromDatabase();
			while (rs.next()) {
				total++;
				comentario = rs.getString("text");
				rating = rs.getInt("rating");
				size = comentario.split(" ").length;
				
				switch (rating) {
				case 1:
					mat = increaseMatrix(mat, size, 0); break;
				case 2:
					mat = increaseMatrix(mat, size, 1); break;
				case 3:
					mat = increaseMatrix(mat, size, 2); break;
				case 4:
					mat = increaseMatrix(mat, size, 3); break;
				case 5:
					mat = increaseMatrix(mat, size, 4); break;
				default:
					break;
				}
				
			}
			
			for (int i = 0; i< 5; i++)
				for (int j = 0; j< 5; j++)
					matf[i][j] = (float)mat[i][j] / (float)total;

			
			System.out.println("N�mero de comentarios:");
			for (int i = 0; i< 5; i++) {
				for (int j = 0; j< 5; j++) {
					System.out.print(mat[i][j]+ " ");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println("Porcentagem de coment�rios:");
			for (int i = 0; i< 5; i++) {
				for (int j = 0; j< 5; j++) {
					System.out.print(matf[i][j]+ " ");
				}
				System.out.println();
			}
			
			Arrays.toString(mat);
			Arrays.toString(matf);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int[][] increaseMatrix(int[][] mat, int size, int i) {
		if (size <= 5)
			mat[0][i]++;
		else if ((size > 5) && (size <= 10))
			mat[1][i]++;
		else if ((size > 10) && (size <= 25))
			mat[2][i]++;
		else if ((size > 25) && (size <= 40))
			mat[3][i]++;
		else if (size > 40)
			mat[4][i]++;
		
		
		return mat;
	}
	


	private void countCommentsWithError() {
		String[] words = new String[37007];
		String sCurrentLine;
		String comentario;
		
		
		int size, rating;
		
		int mat[][] = new int[5][5];
		float matf[][] = new float[5][5];
		int total = 0;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario_nao_corrigido.txt")), "UTF8"));

			int i = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				words[i] = sCurrentLine.trim();
				i++;
			}
			
			int count = 0;
			int j  = 0;
			ResultSet rs = getAllComentsFromDatabase();
			boolean error = false;
			while (rs.next()) {
				comentario = rs.getString("text");
				error = false;
				for (String str : words) {
					if (comentario.contains(" "+str+" ")) {
						count++;
						error = true;
						break;
					}
				}
				
				//if (error == false) {
					System.out.println("Rating: "+rs.getInt("rating"));
					System.out.println("Coment�rio: "+rs.getString("bag_of_words"));
					System.out.println("\n");
				//}
				
				
				total++;
				comentario = rs.getString("text");
				rating = rs.getInt("rating");
				size = comentario.split(" ").length;
				
				switch (rating) {
				case 1:
					mat = increaseMatrix(mat, size, 0); break;
				case 2:
					mat = increaseMatrix(mat, size, 1); break;
				case 3:
					mat = increaseMatrix(mat, size, 2); break;
				case 4:
					mat = increaseMatrix(mat, size, 3); break;
				case 5:
					mat = increaseMatrix(mat, size, 4); break;
				default:
					break;
				}
				
				//System.out.println(j++);
				
				
			}
			
			
			for (int k = 0; k< 5; k++)
				for (int l = 0; l< 5; l++)
					matf[k][l] = (float)mat[k][l] / (float)total;

			
			for (int k = 0; k< 5; k++)
				for (int l = 0; l< 5; l++)
					System.out.print(mat[k][l]+"\t");
				System.out.println();
			
			for (int k = 0; k< 5; k++)
				for (int l = 0; l< 5; l++)
					System.out.print(matf[k][l]+"\t");
				System.out.println();
			System.out.println(Arrays.toString(mat));
			System.out.println(Arrays.toString(matf));
			System.out.println();
			
			System.out.println("Comentarios errados encontrados:" + count);
			
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private void updateCommentsCorrect2000WrongWords() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		BufferedReader br = null;
		String sCurrentLine = null;
		String[] sArray;
		String word = null;


		HashMap<String, String> dict = new HashMap<String, String>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("dicionario2000.txt")), "UTF8"));
			while ((sCurrentLine = br.readLine()) != null) {
				sArray = sCurrentLine.split(" ");
				word = "";
				for (int i = 1; i < sArray.length; i++)
					word = word.concat(sArray[i]);
				word = word.replace("\"", "");
				if (!word.isEmpty()) {
					dict.put(sArray[0], word);
				}
			}


			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			Statement stmt2 = (Statement) con.createStatement();
			ResultSet rs;
			String query;
			query = "select * from comentarios;";
			rs = (ResultSet) stmt.executeQuery(query);
			String comentario = null;
			String aux, aux2;
			int j = 1;
			int corr = 0;
			while (rs.next()) {
				comentario = rs.getString("text");
				comentario = " " + comentario + " ";
				comentario = comentario.replace('\t', ' ');
				comentario = comentario.replace("."," . ");
				comentario = comentario.replace(","," , ");
				comentario = comentario.replace("?"," ? ");
				comentario = comentario.replace("!"," ! ");
				for (String str : dict.keySet()) {
					aux = " "+str+" ";
					if (comentario.contains(aux)) {
						comentario = comentario.replace(aux, " "+dict.get(str)+" ");
					}
				}
				//System.out.println(comentario);
				//System.out.println();
				
				if (!comentario.equals(rs.getString("text"))) {
					comentario = comentario.toLowerCase();
					comentario = replaceQuote(comentario);
					comentario = comentario.replace("\\", "\\\\");
					query = "update comentarios set text = '"+ comentario +"' where id = "+rs.getInt("id");
					stmt2.executeUpdate(query);
					corr++;
				}
				System.out.println(j++);
			}
			

			System.out.println("Corre��es: " + corr);
			con.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}


	private void updateCommentsDroppRepeatedLetters() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			Statement stmt2 = (Statement) con.createStatement();
			ResultSet rs;
			String query;
			query = "select * from comentarios;";
			rs = (ResultSet) stmt.executeQuery(query);
			String comentario;
			String aux, aux2;
			char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
			int j = 1;
			while (rs.next()) {
				comentario = rs.getString("text_original");
				for (int i=0; i<52;i++) {
					aux = new StringBuilder().append(letters[i]).append(letters[i]).append(letters[i]).toString();
					aux2 = new StringBuilder().append(letters[i]).append(letters[i]).toString();
					while (comentario.contains(aux)) {
						comentario = comentario.replaceFirst(aux, aux2);
					}
				}
				comentario = comentario.toLowerCase();
				comentario = replaceQuote(comentario);
				comentario = comentario.replace("\\", "\\\\");
				query = "update comentarios set text = '"+ comentario +"' where id = "+rs.getInt("id");
				stmt2.executeUpdate(query);
				System.out.println(j++);
				
			}
			
			con.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	private ResultSet getAllComentsFromDatabase() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs;
			String query;
			query = "select * from comentarios;";
			rs = (ResultSet) stmt.executeQuery(query);
			
			//con.close();
			return rs;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ResultSet getCommentsFromDatabase(String ids) {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		


		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs;
			String query;
			query = "select * from comentarios where id in (";
			query = query + ids;
			query = query + ");";
			rs = (ResultSet) stmt.executeQuery(query);
			
			//con.close();
			return rs;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getSampleFromDatabase() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {

			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs;
			/*PreparedStatement stmt2 = (PreparedStatement) con.prepareStatement("SELECT id FROM comentarios WHERE rating = ? ORDER BY RAND( ) LIMIT ?");
			stmt2.setInt(1, 1);
			stmt2.setInt(2, 233);
			rs = (ResultSet) stmt2.executeQuery();
			while(rs.next()) {
				System.out.print(rs.getString("id")+",");
			}
			System.out.println();
			stmt2.setInt(1, 2);
			stmt2.setInt(2, 62);
			rs = (ResultSet) stmt2.executeQuery();
			while(rs.next()) {
				System.out.print(rs.getString("id")+",");
			}
			System.out.println();
			stmt2.setInt(1, 3);
			stmt2.setInt(2, 142);
			rs = (ResultSet) stmt2.executeQuery();
			while(rs.next()) {
				System.out.print(rs.getString("id")+",");
			}
			System.out.println();
			stmt2.setInt(1, 4);
			stmt2.setInt(2, 308);
			rs = (ResultSet) stmt2.executeQuery();
			while(rs.next()) {
				System.out.print(rs.getString("id")+",");
			}
			System.out.println();
			stmt2.setInt(1, 5);
			stmt2.setInt(2, 1655);
			rs = (ResultSet) stmt2.executeQuery();
			while(rs.next()) {
				System.out.print(rs.getString("id")+",");
			}
			System.out.println();*/
			
			
			String query;
			String ids = "122423,156088,138745,33564,6051,103741,95406,27181,169104,190416,34826,91957,85916,161770,88952,34354,16748,187339,32352,174166,121956,107872,157302,29567,8920,183966,161764,16244,156509,62384,83904,77158,118796,129206,173317,136380,9864,77805,12919,62809,183109,9653,44800,70447,6855,128048,70596,163681,12731,83556,180154,83823,140664,185198,82205,34395,59469,134650,77440,180301,128187,15882,32514,84232,186792,23534,154519,94907,183258,85370,83608,61483,189604,32704,177326,6347,107889,22083,59853,32148,105815,5863,127758,5769,127281,66392,167511,78241,119264,168678,32722,11956,59167,65720,5912,184733,62930,182100,83248,183668,77828,8114,190861,103859,84465,121843,41467,11252,77648,58671,141750,137733,50061,166595,89330,32993,9434,122906,150405,153806,121730,39849,723,74475,9654,84893,162043,183814,190762,71545,65861,103320,44835,127070,107638,116708,57582,66522,4565,111474,26007,71302,8211,87043,65954,90143,42754,140287,89702,13840,116036,8902,48635,78439,73540,90107,90214,116951,120074,107764,162602,7111,26456,59527,106199,134631,15834,129198,180472,21234,128311,734,96079,155945,164955,150798,10006,190537,49393,51819,119507,127967,149811,90041,61029,170604,15202,87611,101496,54550,182713,138704,78946,143730,173605,107891,95862,75649,8487,10346,161422,49242,108223,94257,55183,8280,92784,114583,106768,85052,169495,32852,59956,109479,46111,9953,65961,65964,174545,116490,180013,34596,40401,9493,40824,87882,35015,21639,60358,179259,71276,154888,74279,87786,186930,70368,45388,5490,121061,36993,189259,156219,32476,15756,4369,62785,184650,54781,49066,162870,172001,162273,170051,17221,62634,12601,21505,120314,128998,51394,5513,163313,92361,189592,109267,157753,34851,187259,102593,77356,163271,82591,189654,66415,185678,167561,127989,58632,48190,174170,85993,45405,183743,165486,164917,54845,114701,115274,32039,150884,77670,126816,129464,131944,137807,67466,104673,21560,143682,77820,43473,107475,81095,108972,51686,92789,84704,45891,159250,31517,70227,168270,78895,66518,59110,110278,96400,156143,44010,111449,67977,9449,79940,125680,79551,168822,85044,30013,30438,172554,101316,130113,136975,81196,103387,71867,62229,94315,29335,7729,50515,5190,161112,37521,155978,128089,54124,55553,130020,74632,91892,181247,188894,67361,127941,21275,110216,89892,116477,27536,115260,82220,27422,133321,9226,54306,72514,81909,36847,70031,45324,88246,91829,26536,165760,123251,167471,26429,128819,21792,73542,90229,29398,134092,146290,33689,187910,44154,90773,68146,43913,33592,78991,64263,103764,129971,142795,65778,137892,60082,146339,188660,161197,12170,128408,133715,109651,112501,66299,8685,173301,109885,70057,127358,50632,44930,146958,4739,21920,133588,182402,28666,45893,75148,113355,13858,96239,77458,81190,43505,123526,91113,140440,57146,160964,147328,76990,5261,65491,148132,189638,96403,161904,54143,161772,171669,9808,185777,93362,139171,181921,13601,40097,23887,97076,41183,160948,159569,56889,105308,29768,127278,74530,138084,96701,132813,95373,186547,1673,69196,88357,151254,174158,130239,50110,94254,168840,88115,177431,80430,56170,106839,189857,107336,127243,163534,127995,119367,99538,96583,53497,72793,151855,101058,172632,132847,82736,101901,65534,188772,98705,34930,88679,138174,111627,57688,39992,29068,155954,3622,159301,3476,2058,149185,122622,134913,51326,174854,79178,137631,48453,131903,55330,139502,119648,88156,2435,137427,120841,151684,112392,26256,76224,117899,37818,161850,175345,55294,134231,27619,35093,105720,83695,134173,117902,178382,85968,178788,108142,161973,24968,91876,91273,179529,138506,173192,94071,81111,4679,8449,60683,128616,141815,162634,53225,20506,120107,960,68212,57562,141104,107771,13662,142130,29321,28345,81699,158411,27758,132138,43012,91939,173834,112483,113917,24130,23036,48332,189634,175586,175478,138685,187755,59498,130298,56143,65097,172971,186537,152622,160904,137570,70012,138198,30084,178933,89530,26928,132613,70011,129599,25663,115966,158358,63888,141758,118969,15225,7391,176979,108483,163201,128929,81349,863,60437,75350,174846,138583,29654,117531,178838,110361,64765,126217,102711,143702,137964,132325,159326,68844,72506,171301,111787,110474,100821,142644,112495,143352,66510,163437,179144,35232,115688,130991,85814,43319,169988,161002,22284,94702,28426,144046,27529,74955,160474,165196,89663,116525,98417,135462,51339,79151,168276,71968,176557,140107,111877,149269,163605,128298,61144,156879,8580,67622,110699,74246,118831,32983,67553,83562,119632,143759,128633,26826,85351,173985,134600,130034,128660,179156,148519,71513,143213,108964,113472,94350,33581,123699,61445,164874,94577,173888,91869,110213,70026,27790,47011,95913,45222,36601,50090,48988,72103,69266,189141,179050,34535,174368,142909,109950,168927,170651,119883,126860,172214,135301,120034,1665,85001,27887,168183,170452,99650,185203,148815,152566,139986,188509,102680,25316,183815,118419,115451,106911,53440,83064,103475,64850,178117,120551,30114,160622,21749,115427,139549,141188,136418,70750,3858,54830,76840,46975,129372,96235,138395,3707,157625,159133,62421,85859,136125,46057,131335,93558,170073,114386,5248,46752,49709,22545,25384,20563,94989,69233,82277,61142,75160,179597,80921,68601,76389,16722,13225,161790,85994,169603,72115,82373,28000,189562,42626,188549,119581,6064,29236,102597,60490,16757,114180,137543,96607,190253,22744,60790,8186,106780,108759,158512,163385,131684,152311,107631,75757,27566,86266,73122,69770,166163,73109,113696,158716,181905,185445,166435,57087,86574,109315,23973,36787,151448,97110,43717,12443,48893,117193,40219,183872,185006,188329,50656,103085,60476,78,113650,15177,61781,184471,185497,114557,79244,10949,30948,326,31404,158592,155526,23012,63570,118852,186841,69764,74673,51602,21245,101418,168278,171168,70522,102391,74754,165453,67000,176489,144030,12459,188021,94958,160496,35190,65228,63564,180535,104356,91441,164379,79245,143936,102804,136042,38489,129396,102628,171572,149906,60257,82813,124818,115554,189242,36774,165464,28756,34431,11707,81059,149721,30989,128549,92478,157394,183295,117370,176789,35196,60766,44860,75016,136623,114735,145369,140040,64488,130007,102598,41077,163737,65653,60212,38997,159120,184831,38578,2303,185829,188709,148929,34795,106169,162151,127049,126120,84372,105577,101728,136066,119709,176013,92856,42118,115714,5929,73988,81479,147323,86171,120610,54501,126454,141023,112628,11789,150272,79044,48404,178202,142109,52320,50827,87500,30465,75470,104683,103277,113797,157446,141659,148833,88079,139945,140398,51688,120014,52798,7745,28959,115998,56643,148850,22946,6780,16763,151441,112378,108259,184632,172184,99541,75526,54208,115218,135279,25487,28429,155053,114907,180497,47426,40835,54233,56265,152877,177820,63322,61346,29343,168749,180071,61318,110877,46394,57660,148038,164798,21873,41035,102908,118502,91041,125365,41161,172634,130924,182316,152123,169922,188293,150281,81939,126303,191068,112852,38656,46042,118512,168192,102813,44271,62148,186946,154663,60877,101979,155013,113903,167822,153156,46760,68548,181854,2598,2358,163953,63525,104389,146018,135325,169176,16273,47240,163001,71962,16485,80570,86741,136256,190934,140477,100690,66205,133436,174747,46529,33955,170821,47576,103657,78079,56156,162488,5716,156704,67118,92459,140669,161759,115676,190697,140716,36715,16289,190260,13836,92890,69429,114149,171285,145910,110327,105446,135084,114375,74646,22569,151062,142357,187317,67202,116935,76621,113017,106948,76426,190329,155901,79247,169846,35194,168060,183859,178389,146978,137865,60176,158884,180732,148524,104750,2022,84593,177292,117216,43823,13486,170936,62578,101174,36895,33931,148696,46471,165298,11603,183197,143503,68490,83983,142245,67178,75463,114106,22936,47472,47739,48652,59274,161711,56397,92542,124808,142768,112681,122128,44912,68870,158323,32987,152077,154790,154671,5702,190873,168937,65207,78740,178874,115322,63745,97274,58580,81168,71604,157392,55029,4019,25745,138961,106976,35057,83094,155361,91702,39329,51928,57706,105462,121515,111857,114096,49832,99094,109708,62907,68568,75433,156518,47276,115595,68703,43864,102623,53698,166480,115648,154270,166299,109251,98985,55747,140872,178450,114883,36964,109592,74794,79490,2106,7958,174073,179750,157183,48702,81783,52542,52415,46086,85307,144811,163416,142825,55707,117151,158104,96241,184347,140381,33132,17388,96319,115616,27466,153512,66825,171926,11305,176793,169404,79369,123874,89282,114623,131677,74337,34654,53175,130845,116243,30178,54035,62739,70686,6201,24836,82634,56457,37488,134244,134131,118192,60208,52878,69225,102220,176093,133274,179478,164983,132555,93608,68543,22013,38318,84371,129217,101248,12603,143658,157272,3268,36542,61435,79309,54403,32306,121077,64207,103507,120033,152772,74514,96803,13393,71878,82809,61349,120598,169981,145986,142030,124940,11564,161708,41663,123377,33612,152819,22467,142671,62260,64301,126131,65712,180397,151882,23893,162386,14986,146571,51314,140635,101658,80226,74214,143253,97999,162016,27453,83425,133977,107006,46931,111319,91388,5491,110841,25124,156902,116287,145333,125038,22163,125761,26053,147364,73799,101158,132156,116893,130583,49501,70485,156194,131045,181525,177711,147515,136157,97320,101984,144504,180350,172509,50416,162529,198,10259,178711,148946,169664,76036,15131,102779,91361,144344,29110,102076,150092,48210,110326,175059,76477,69936,144865,54310,90895,51337,138107,141937,142459,30146,38676,116401,44350,68330,30637,79439,42790,11157,39129,135237,25045,133510,15429,112718,37964,11655,124714,89599,171000,93208,143550,11608,82578,86708,88492,116917,94821,98480,73158,79729,158390,30256,169000,73656,166358,102599,146924,108574,78093,53449,108830,173609,43268,87240,93503,175773,37997,69143,1229,190553,26241,164090,101747,27115,68358,149803,149796,131519,171826,42488,121640,160773,132831,2707,91421,16468,63061,114897,154443,87311,33109,35581,133825,2613,130674,87931,88882,115004,72546,109668,103322,77196,175854,169111,34810,22984,36237,106363,101181,108314,25915,179362,168520,166736,119285,89265,30840,137355,167900,106574,28036,35429,145633,154279,129698,103732,119893,46777,124165,39292,73090,169422,97178,60216,35938,161261,27416,114379,91718,25898,101928,73985,55400,76435,180550,64034,147190,2997,36263,28600,79961,134121,107142,116615,161862,186787,90771,178558,8880,183310,81236,46649,156954,127538,30603,160710,127992,61489,56427,29856,184212,179518,39449,131816,51505,111097,67988,69497,140290,64786,190236,188538,92671,89398,97897,148687,128770,67313,40503,1401,21005,83026,114473,95841,137963,55650,88203,81256,74004,59680,63921,121249,114638,23403,123286,178233,80377,150317,40779,148146,96852,163284,164318,73445,49929,12070,635,77240,40644,36347,116731,39317,124790,135754,167598,43818,123986,96118,1641,27412,163716,62832,83332,50741,72198,85811,141623,131946,37463,178675,172379,135338,98872,52645,36963,84060,118037,68621,75387,158600,9883,122704,64965,121781,150032,150845,1891,181191,157329,148646,56991,29484,50231,127482,65915,64385,128424,137844,107175,26428,36297,115452,159985,163977,168399,158946,184499,83416,65109,131700,31416,144456,20987,115093,147275,133604,159097,116407,114363,176861,22518,49086,129947,152569,22494,5249,63079,72331,12820,59018,110475,55073,48776,30673,142515,56036,114898,148880,141578,33867,66385,3745,184798,82041,50817,174656,97445,190886,78856,55436,124098,120719,162860,80649,65138,64128,77716,166103,33166,59922,89732,104284,155769,108007,59180,114522,135268,161634,134271,98897,184636,53113,88087,83180,34453,180081,108272,33864,27322,7149,50974,25818,100164,25077,146506,184480,109809,93893,175810,22625,60041,5499,190,166912,64539,64646,117329,62962,92688,132206,45375,134809,136080,161649,8934,35189,111892,58407,167470,36910,180080,89426,121468,58105,165561,137396,6533,69802,160606,72633,174765,102806,155992,170575,138798,87423,50030,57440,119298,70372,171510,177388,32600,31142,87935,35402,128061,181785,58140,187288,96529,99551,153194,65632,3484,97780,123980,151175,32190,68347,182603,93456,145728,61133,154456,131600,75959,38394,114892,167075,25501,22606,9489,66801,164182,24979,89868,57970,85383,136126,74121,68510,184078,28432,141414,70167,185417,83978,111378,155206,30417,45674,35807,26101,61022,280,84875,66317,184333,89499,24436,9725,186549,68668,88606,79740,64291,179265,16708,25903,103563,6001,136353,119211,63468,98810,97245,106995,123193,31063,103583,155196,153771,25747,98084,32392,118547,176620,123998,24024,140303,124081,7818,162533,115489,156315,7678,158294,186864,46892,152246,105717,123793,148302,41828,171675,90999,97031,23565,45168,73131,73224,120747,148810,149232,32442,155195,65702,101943,185290,9121,149367,188435,75619,101610,35177,187802,54338,45112,173785,91770,17312,28471,33509,28858,156079,184158,172112,43811,48701,56652,181137,78734,149877,147050,143770,77604,178967,87698,123096,30690,66321,76764,115077,22957,97026,6651,61528,43231,156361,120322,30979,54,42162,148247,130693,99943,38886,150924,47935,52001,71092,58051,1619,69286,82590,66089,112170,100558,67453,170361,39420,107180,105885,92121,189606,104304,39403,30887,152636,67835,97404,46055,183916,180960,87415,32768,122933,58751,178965,99810,122,162397,114873,25516,166450,154329,142292,92580,26505,107156,125867,163874,157654,116944,76028,148490,101286,31474,130980,179671,47064,71512,411,93743,135879,162220,173744,81695,190119,98042,20754,160832,152821,148390,178577,29140,55808,22900,180630,21115,54716,184933,107605,10197,96399,180919,127048,145301,54298,29086,143828,105357,37716,101067,168470,9403,60641,160419,133936,114296,103870,31222,167330,94439,58347,151877,31917,106362,46647,37377,84920,27555,166243,114566,78586,83382,168032,103353,49828,64821,133280,2039,12484,45082,88597,70518,78811,48343,141298,67196,32002,186989,54418,151458,143606,105299,104752,51628,79509,62981,70202,173334,1933,143713,144872,107213,129848,52282,50367,168658,106876,108748,53494,145015,47760,29438,61316,30554,166136,140142,83159,85861,66750,101529,73477,169795,30206,110084,153534,68058,108874,125991,43965,16479,98314,69892,75372,72948,96649,103270,174428,72901,98687,114374,164691,170819,134764,29102,118484,158861,133857,145362,26049,134966,7363,98501,96120,149316,181420,98643,138069,65628,112674,133597,79142,47682,103279,184747,138102,50365,146187,92658,148700,108701,24260,86445,121762,768,118174,148827,121818,149024,72586,93049,47118,66170,115528,127788,65569,115773,142799,67997,85610,31287,154518,28947,22413,107245,162353,179606,187229,144364,120646,37316,98948,94475,136402,55710,30927,189376,104300,142722,149063,63874,162635,50689,61525,148212,140565,69302,78228,31001,92339,98214,118600,88048,109837,93114,186831,79248,117811,80895,140161,139542,47400,26699,13693,187730,2113,94788,108412,46131,4665,97524,145011,61630,124030,117846,10803,123833,71165,28139,126447,179052,89137,120590,175553,144396,89165,81259,149840,69535,168309,147503,122874,156040,167333,136178,36940,182010,70783,77270,97569,127719,90754,145891,97312,158304,25753,115820,127470,54704,35489,143280,190975,40518,122172,36270,179561,154683,64494,479,2939,125272,153296,65833,159613,141061,98233,132408,35654,105101,149934,100616,89897,125448,114611,112049,109308,151439,144885,164644,147947,97746,66843,36950,28895,81873,140323,74485,170138,81596,71341,91017,68659,32776,7196,154371,187897,160567,152607,177333,188946,54155,138443,175223,47607,67937,149157,164336,137015,43773,60058,190494,22093,159153,73430,95440,176361,148037,105774,146461,189650,39787,24049,144566,21545,102722,30743,5619,2654,93599,59375,36406,185439,56977,42997,138283,99689,85542,79388,71343,52629,185500,64707,119817,92414,79891,94745";
			//String ids_errados = "78, 3858, 4665, 5261, 5769, 7363, 8934, 9653, 9864, 10197, 10346, 11252, 17221, 22013, 22900, 22984, 23534, 30438, 32993, 33132, 34851, 35015, 36993, 38656, 41183, 42488, 45324, 45388, 45893, 48190, 50090, 54233, 55183, 57440, 59167, 59527, 67118, 64128, 65138, 65861, 65954, 65964, 67553, 68146, 70011, 70057, 73542, 73799, 74337, 74530, 75649, 76036, 76621, 77158, 77356, 78811, 81190, 82041, 82373, 83180, 88952, 89702, 92890, 97445, 103270, 106780, 107142, 107771, 107889, 110474, 112628, 113917, 115427, 115595, 116407, 118037, 118192, 122172, 133274, 133510, 133715, 134244, 134600, 135279, 135462, 136157, 141815, 144811, 144872, 146187, 146339, 158104, 160606, 161904, 162016, 163534, 166450, 166912, 167900, 170361, 170575, 173985, 174158, 179529, 181247, 181854, 181921, 183197, 183859, 183916, 184480, 185203, 186841, 187259, 188293, 188538, 188660, 189638, 190253, 190329, 190553, 190873, 190934";
			//String ids_errados_braga = "5190, 5249, 5490, 5929, 6780, 7391, 8449, 8580, 13836, 17312, 21234, 21639, 21920, 25663, 26007, 26536, 27790, 29484, 29654, 30084, 30206, 34453, 34930, 36847, 37463, 40219, 41828, 42626, 43473, 43913, 44010, 44930, 45891, 46760, 47064, 47240, 49501, 51628, 52415, 55553, 56156, 56265, 56427, 56889, 60082, 61029, 65915, 66518, 67000, 69764, 70167, 70227, 73656, 74214, 75160, 77805, 78946, 79551, 80921, 81095, 82591, 82813, 83695, 85044, 87043, 89330, 89892, 90229, 91113, 91892, 92478, 92580, 93456, 94254, 94315, 94821, 95440, 96239, 96319, 96400, 96607, 97031, 99650, 102598, 103320, 104284, 104389, 106199, 106839, 107006, 107245, 108259, 111787, 116243, 116401, 116477, 117902, 118852, 123096, 123251, 123377, 128089, 128549, 129599, 130020, 133604, 133825, 134131, 134231, 134913, 137631, 143730, 147050, 147328, 148929, 149024, 149840, 149906, 150798, 153806, 155526, 155992, 156143, 157329, 160964, 161112, 161634, 163716, 163874, 164917, 167822, 170138, 172112, 173301, 174166, 174846, 176093, 177711, 180081, 182603, 183872, 184158, 184333, 184471, 184499, 185678, 187229, 189141, 189650, 190697, 190886, 190975";
			String ids_errados = "78, 3858, 4665, 5261, 5769, 7363, 8934, 9653, 9864, 10197, 10346, 11252, 17221, 22013, 22900, 22984, 23534, 30438, 32993, 33132, 34851, 35015, 36993, 38656, 41183, 42488, 45324, 45388, 45893, 48190, 50090, 54233, 55183, 57440, 59167, 59527, 67118, 64128, 65138, 65861, 65954, 65964, 67553, 68146, 70011, 70057, 73542, 73799, 74337, 74530, 75649, 76036, 76621, 77158, 77356, 78811, 81190, 82041, 82373, 83180, 88952, 89702, 92890, 97445, 103270, 106780, 107142, 107771, 107889, 110474, 112628, 113917, 115427, 115595, 116407, 118037, 118192, 122172, 133274, 133510, 133715, 134244, 134600, 135279, 135462, 136157, 141815, 144811, 144872, 146187, 146339, 158104, 160606, 161904, 162016, 163534, 166450, 166912, 167900, 170361, 170575, 173985, 174158, 179529, 181247, 181854, 181921, 183197, 183859, 183916, 184480, 185203, 186841, 187259, 188293, 188538, 188660, 189638, 190253, 190329, 190553, 190873, 190934, 5190, 5249, 5490, 5929, 6780, 7391, 8449, 8580, 13836, 17312, 21234, 21639, 21920, 25663, 26007, 26536, 27790, 29484, 29654, 30084, 30206, 34453, 34930, 36847, 37463, 40219, 41828, 42626, 43473, 43913, 44010, 44930, 45891, 46760, 47064, 47240, 49501, 51628, 52415, 55553, 56156, 56265, 56427, 56889, 60082, 61029, 65915, 66518, 67000, 69764, 70167, 70227, 73656, 74214, 75160, 77805, 78946, 79551, 80921, 81095, 82591, 82813, 83695, 85044, 87043, 89330, 89892, 90229, 91113, 91892, 92478, 92580, 93456, 94254, 94315, 94821, 95440, 96239, 96319, 96400, 96607, 97031, 99650, 102598, 103320, 104284, 104389, 106199, 106839, 107006, 107245, 108259, 111787, 116243, 116401, 116477, 117902, 118852, 123096, 123251, 123377, 128089, 128549, 129599, 130020, 133604, 133825, 134131, 134231, 134913, 137631, 143730, 147050, 147328, 148929, 149024, 149840, 149906, 150798, 153806, 155526, 155992, 156143, 157329, 160964, 161112, 161634, 163716, 163874, 164917, 167822, 170138, 172112, 173301, 174166, 174846, 176093, 177711, 180081, 182603, 183872, 184158, 184333, 184471, 184499, 185678, 187229, 189141, 189650, 190697, 190886, 190975";
			query = "select * from comentarios where id in (";
			query = query + ids_errados;
			query = query + ");";
			rs = (ResultSet) stmt.executeQuery(query);
			
			
			int s1, s2, s3, s4, s5;
			s1=s2=s3=s4=s5=0;
			while (rs.next()) {
				if (rs.getString("rating").equals("1")) {
					s1++;
				}else if (rs.getString("rating").equals("2")) {
					s2++;
				}else if (rs.getString("rating").equals("3")) {
					s3++;
				}else if (rs.getString("rating").equals("4")) {
					s4++;
				}else if (rs.getString("rating").equals("5")) {
					s5++;
				}
			}
			int total = 2400;
			System.out.println("1 estrela: "+s1 + " - "+ ((float)s1/233 * 100)+"%");
			System.out.println("2 estrela: "+s2 + " - "+ ((float)s2/62 * 100)+"%");
			System.out.println("3 estrela: "+s3 + " - "+ ((float)s3/142 * 100)+"%");
			System.out.println("4 estrela: "+s4 + " - "+ ((float)s4/308 * 100)+"%");
			System.out.println("5 estrela: "+s5 + " - "+ ((float)s5/1655 * 100)+"%");
			
			
			/*BufferedWriter bufferedWriter = null;
	        
	        try {
	            
	            //Construct the BufferedWriter object
	            bufferedWriter = new BufferedWriter(new FileWriter("samples_lucas.txt"));
	            
	            
	            String str;
				
	            int i = 1;
				while (rs.next()) {
					if (i%2==0) {
						str = "FERNANDO\n";
					}
					else {
						str = "LUCAS\n";
						str =str +  "Id: " + rs.getString("id") + "\nRating: "+rs.getString("rating") + "\nComent�rio: " +rs.getString("text") + "\n\n";
						bufferedWriter.write(str);
						System.out.print(str);
					}
					i++;
				}
	            
	        } catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            //Close the BufferedWriter
	            try {
	                if (bufferedWriter != null) {
	                    bufferedWriter.flush();
	                    bufferedWriter.close();
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }*/
			
			con.close();
			
			
		} 
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	private void storeInMySQL() {
		String dbUrl = "jdbc:mysql://localhost/comments";
		String dbClass = "com.mysql.jdbc.Driver";
		

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager.getConnection(dbUrl, "root", "");
		Statement stmt = (Statement) con.createStatement();
		String query;
		ArrayList<Comentario> list = getCommentList();
		String text;
		int i = 0;
		for (Comentario c : list) {
			//text = StringEscapeUtils.escapeSql(c.getText());
			text = replaceQuote(c.getText());
			text = text.replace("\\", "\\\\");
			query = "INSERT comentarios VALUES ('"+c.getId()+"', '"+c.getAppId()+"', '"+c.getAppName()+"', '"+c.getRating()+"', '"+formatter.format(c.getDate())+"', '"+text+"')";
			stmt.executeUpdate(query);
			System.out.println(i++);
		}
		con.close();
		} 

		catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private ArrayList<Comentario> getCommentList() {
		
		ArrayList<Comentario> commentsList = new ArrayList<Comentario>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(comments);
			
			NodeList apps = dom.getElementsByTagName("app");
			Calendar calendar = Calendar.getInstance();
			
			String appName;
			for (int j=0; j<apps.getLength(); j++) {
				appName = apps.item(j).getAttributes().getNamedItem("appId").getNodeValue();
				NodeList comm = ((Element) apps.item(j)).getElementsByTagName("comment");
				for (int i=0; i<comm.getLength(); i++) {
					Comentario c = new Comentario();
					c.setId(i+1);
					
					c.setAppId(j+1);
					
					c.setAppName(appName);
					
					c.setRating(Integer.valueOf(getTextValue(comm.item(i), "commentRating")));
					
					calendar.setTimeInMillis(Long.valueOf(getTextValue(comm.item(i), "commentCreationTime")));
					c.setDate(calendar.getTime());
					
					c.setText(getTextValue(comm.item(i), "commentText"));
					
					commentsList.add(c);
				}
			}
			
			


		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return commentsList;
	}

	
	

	public class DateComparator implements Comparator<Comentario> {
	    public int compare(Comentario object1, Comentario object2) {
	        return object1.getDate().compareTo(object2.getDate());
	    }
	}
	private void printStatistics2() {
		
		int pagos = 0;
		int gratuitos = 0;
		int pagos1star = 0;
		int pagos2star = 0;
		int pagos3star = 0;
		int pagos4star = 0;
		int pagos5star = 0;
		int gratuitos1star = 0;
		int gratuitos2star = 0;
		int gratuitos3star = 0;
		int gratuitos4star = 0;
		int gratuitos5star = 0;
		int a32010 = 0;
		int a62010 = 0;
		int a92010 = 0;
		int a12011 = 0;
		int a32011 = 0;
		int a62011 = 0;
		int a92011 = 0;
		int a12012 = 0;
		int a32012 = 0;
		int a62012 = 0;
		int a92012 = 0;
		int a12013 = 0;
		int ahoje = 0;
		
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		Calendar c3 = Calendar.getInstance();
		Calendar c4 = Calendar.getInstance();
		Calendar c5 = Calendar.getInstance();
		Calendar c6 = Calendar.getInstance();
		Calendar c7 = Calendar.getInstance();
		Calendar c8 = Calendar.getInstance();
		Calendar c9 = Calendar.getInstance();
		Calendar c10 = Calendar.getInstance();
		Calendar c11 = Calendar.getInstance();
		Calendar c12 = Calendar.getInstance();
		
		c1.set(2010, 3, 1);
		c2.set(2010, 6, 1);
		c3.set(2010, 9, 1);
		c4.set(2011, 1, 1);
		c5.set(2011, 3, 1);
		c6.set(2011, 6, 1);
		c7.set(2011, 9, 1);
		c8.set(2012, 1, 1);
		c9.set(2012, 3, 1);
		c10.set(2012, 6, 1);
		c11.set(2012, 9, 1);
		c12.set(2013, 1, 1);
		ArrayList<Comentario> commentsList = getCommentList();
		//Collections.sort(commentsList, new DateComparator());
		Calendar calendar = Calendar.getInstance();
		int maiorComentario = 0;
		
		for (Comentario c : commentsList) {
			if (c.getAppId() <= 350) {
				pagos++;
				switch (c.getRating()) {
				case 1: pagos1star++;	
					break;
				case 2: pagos2star++;	
					break;
				case 3: pagos3star++;	
					break;
				case 4: pagos4star++;	
					break;
				case 5: pagos5star++;	
					break;
				}
			}
			if (c.getAppId() > 350) {
				gratuitos++;
				switch (c.getRating()) {
				case 1: gratuitos1star++;	
					break;
				case 2: gratuitos2star++;	
					break;
				case 3: gratuitos3star++;	
					break;
				case 4: gratuitos4star++;	
					break;
				case 5: gratuitos5star++;	
					break;
				}
			}
			
			if (c.getDate().before(c1.getTime())) {
				a32010++;
			} else if (c.getDate().after(c1.getTime()) && c.getDate().before(c2.getTime())) {
				a62010++;
			} else if (c.getDate().after(c2.getTime()) && c.getDate().before(c3.getTime())) {
				a92010++;
			} else if (c.getDate().after(c3.getTime()) && c.getDate().before(c4.getTime())) {
				a12011++;
			} else if (c.getDate().after(c4.getTime()) && c.getDate().before(c5.getTime())) {
				a32011++;
			} else if (c.getDate().after(c5.getTime()) && c.getDate().before(c6.getTime())) {
				a62011++;
			} else if (c.getDate().after(c6.getTime()) && c.getDate().before(c7.getTime())) {
				a92011++;
			} else if (c.getDate().after(c7.getTime()) && c.getDate().before(c8.getTime())) {
				a12012++;
			} else if (c.getDate().after(c8.getTime()) && c.getDate().before(c9.getTime())) {
				a32012++;
			} else if (c.getDate().after(c9.getTime()) && c.getDate().before(c10.getTime())) {
				a62012++;
			} else if (c.getDate().after(c10.getTime()) && c.getDate().before(c11.getTime())) {
				a92012++;
			} else if (c.getDate().after(c11.getTime()) && c.getDate().before(c12.getTime())) {
				a12013++;
			} else if (c.getDate().after(c12.getTime())) {
				ahoje++;
			}
			
			if (c.getText().length() > maiorComentario) {
				maiorComentario = c.getText().length();
			}
			
		}
		
		System.out.println(pagos);
		System.out.println(gratuitos);
		System.out.println();
		System.out.println(pagos1star);
		System.out.println(pagos2star);
		System.out.println(pagos3star);
		System.out.println(pagos4star);
		System.out.println(pagos5star);
		System.out.println(gratuitos1star);
		System.out.println(gratuitos2star);
		System.out.println(gratuitos3star);
		System.out.println(gratuitos4star);
		System.out.println(gratuitos5star);
		System.out.println();
		
		System.out.println(a32010+" "+a62010+" "+a92010+" "+a12011+" "+a32011+" "+a62011+" "+a92011+" "+a12012+" "+a32012+" "+a62012+" "+a92012+" "+a12013+" "+ahoje);
		System.out.println(maiorComentario);
	}

	
	private String getTextValue(Node elem, String tagName) {
		Element ele = (Element)elem;
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	public static String removeAccents(String str) {
	    str = Normalizer.normalize(str, Normalizer.Form.NFD);
	    str = str.replaceAll("[^\\p{ASCII}]", "");
	    return str;
	}
	
	public static String replaceQuote(String st)  
	{  
		/********************************************************         
		 * This method takes a String varable as a parameter and* 
		 * replaces the single quote (which causes aSQLException)* 
		 * with two single quotes(actually, it just adds a second* 
		 * single quote when it finds one)                       * 
		 *       * 
		 * example:   input-->  "O'Reilly"      output-->  "O''Reilly"* 
		 * input-->  "O'Conor's"              output-->  "O''Conor''s"* 
		 *            *  ***************************************************************/  
		StringBuffer sb = new StringBuffer();  
		char cArray[] = st.toCharArray();  
		for(int i = 0; i < st.length(); i++)  
		{  
			if(cArray[i] == '\'') // find single quote in String  
			{  
				sb.append('\''); //append the escape character  
			}  
			sb.append(cArray[i]); //append the regular character  
		}  
		return new String(sb);  
	}  
}
