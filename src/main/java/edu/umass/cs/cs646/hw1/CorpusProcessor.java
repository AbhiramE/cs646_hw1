package edu.umass.cs.cs646.hw1;

import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valar Dohaeris on 9/16/16.
 */
public class CorpusProcessor {

    public CorpusProcessor() throws FileNotFoundException {
    }

    public static void main(String[] args) {
        double startime = System.currentTimeMillis();

        String pathCorpus = "/home/abhiram/codebase/InformationRetrieval/acm_corpus";

        Pattern docNoPattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>",
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

        File textList = new File("/home/abhiram/codebase/InformationRetrieval/TextList");
        File docList=new File("/home/abhiram/codebase/InformationRetrieval/DocList");


        try {
            BufferedWriter bwTextList = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textList)));
            BufferedWriter bwDocList = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docList)));

            double countOfDocs = 0, informationCount = 0, retrievalCount = 0;
            double totalLength = 0,queryAndReformulationCount = 0;
            List<Pair<String, Double>> longestDoc = new ArrayList<>();
            longestDoc.add(new Pair<>("zzz", -999.0));
            List<String> uniqueWords = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(pathCorpus))) {
                String line;
                while ((line = br.readLine()) != null) {

                    String docNo = null, text = "";

                    line.trim();
                    if (line.equals("<DOC>")) {
                        line = br.readLine();
                        Matcher docNoMatcher = docNoPattern.matcher(line);
                        while (docNoMatcher.find())
                            docNo = docNoMatcher.group(1).trim();

                        //Read Lines till you get text
                        br.readLine();

                        //Read Text
                        while (!(line = br.readLine().trim()).equals("</TEXT>"))
                            text += line;

                        //Tokenisation begins.
                        text = text.toLowerCase();
                        text=text.replaceAll("\\."," ").replaceAll(","," ").replaceAll("-"," ").replaceAll("/"," ").replaceAll(";"," ")
                                .replaceAll(":"," ").replaceAll("\\?"," ").replaceAll("\\["," ").replaceAll("]"," ").replaceAll("`"," ")
                                .replaceAll("\\("," ").replaceAll("\\)"," ").replaceAll("%"," ").replaceAll("&"," ");
                        text = text.replaceAll("\\s+", " ");

                        String[] textString=text.split(" ");


                        //Keep an eye for the largest document
                        if (textString.length > longestDoc.get(0).getValue()) {
                            longestDoc = new ArrayList<>();
                            longestDoc.add(new Pair<>(docNo,(double)textString.length));
                        } else if (textString.length == longestDoc.get(0).getValue())
                            longestDoc.add(new Pair<>(docNo, (double)textString.length));

                        List<String> words=Arrays.asList(textString);
                        totalLength += words.size();
                        words.stream().filter(word -> !uniqueWords.contains(word)).forEach(uniqueWords::add);
                        if (words.contains("information"))
                            informationCount++;
                        if (words.contains("retrieval"))
                            retrievalCount++;
                        if (words.contains("query") && words.contains("reformulation")) {
                            System.out.print("\n\t" + docNo);
                            queryAndReformulationCount++;
                        }
                        countOfDocs++;
                    }
                }
                bwTextList.close();
                bwDocList.close();
                br.close();

                double idfInformation = Math.log(countOfDocs / informationCount);
                double idfRetrieval = Math.log(countOfDocs / retrievalCount);


                //Outputs!
                System.out.print("\n1. Total document length " + countOfDocs);
                System.out.print("\n2. Average length " + totalLength / countOfDocs);
                System.out.print("\n3. Total count of unique words " + uniqueWords.size());
                System.out.print("\n4. Largest documents \n \t Size of doc " + longestDoc.get(0).getValue());
                System.out.print("\n \t Doc Numbers for largest doc");
                for (Pair<String, Double> doc : longestDoc)
                    System.out.print("\n \t \t DOCNO: " + doc.getKey());

                System.out.print("\n5. Words counts of information and retrieval");
                System.out.print("\n\tinformation count:" + informationCount + "\tidf:" + idfInformation);
                System.out.print("\n\tretrieval count:" + retrievalCount + "\tidf:" + idfRetrieval);

                //Find query and reformulation from TextList
                System.out.print("\n 6. Documents where Query and Reformulations appear together\n");
                System.out.print("\n \tTotal number of appearances of Query and Reformulation " + queryAndReformulationCount);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
                System.out.print("\n Total Program Time " + String.valueOf(System.currentTimeMillis() - startime));
            }
        }
    }
