package com.firstpeak;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.common.PartialVectorMerger;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;

public class TFIDFTester {

    String outputFolder;
    Configuration configuration;
    FileSystem fileSystem;
    Path documentsSequencePath;
    Path tokenizedDocumentsPath;
    Path tfidfPath;
    Path termFrequencyVectorsPath;

    public static void main(String args[]) throws Exception {

        TFIDFTester tester = new TFIDFTester();

        tester.createTestDocuments2();
        tester.calculateTfIdf();

        tester.printSequenceFile(tester.documentsSequencePath);

        System.out.println("\n Step 1: Word count ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "wordcount/part-r-00000"));

        System.out.println("\n Step 2: Word dictionary ");
        tester.printSequenceFile(new Path(tester.outputFolder,
                "dictionary.file-0"));

        System.out.println("\n Step 3: Term Frequency Vectors ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "tf-vectors/part-r-00000"));

        System.out.println("\n Step 4: Document Frequency ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "tfidf/df-count/part-r-00000"));

        System.out.println("\n Step 5: TFIDF ");
        
        List<Integer> indexList = new ArrayList<Integer>();
        List<Float> tfidfList = new ArrayList<Float>();
        
        double seventyFivePercentile = tester.printSequenceFileTFIDF(new Path(tester.outputFolder
                + "tfidf/tfidf-vectors/part-r-00000"), indexList, tfidfList);
        
        System.out.format("75 percentile: %s", seventyFivePercentile);

    }

    public TFIDFTester() throws IOException {

        configuration = new Configuration();
        fileSystem = FileSystem.get(configuration);

        outputFolder = "output/";
        documentsSequencePath = new Path(outputFolder, "sequence");
        tokenizedDocumentsPath = new Path(outputFolder,
                DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
        tfidfPath = new Path(outputFolder + "tfidf");
        termFrequencyVectorsPath = new Path(outputFolder
                + DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER);
    }

    public void createTestDocuments() throws IOException {
        SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                configuration, documentsSequencePath, Text.class, Text.class);

        Text id1 = new Text("Document 1");
        Text text1 = new Text("I saw a yellow car and a green car. Google stock is tanking");
        writer.append(id1, text1);

        Text id2 = new Text("Document 2");
        Text text2 = new Text("You saw a red car. Google stock is taking a dive");
        writer.append(id2, text2);

        writer.close();
    }
    
    public void createTestDocuments2() throws IOException {
        SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                configuration, documentsSequencePath, Text.class, Text.class);

        String content = readFile("input/google_bad.txt");
        
        Text id1 = new Text("Document 1");
        Text text1 = new Text(content);
        writer.append(id1, text1);
        
        content = readFile("input/google_good.txt");

        Text id2 = new Text("Document 2");
        Text text2 = new Text(content);
        writer.append(id2, text2);

        writer.close();
    }

    public void calculateTfIdf() throws ClassNotFoundException, IOException,
            InterruptedException {

        // Tokenize the documents using Apache Lucene StandardAnalyzer
        DocumentProcessor.tokenizeDocuments(documentsSequencePath,
                StandardAnalyzer.class, tokenizedDocumentsPath, configuration);

        DictionaryVectorizer.createTermFrequencyVectors(tokenizedDocumentsPath,
                new Path(outputFolder),
                DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER,
                configuration, 1, 1, 0.0f, PartialVectorMerger.NO_NORMALIZING,
                true, 1, 100, false, false);

        Pair<Long[], List<Path>> documentFrequencies = TFIDFConverter
                .calculateDF(termFrequencyVectorsPath, tfidfPath,
                        configuration, 100);

        TFIDFConverter.processTfIdf(termFrequencyVectorsPath, tfidfPath,
                configuration, documentFrequencies, 1, 100,
                PartialVectorMerger.NO_NORMALIZING, false, false, false, 1);
        
    }

    void printSequenceFile(Path path) {
        SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(
                path, configuration);
        for (Pair<Writable, Writable> pair : iterable) {
            System.out
                    .format("%10s -> %s\n", pair.getFirst(), pair.getSecond());
        }
        
    }
    
    double printSequenceFileTFIDF(Path path, List<Integer> indexList, List<Float> tfidfList) {
        SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(
                path, configuration);
        
        for (Pair<Writable, Writable> pair : iterable) {
        	String docString = pair.getSecond().toString();
        	docString = docString.substring(1, docString.lastIndexOf('}')-1);
        	String[] pairs = docString.split(",");
        	
        	for(int i=0; i<pairs.length; i++){
        		String pair1 = pairs[i];
        		String[] pair2 = pair1.split(":");
        		
        		indexList.add(Integer.parseInt(pair2[0]));
            	tfidfList.add(Float.parseFloat(pair2[1]));
        	}
        	
            System.out
                    .format("%10s -> %s\n", pair.getFirst(), pair.getSecond());
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        // Add the data from the array
        Object[] inputArray = tfidfList.toArray();
        
        for( int i = 0; i < inputArray.length; i++) {
        	stats.addValue((Float)inputArray[i]);
        }
        
        double seventyFivePercentile = stats.getPercentile(75);
        
        return seventyFivePercentile;
    }
    
    protected String readFile(String fileName) {
		File file = new File(fileName);
		String str = null;
		try {
			str = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
}