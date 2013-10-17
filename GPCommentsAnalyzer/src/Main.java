import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import weka.classifiers.misc.InputMappedClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;


public class Main {


	
	private final static String stopwords = "C:\\Users\\Fernando\\workspace\\GPCommentsAnalyzer\\pt-br_stopwords";
	
	public static void main(String[] args) {
		Main obj = new Main();
		Instances data = null;
		Instances tdm = null;
		
		
		obj.evaluateSavedModel();
		
		
		//obj.XMLtoCSV(new File("data/comments.xml"), "data/comments.csv");
		
		//obj.CSVtoARFF(new File("data/comments.csv"), "data/comments.arff");
		
		//data = obj.readArff("data/comments.arff");
		
		//tdm = obj.getTermDocumentMatrix(data);
		
		//obj.toArffFile(tdm, "data/comments_tdm.arff");
		
		//tdm = obj.readArff("data/comments_tdm.arff");
		
		
		//tdmAttrSelection = obj.getTDMAttrSelection(data);
		
/*		
		
		try {
			
			//Randomize the order of the instances
			
//			Randomize randomize = new Randomize();
//			randomize.setOptions(Utils.splitOptions("-S 123"));
//			randomize.setInputFormat(tdm);
//			tdm = Filter.useFilter(tdm, randomize);
//			
//			tdm.setClassIndex(tdm.numAttributes() - 1);
//			
//			
//
//			//split data for test, validation and train
//			
//			Instances train, train2, train3, validate, test;
//			
//			StratifiedRemoveFolds srf = new StratifiedRemoveFolds();
//			srf.setOptions(Utils.splitOptions("-S 1234 -N 5 -F 1")); //Recupera a fold 1, dividindo em 5 folds (20% pra cada)
//			srf.setInputFormat(tdm);
//			train = Filter.useFilter(tdm, srf);
//			
//			srf.setOptions(Utils.splitOptions("-S 1234 -N 5 -F 2")); //Recupera a fold 2, dividindo em 5 folds (20% pra cada)
//			srf.setInputFormat(tdm);
//			train2 = Filter.useFilter(tdm, srf);
//			
//			
//			srf.setOptions(Utils.splitOptions("-S 1234 -N 5 -F 3")); //Recupera a fold 3, dividindo em 5 folds (20% pra cada)
//			srf.setInputFormat(tdm);
//			train3 = Filter.useFilter(tdm, srf);
//			
//			
//			srf.setOptions(Utils.splitOptions("-S 1234 -N 5 -F 4")); //Recupera a fold 4 (validation), dividindo em 5 folds (20% pra cada)
//			srf.setInputFormat(tdm);
//			validate = Filter.useFilter(tdm, srf);
//			
//			srf.setOptions(Utils.splitOptions("-S 1234 -N 5 -F 5")); //Recupera a fold 5 (test), dividindo em 5 folds (20% pra cada)
//			srf.setInputFormat(tdm);
//			test = Filter.useFilter(tdm, srf);
//			
//			train.addAll(train2);
//			train.addAll(train3);
//			obj.toArffFile(train, "data/train.arff");
//			obj.toArffFile(validate, "data/validate.arff");
//			obj.toArffFile(test, "data/test.arff");
			                                                   
			
			Instances train,validate, test;
			train = obj.readArff("data/train.arff");
			train.setClassIndex(train.numAttributes() - 1);
			validate = obj.readArff("data/validate.arff");
			validate.setClassIndex(validate.numAttributes()-1);
			
			
			
			                                                                                        //	-S  set type of SVM (default 0)	                                           
                                                                                                    //			 0 = C-SVC                                                         
			//create classifier model with train data                                               //			 1 = nu-SVC                                                        
			LibSVM svm = new LibSVM();                                                              //			 2 = one-class SVM                                                 
			String opt = "-S 0 -K 0 -D 3 -G 0.05 -R 5 -C 5.0 -N 0.5 -P 0.1 -M 100.0 -E 0.0001 -H 1 -W 1 -seed 1";                                  //			 3 = epsilon-SVR                                                   
			svm.setOptions(Utils.splitOptions(opt));                                                //			 4 = nu-SVR                                                        
			svm.buildClassifier(train);                                                             //	-K  set type of kernel function (default 2)	                               
                                                                                                    //			 0 = linear: u'*v                                                  
			//evaluate classifier with validation and adjust parameters                             //			 1 = polynomial: (gamma*u'*v + coef0)^degree                       
			Evaluation eval = new Evaluation(train);                                                //			 2 = radial basis function: exp(-gamma*|u-v|^2)                    
			eval.evaluateModel(svm, validate);                                                      //			 3 = sigmoid: tanh(gamma*u'*v + coef0)                             
			System.out.println(eval.toSummaryString());                                             //	-D  set degree in kernel function (default 3)	                           
			System.out.println(eval.toClassDetailsString());                                        //	-G  set gamma in kernel function (default 1/k)	                           
			System.out.println(eval.toMatrixString());                                              //	-R  set coef0 in kernel function (default 0)	                           
			                                                                                        //	-C  set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)	   
			                                                                                        //	-N  set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
                                                                                                    //	-Z  whether to normalize input data, 0 or 1 (default 0)                    
			//apply classifier to test data (never seen before)                                     //	-P  set the epsilon in loss function of epsilon-SVR (default 0.1)          
			//evaluate classification                                                               //	-M  set cache memory size in MB (default 40)                               
                                                                                                    //	-E  set tolerance of termination criterion (default 0.001)                 
                                                                                                    //	-H  whether to use the shrinking heuristics, 0 or 1 (default 1)            
			//CONTINUAR DAQUI: USAR O INPUTMAPPEDCLASSIFIER, passando o modelo salvo                //	-W  set the parameters C of class i to weight[i]*C, for C-SVC (default 1)  
                                                                                                                                                                                    


		
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}



	private void evaluateSavedModel() {
		
		try {
//			Logistic logistic = (Logistic) weka.core.SerializationHelper.read(new FileInputStream(new File("data/logistic.model")));
			Instances data = readArff("data/comments_test_tdm.arff");
			data.setClassIndex(data.numAttributes() - 1);
//			
//			classifier.setModelPath("data/logistic.model");
			
			
			//PrintStream out = new PrintStream(new FileOutputStream("data/output.txt"));
			//System.setOut(out);
			
			
			InputMappedClassifier classifier = new InputMappedClassifier();
			classifier.setModelPath("data/logistic.model");
			Evaluation e = new Evaluation(classifier.getModelHeader(data));
			e.evaluateModel(classifier, data);      
			System.out.println(e.toSummaryString());
			
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}



	private Instances getTermDocumentMatrix(Instances data) {
		try {
			String opt = "-R 1 -W 200 -prune-rate -1.0 -C -T -I -N 1 -L -S -stemmerweka.core.stemmers.NullStemmer" +
			" -M 5 -stopwords "+ stopwords +
			" -tokenizer weka.core.tokenizers.NGramTokenizer -delimiters \" \\r\\n\\t.,;:\\\'\\\"()?!\" -max 2 -min 1'";
			
			String[] options = Utils.splitOptions(opt);

			StringToWordVector strToWordVector = new StringToWordVector();
			strToWordVector.setOptions(options);
			strToWordVector.setInputFormat(data);
			
			Instances newData = Filter.useFilter(data, strToWordVector);
			
			newData.setClassIndex(0);
			
			
			//Remove os atributos que combinam com as seguintes expressoes regulares
			String regexp1 = "."; //qualquer atributo de tamanho igual a 1
			String regexp2 = "\\d+"; //numeros 
			List<Attribute> attrs = new ArrayList<Attribute>();
			for (int i = 0; i < newData.numAttributes(); i++) {
		        if (Pattern.matches(regexp1, newData.attribute(i).name()) || Pattern.matches(regexp2, newData.attribute(i).name()))
		        	attrs.add(newData.attribute(i));
			}
			for (int i = 0; i < attrs.size(); i++)
				newData.deleteAttributeAt(newData.attribute((attrs.get(i).name())).index());
			

			newData.setClassIndex(0);
			
			newData = getReorderedData(newData, "-R 2-last,1");
			
			
			//toArffFile(newData, "C:/Users/Fernando/Desktop/testiii.arff");
			
			return newData;
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao criar matriz de termo-documento.");
			System.exit(1);
			return null;
		}
	}

	private void toArffFile(Instances data, String arffPath) {
		try {
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(arffPath));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao salvar arquivo arff:" + arffPath);
		}
	}

	private Instances readArff(String arffPath) {
		Instances data = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(arffPath));
			data = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Arquivo arff não encontrado.");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao ler arquivo arff.");
			System.exit(1);
		}
		return data;
	}
	

	private void XMLtoCSV(File file, String csvPath) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(file);
			
			
			
			//Cria o arquivo csv
			File csvFile = new File(csvPath);
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
			
				int rating;
				String classe = "";
				String comment = "";
				
				writer.write("class, rating, comment");
				writer.newLine();
			
				NodeList comm = dom.getElementsByTagName("comment");
				for (int i=0; i<comm.getLength(); i++) {
					
					rating = Integer.valueOf(comm.item(i).getChildNodes().item(1).getTextContent()).intValue(); 
					comment = comm.item(i).getChildNodes().item(5).getTextContent();
					
					if (comment.contains("\n")) {
						comment = comment.replace("\n", " ");
					}
					if (comment.contains("\"")) {
						comment = comment.replace("\"","\\\"");
					}
					
					switch (rating) {
					case 0: classe = null; break;
					case 1: classe = "neg"; break;
					case 2: classe = "neg"; break;
					case 3: classe = "neu"; break;
					case 4: classe = "pos"; break;
					case 5: classe = "pos"; break;
					default:
						System.out.println("ERRO: rating não encontrado.");
						System.out.println(comment);
						System.exit(1);
						break;
					}

					
					//Alguns aplicativos possuem rating == 0
					if (classe != null) {
						writer.write(classe + "," + rating + ",\"" + comment + "\"");
						writer.newLine();
					}
		
				}
			
			writer.close();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	

	private void CSVtoARFF(File csv, String arff) {
		CSVLoader loader = new CSVLoader();
		try {
			loader.setSource(csv);
			Instances data = loader.getDataSet();
			
			
//			int numAttrs = data.numAttributes(); 
//			int numInstances = data.numInstances(); 
//
//			for (int instIdx = 0; instIdx < numInstances; instIdx++) { 
//				Instance currInst = data.instance(instIdx); 
//				System.out.print(instIdx+". ");
//
//				for (int attrIdx = 0; attrIdx < numAttrs; attrIdx++) { 
//					Attribute currAttr = currInst.attribute(attrIdx); 
//					if (currAttr.isNominal()) { 
//						System.out.print(currInst.stringValue(attrIdx));
//					} 
//					else if (currAttr.isNumeric()) { 
//						System.out.print(currInst.value(attrIdx));
//					} 
//					System.out.print(" -- ");
//				} 
//				System.out.println();
//			} 

			
			//Seta o attributo classe
			data.setClassIndex(0);
			
			//Retira o campo rating do arff file
			data.deleteAttributeAt(1);

			
			//Reordena os atributos para colocar a classe como último atributo
			data = getReorderedData(data, "-R last-first");
			
			
			//Transforma o campo de comentários de Nominal (categorias) para String
			String[] options = new String[2];
			options[0] = "-C";
			options[1] = "1"; //seta o atributo 1 como string
			NominalToString nomToString = new NominalToString();
			nomToString.setOptions(options);
			nomToString.setInputFormat(data);
			data = Filter.useFilter(data, nomToString);
			

			
			//Save arff file
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(arff));
			//saver.setDestination(new File(arff));
			saver.writeBatch();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Instances getReorderedData(Instances data, String options) {
		try {
			String[] opt = Utils.splitOptions(options);
			Reorder reorder = new Reorder();                      // new instance of filter
			reorder.setOptions(opt);                          // set options
			reorder.setInputFormat(data);                         // inform filter about dataset **AFTER** setting options
			data = Filter.useFilter(data, reorder);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao reoderar atributos");
			System.exit(1);
			return null;
		}
	}
	
	private Instances appendInstances(Instances train1, Instances train2,
			Instances train3) {
		
		Instances newInstances = new Instances(train1);
			newInstances.addAll(train2);
		return null;
	}
	
}
