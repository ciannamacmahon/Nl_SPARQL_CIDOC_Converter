package com.project.NlConverter;
//nlpPipeline.java
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class nlpAnalysis {
    public static LinkedList<String> qualities = new LinkedList<>();
    public static LinkedList<String[]> quest_ans_targets=new LinkedList<>();
    public static String question_entity="";
    public static String subject="";
    public static String predicate="";
    public static String object="";
    public static String questionWord="";
    public static String targetAnswer="";
    public static String unknown="";
    static StanfordCoreNLP pipeline;
    public static void main(String[] args) throws FileNotFoundException {
        String question = "Where was Obama born?";
        nlpAnalysis.init();
        populateTargetQuestions();
        //   nlpAnalysis.findPOStags(question);
        predictTarget();
        //       qualities = nlpAnalysis.dependancyParser(question);
        nlpAnalysis.dependancyParser(question);
        // nlpAnalysis.pringQual(qualities);
    }


    public static void populateTargetQuestions() throws FileNotFoundException{
        File file= new File("QA_Target.txt");
        Scanner scanner= new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(", ");

            // Add the array to the list
            quest_ans_targets.add(parts);
        }
        scanner.close();
    }

    public static void predictTarget(){
        for (String[] q:quest_ans_targets){
            System.out.println(questionWord+"hihiih"+q[0]);
            if(q[0].equals(questionWord)){
                targetAnswer=q[1];
            }
        }
        if (questionWord.equals("Who")){
            unknown="subject";
        }
        else if (questionWord.equals("When")||questionWord.equals("Where")){
            unknown="object";
        }
        System.out.println(targetAnswer+" "+ unknown);
    }
    public static void SPOExtraction(LinkedList<String> qualOfQuery) {
        LinkedList<String[]> listOfArray = intoArray(qualOfQuery);
        //More complex questions with more than one of these
        // LinkedList<String> subject = new LinkedList<>();
        //  LinkedList<String> predicate = new LinkedList<>();
        //  LinkedList<String> object = new LinkedList<>();

        // this extracts it using the root as the predicate and the subject is the entity extraction
        if( unknown.equals("subject")){
            //entity is the object of the question
            object=question_entity;
        }
        else if(unknown.equals("object")){
            subject=question_entity;
        }
        for (String[] array : listOfArray) {
            if (array[0].equals("root")) {
                predicate=array[2].substring(0, array[2].indexOf("-"));
            }
        }
        if (object.equals("")){
            object=targetAnswer;

        }
        System.out.println("Subject: "+ subject+ " Predicate: "+predicate+ " Object: "+object);


// This will be following the grammar rules shown in indeed article
        //   for (String[] array : listOfArray) {
        //   if (conatinsNsubj(array)){
        //       System.out.println("its nsubj");

        //    }
        //    else { //subj
        //        System.out.println("its nsubjpass");
        //    }

    }
    public static Boolean conatinsNsubj(String []array) {
        for (String element : array) {
            if ("nsubj".equals(element)) {
                return true;
            }
        }
        return false;
    }
    public static LinkedList<String[]> intoArray(LinkedList <String>qualOfQuery) {
        LinkedList<String[]> divArrays = new LinkedList<>();

        for (String parser : qualOfQuery) {
            System.out.println(parser);
            divArrays.add(splitString(parser));
        }
        return divArrays;
    }
    public static String[] splitString(String input) {
        System.out.println(input);
        String word_type="";
        String[] result = new String[3];

        word_type = input.substring(0, input.indexOf("("));
        System.out.println(word_type);
        result[0]=word_type;
        String subtext=input.substring(input.indexOf("(")+1,input.indexOf(")"));
        System.out.println(subtext);

        // Split the string using ',' as the delimiter
        String[] parts = subtext.split(", ");
        result[1]=parts[0];
        result[2]=parts[1];
        // for(String test1:parts){
        //     System.out.println(test1);
        // }


        return result;
    }

    //Test method; each item in the list is the characteristic of that word
    public static void pringQual(LinkedList <String>qualOfQuery){
        for (String line:qualOfQuery){
            System.out.println(line);

        }
        SPOExtraction(qualOfQuery);
    }
    public static void init()
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        pipeline = new StanfordCoreNLP(props);
    }
    @SuppressWarnings("unchecked")
    //    public static LinkedList<String> dependancyParser(String q1)
    public static void dependancyParser(String q1)
    {
        String textQualitites="";
        LinkedList<String> depParse= new LinkedList<>();
        CoreDocument document = new CoreDocument(q1);
        pipeline.annotate(document);

        CoreSentence sentence=document.sentences().get(0);
        List<String>posTags=sentence.posTags();
        System.out.println("POS tags");
        System.out.println(posTags);

        // constituency parse for the second sentence
        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        // dependency parse for the second sentence
        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Example: dependency parse");
        System.out.println(dependencyParse);
        System.out.println();

        // for NER but i think this can all be done with same object type- look into this more
        //       CoreDocument doc=new CoreDocument(q1);
        //      pipeline.annotate(doc);
        //Assumes there is only one sentence in the document ie. the question
        //   CoreMap sentence =document.get(CoreAnnotations.SentencesAnnotation.class).get(0);
//
        //   NER(doc);
        //   //Get constituency parse: CFG type output- nounPhrasees etc
        //   Tree constituencyParse = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        //   System.out.println("Constituency Parse:");
        //   System.out.println(constituencyParse);

        // //Gets the dependancy Parse
        //    //  SemanticGraph dependencyParse =
        //    //             sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        ////// System.out.println("Depenecy parse");
        //    //// for (TypedDependency typedDependency : dependencyParse.typedDependencies()) {
        //////     String line = typedDependency.toString();
        //////     depParse.add(line);
        //////}

        //return depParse;
    }
}