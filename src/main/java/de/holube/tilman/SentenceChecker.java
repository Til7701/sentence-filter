package de.holube.tilman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

public class SentenceChecker {

    // paths
    private static final String fileNameWRT = "deu-de_web-wrt_2019_1M-sentences_tagged";
    private static final String readPathWRT = fileNameWRT + ".txt";
    private static final String writePathWRT = fileNameWRT + "_filtered.txt";
    private static final String fileNameGOESA = "GOESA";
    private static final String readPathGOESA = fileNameGOESA + ".txt";
    private static final String writePathGOESA = fileNameGOESA + "_filtered.txt";

    private static final int freqencyThreshhold = 1000;

    public static void main(String[] args) throws IOException {
        wrt();
        goesa();
    }

    private static void goesa() throws IOException {
        // read
        List<String> sentenceList = readFile(readPathGOESA);
        System.out.println(sentenceList.size() + " sentences read from file.");

        // unknown check
        Set<String> allWords = getAllWords(sentenceList);
        System.out.println(allWords.size() + " different words.");
        List<String> unknownWords = getUnknownWords(allWords);
        System.out.println(unknownWords.size() + " unknown words to look up.");

        // look unknown words up
        while (unknownWords.size() > 0) {
            APIRequest.addWordsToJSON(unknownWords);
            unknownWords = getUnknownWords(allWords);
        }

        // remove uncommon words
        Pattern uncommonPattern = PatternGenerator.getUncommonWordsNumbered(allWords, freqencyThreshhold);
        sentenceList = checkStringsForPattern(sentenceList, uncommonPattern, false);
        System.out.println(sentenceList.size() + " sentences remaining after freqency check.");

        writeToFile(sentenceList, writePathGOESA);
    }

    private static void wrt() throws IOException {
        // read
        List<String> sentenceList = readFile(readPathWRT);
        System.out.println(sentenceList.size() + " sentences read from file.");

        // length check
        Pattern lengthPattern = PatternGenerator.getNumberedTaggedSentenceLength(4, 7);
        sentenceList = checkStringsForPattern(sentenceList, lengthPattern, true);
        System.out.println(sentenceList.size() + " sentences remaining after length check.");

        // structure check
        Pattern sentencePattern = PatternGenerator.getValidNumberedTaggedSentences();
        sentenceList = checkStringsForPattern(sentenceList, sentencePattern, true);
        System.out.println(sentenceList.size() + " sentences remaining after structure check.");

        // clean up
        sentenceList = cleanUpSentences(sentenceList);
        writeToFile(sentenceList, writePathWRT);

        // unknown check
        Set<String> allWords = getAllWords(sentenceList);
        System.out.println(allWords.size() + " different words.");
        List<String> unknownWords = getUnknownWords(allWords);
        System.out.println(unknownWords.size() + " unknown words to look up.");

        // look unknown words up
        while (unknownWords.size() > 0) {
            APIRequest.addWordsToJSON(unknownWords);
            unknownWords = getUnknownWords(allWords);
        }

        // remove uncommon words
        Pattern uncommonPattern = PatternGenerator.getUncommonWordsNumbered(allWords, freqencyThreshhold);
        sentenceList = checkStringsForPattern(sentenceList, uncommonPattern, false);
        System.out.println(sentenceList.size() + " sentences remaining after freqency check.");

        writeToFile(sentenceList, writePathWRT);
    }

    private static List<String> readFile(String path) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(path));

        List<String> sentenceList = new ArrayList<String>();
        String sentence = in.readLine();
        while (sentence != null) {
            sentenceList.add(sentence);
            sentence = in.readLine();
        }

        in.close();
        return sentenceList;
    }

    private static List<String> checkStringsForPattern(List<String> list, Pattern pattern, boolean bool) {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < list.size(); i++) {
            if (pattern.matcher(list.get(i)).matches() == bool) {
                result.add(list.get(i));
            }
        }

        return result;
    }

    private static List<String> cleanUpSentences(List<String> list) {
        List<String> result = new ArrayList<String>();
        String regex = PatternGenerator.getTaggedWordEnds();

        for (int i = 0; i < list.size(); i++) {
            String sentence = list.get(i);
            String[] words = sentence.split(regex);
            sentence = String.join(" ", words);
            result.add(sentence.substring(0, sentence.length() - 5) + ".");
        }

        return result;
    }

    private static Set<String> getAllWords(List<String> list) {
        Set<String> result = new HashSet<String>();

        for (int i = 0; i < list.size(); i++) {
            String sentence = list.get(i);
            String[] words = sentence.split("\\s+");
            for (int j = 1; j < words.length; j++) {
                if (words[j].endsWith(".") || words[j].endsWith("!") || words[j].endsWith("?")) words[j] = words[j].substring(0, words[j].length() - 1);
                result.add(words[j]);
            }
        }

        return result;
    }

    private static List<String> getUnknownWords(Set<String> list) throws JSONException, IOException {
        List<String> unknownWords = new ArrayList<String>();
        JSONObject data = new JSONObject(new String(Files.readAllBytes(Paths.get("database.json"))));
        for (String word : list) {
            if (!data.has(word)) {
                unknownWords.add(word);
            }
        }

        return unknownWords;
    }

    private static void writeToFile(List<String> array, String path) throws IOException {
        FileWriter writer = new FileWriter(path);
        for (String string : array) {
            writer.write(string + "\n"+ "");
        }
        writer.close();
    }
}
