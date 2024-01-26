package com.project.NlConverter;
//nlpPipeline.java
import java.util.LinkedList;
import java.util.Properties;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class nlpAnalysis {
    public static LinkedList<String> depQualities = new LinkedList<>();
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
        languageAnalysis(question);
        populateTargetQuestions();
        predictTarget();
        nlpAnalysis.pringQual(depQualities);
    }


    public static void populateTargetQuestions() throws FileNotFoundException{
        File file= new File("QA_Target.txt");
        Scanner scanner= new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
          //  System.out.println(line);
            String[] parts = line.split(", ");

            // Add the array to the list
            quest_ans_targets.add(parts);
        }
        scanner.close();
    }

    public static void predictTarget(){
        for (String[] q:quest_ans_targets){
           // System.out.println(questionWord+"hihiih"+q[0]);
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
        System.out.println("Target Answer: "+targetAnswer+" Word type: "+unknown);
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
            // each line of the depParser is printed, split into an array of words to enable SPO extraction
          //  System.out.println(parser);
            divArrays.add(splitString(parser));
        }
        return divArrays;
    }
    public static String[] splitString(String input) {
        String word_type="";
        String[] result = new String[3];
        //Result with split the sentence into an array of 3 so SPO extraction can take place
        //Result=[wordType][associated word][evaluated word]
        //Eg:    [punct][born][?]


        // Sentence: aux:pass(born-4, was-2)
        word_type = input.substring(0, input.indexOf("("));
        // aux:pass
       // System.out.println(word_type);
        result[0]=word_type;
        String subtext=input.substring(input.indexOf("(")+1,input.indexOf(")"));
        //Subtext: born-4, was-2
      //  System.out.println(subtext);

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

    public static void languageAnalysis(String query){
        LinkedList<String> depParse= new LinkedList<>();
        CoreDocument document = new CoreDocument(query);
        pipeline.annotate(document);
        findPOStags(document);
        findNER(document);
        constituencyParse(document);
        depQualities=dependancyParser(query);
    }

    public static void findPOStags(CoreDocument doc){
        CoreSentence sentence=doc.sentences().get(0);
        //Method 1
        for(CoreLabel token:doc.tokens()){
            System.out.println(String.format("%s\t%s", token.word(),token.tag()));
            if(token.tag().contains("W")){
                questionWord=token.word();
            }
        }
    }
    public static void findNER(CoreDocument doc){
        System.out.println("---");
        System.out.println("entities found");
        String entity="";
        for (CoreEntityMention em : doc.entityMentions()){
            System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());
            question_entity=em.text();

        }
       // System.out.println("---");
       // System.out.println("tokens and ner tags");
        // Gives this output: (Where,O) (was,O) (Obama,PERSON) (born,O) (?,O)
      //  String tokensAndNERTags = doc.tokens().stream().map(token -> "("+token.word()+","+token.ner()+")").collect(
      //          Collectors.joining(" "));
     //   System.out.println(tokensAndNERTags);
        //   return entity;

    }

    public static void constituencyParse(CoreDocument doc){
        CoreSentence sentence=doc.sentences().get(0);
        //Method 1:
        Tree constitParse1 = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constitParse1);
        System.out.println();

    }
    @SuppressWarnings("unchecked")
    public static LinkedList<String> dependancyParser(String query)
    {
        LinkedList<String> depParse= new LinkedList<>();
        //Method 2:
        Annotation doc=new Annotation(query);
        pipeline.annotate(doc);
        CoreMap sentence=doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        SemanticGraph depGraph =sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        //Gets the dependancy Parse
        System.out.println("Depenecy parse");
        for (TypedDependency typedDependency : depGraph.typedDependencies()) {
            String line = typedDependency.toString();
            depParse.add(line);

        }
        return depParse;
    }
}