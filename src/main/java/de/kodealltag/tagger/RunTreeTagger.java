package de.kodealltag.tagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.FileUtils;

import static java.util.Arrays.asList;

public class RunTreeTagger {
	private static final String sentModelPath = "src/main/resources/de-sent.bin";
	private static final String tokModelPath = "src/main/resources/de-token.bin";
	private static final String ttPath = "src/main/resources/TreeTagger/";
	private static final String ttModel = "german-utf8.par:utf-8";
	
	private static SentenceModel sentModel;
	private static TokenizerModel tokModel;
	
	private static HashMap<String, HashMap<String, Integer>> statsEmails = new HashMap<String, HashMap<String,Integer>>();
	private static HashMap<String, Integer> descStatsCorpus = new HashMap<String, Integer>();
	private static HashSet<String> numTypes = new HashSet<String>();
	private static HashSet<String> numLemma = new HashSet<String>();
	
	static SentenceDetectorME sentDetector;
	static TokenizerME tokDetector;
	static TreeTaggerWrapper<String> tt = null;
	static Collection<File> fileList;
	
	static PrintWriter writer;
	
	public static void main(String args[]) throws Exception {
	// Point TT4J to the TreeTagger installation directory. The executable is expected
	// in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger"
		System.setProperty("treetagger.home", ttPath);
		tt = new TreeTaggerWrapper<String>();
		
		initializeComponents();
//		initializeMaps();
		
		for (String arg : args) {
			fileList = FileUtils.listFiles(new File(arg), new String[]{"txt"}, true);
			for (File email : fileList) {
//				System.out.println(email.getAbsolutePath());
				writer = new PrintWriter("tok/"+email.getName()+".tok", "UTF-8");
//				// count emails
//				descStatsCorpus.put("Num_Mails", descStatsCorpus.get("Num_Mails") + 1 );
				String cEmail = FileUtils.readFileToString(new File(email.getAbsolutePath()));
				for (String sent : getSentences(cEmail)) {
//					// count sentences
//					descStatsCorpus.put("Num_Sent", descStatsCorpus.get("Num_Sent") + 1 );
					tagText(tokText(sent));
					writer.println("");
				}
				writer.close();
			}
		}
	}
	
	private static void initializeMaps() {
		descStatsCorpus.put("Num_Mails", 0);
		descStatsCorpus.put("Num_Sent", 0);
		descStatsCorpus.put("Num_Tokens", 0);
	}

	private static String[] getSentences(String cEmail) {
		return sentDetector.sentDetect(cEmail);
	}

	private static void initializeComponents() throws IOException {
		InputStream modelIn;
		
		// load sentence model
		modelIn = new FileInputStream(sentModelPath);
		try {
			sentModel = new SentenceModel(modelIn);
			sentDetector = new SentenceDetectorME(sentModel);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
		
		// load token model
		modelIn = new FileInputStream(tokModelPath);
		try {
			tokModel = new TokenizerModel(modelIn);
			tokDetector = new TokenizerME(tokModel);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
		
		// load tt Model
		tt.setModel(ttModel);
		tt.setHandler(new TokenHandler<String>() {
			public void token(String token, String pos, String lemma) {
//				System.out.println(token + "\t" + pos + "\t" + lemma);
				writer.println(token + "\t" + pos + "\t" + lemma);
			}
		});
	}

	private static String[] tokText(String email) throws IOException {
		String[] tokens = tokDetector.tokenize(email);
		// count tokens
//		descStatsCorpus.put("Num_Tokens", descStatsCorpus.get("Num_Tokens") + tokens.length);
		return tokens;
	}
	
	private static void tagText(String[] emailAsList) throws Exception {
		try {
			tt.process(asList(emailAsList));
		}
		finally {
//			System.out.println(tmp_results);
//			tmp_results.clear();
		}
	}
}
