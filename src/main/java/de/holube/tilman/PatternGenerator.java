package de.holube.tilman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternGenerator {

    //private static String oldRegex = "(\\w+\\b.){" + minSentenceLength + "," + maxSentenceLength + "}$";
    //private static String regex = "\\d+\\s+([A-Za-z]+\\b\\s?){" + minSentenceLength + "," + maxSentenceLength + "}(\\.|!|\\?)$";

    // regex
    private static String regexWord = "([A-Z]?[a-z]+)";

    private static String regexAllTaggedWordEnds = "(\\|([A-Z]+)\\b\\s*)";

    private static String regexArticle                      = regexWord + "(\\|ART\\b\\s*)";

    private static String regexAttributiveAdjective         = regexWord + "(\\|ADJA\\b\\s*)";
    private static String regexPredicateAdjective           = regexWord + "(\\|ADJD\\b\\s*)";
    private static String regexAllAdjectives                = "(" + regexAttributiveAdjective + "|" + regexPredicateAdjective + ")";

    private static String regexAdverb                       = regexWord + "(\\|ADV\\b\\s*)";

    private static String regexNoun                         = regexWord + "(\\|NN\\b\\s*)";
    private static String regexAdjectiveNoun                = regexWord + "(\\|NA\\b\\s*)";
    private static String regexProperNoun                   = regexWord + "(\\|NE\\b\\s*)";
    private static String regexAllNouns                     = "(" + regexNoun + "|" + regexAdjectiveNoun + "|" + regexProperNoun + ")";

    private static String regexVAFIN                        = regexWord + "(\\|VAFIN\\b\\s*)";
    private static String regexVAIMP                        = regexWord + "(\\|VAIMP\\b\\s*)";
    private static String regexVAINF                        = regexWord + "(\\|VAINF\\b\\s*)";
    private static String regexVAPP                         = regexWord + "(\\|VAPP\\b\\s*)";
    private static String regexVMFIN                        = regexWord + "(\\|VMFIN\\b\\s*)";
    private static String regexVMINF                        = regexWord + "(\\|VMINF\\b\\s*)";
    private static String regexVMPP                         = regexWord + "(\\|VMPP\\b\\s*)";
    private static String regexVVFIN                        = regexWord + "(\\|VVFIN\\b\\s*)";
    private static String regexVVIMP                        = regexWord + "(\\|VVIMP\\b\\s*)";
    private static String regexVVINF                        = regexWord + "(\\|VVINF\\b\\s*)";
    private static String regexVVIZU                        = regexWord + "(\\|VVIZU\\b\\s*)";
    private static String regexVVPP                         = regexWord + "(\\|VVPP\\b\\s*)";
    private static String regexAllVerbs                     = "(" + regexVAFIN + "|" + regexVAIMP + "|" + regexVAINF + 
        "|" + regexVAPP + "|" + regexVMFIN + "|" + regexVMINF + "|" + regexVMPP + "|" + regexVVFIN + "|" + regexVVIMP + 
        "|" + regexVVINF + "|" + regexVVIZU + "|" + regexVVPP + "|" + regexVVPP + ")";
    
    private static String[] regexValidTaggedSentences = new String[] {
        regexArticle + "?" + regexAllAdjectives + "?" + regexAllNouns + regexAllVerbs + regexAdverb + "?" + regexArticle + "?" + regexAllAdjectives + "?" + regexAllNouns + "?"
    };

    public static Pattern getValidNumberedTaggedSentences() {
        String regex = "((" + regexValidTaggedSentences[0] + ")";
        for (int i = 1; i < regexValidTaggedSentences.length; i++) {
            regex += "|(" + regexValidTaggedSentences[i] + ")";
        }
        regex += ")";
        regex = getNumberedTaggedSentence(regex);
        //System.out.println(regex);
        return Pattern.compile(regex);
    }

    public static Pattern getValidSentences() {
        String regex = "((" + regexValidTaggedSentences[0] + ")";
        for (int i = 1; i < regexValidTaggedSentences.length; i++) {
            regex += "|(" + regexValidTaggedSentences[i] + ")";
        }
        regex += ")";
        regex = getNumberedTaggedSentence(regex);
        //System.out.println(regex);
        return Pattern.compile(regex);
    }

    public static String getNumberedTaggedSentence(String content) {
        return "\\d+\\s+(" + content + ")((\\.\\|\\$)|(!\\|\\$))(\\.)$";
    }

    public static String getNumberedSentence(String content) {
        return "\\d+\\s+(" + content + ")(\\.)$";
    }

    public static String getTaggedWordEnds() {
        return regexAllTaggedWordEnds;
    }

    public static Pattern getUncommonWordsNumbered(Set<String> allWords, int freqencyThreshhold) throws JSONException, IOException {
        JSONObject data = new JSONObject(new String(Files.readAllBytes(Paths.get("database.json"))));
        List<String> uncommonWords = new ArrayList<String>();
        for (String word : allWords) {
            if (data.has(word)) {
                if (data.getInt(word) < freqencyThreshhold) {
                    uncommonWords.add(word);
                }
            }
        }
        String regex = "(.*)((\\b" + String.join("\\b)|(\\b", uncommonWords) + "\\b))(.*)";

        return Pattern.compile(regex);
    }
}
