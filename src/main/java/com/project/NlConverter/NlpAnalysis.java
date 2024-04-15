package com.project.NlConverter;
//nlpPipeline.java
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NlpAnalysis {
    public static LinkedList<String> depQualities = new LinkedList<>();
    public static LinkedList<String[]> quest_ans_targets=new LinkedList<>();
    public static String question_entity="";
    public static String question_entity_type="";
    public static String subject="";
    public static String predicate="";
    public static String object="";
    public static String questionWord="";
    public static String targetAnswer="";
    public static String unknown="";
    static StanfordCoreNLP pipeline;
    public static String[] QuestionArray=new String[6];
    public static boolean containsFilter=false;
    public static String filterCondition="";
    private static String fullSPARQLQuery="";
    private static String graphAnswer="";
    private static String chatGPTPrompt="";

    public static void QuestionList(){
        QuestionArray[0]="When";
        QuestionArray[1]="Where";
        QuestionArray[2]="Who";
        QuestionArray[3]="How much";
        QuestionArray[4]="How many";
        QuestionArray[5]="Count";

    }
    public static void resetProperties() {
        depQualities.clear();
        quest_ans_targets.clear();
        question_entity="";
        question_entity_type="";
        subject = "";
        predicate = "";
        object="";
        questionWord = "";
        targetAnswer = "";
        unknown = "";
        containsFilter = false;
        filterCondition = "";
        fullSPARQLQuery = "";
        graphAnswer = "";
        chatGPTPrompt = "";
    }
    NlpAnalysis(){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        pipeline = new StanfordCoreNLP(props);

    }

   //public static void main(String args[]) throws FileNotFoundException{
   //    entryPoint("How many people were born in 1600?");
   // }
    public static void entryPoint(String userInputQuery) throws FileNotFoundException {
       // QuestionList();
        resetProperties();
        //String question = "Who was born in Dublin? ";
        languageAnalysis(userInputQuery);
        populateTargetQuestions();
        predictTarget();
        pringQual(depQualities);
        fullSPARQLQuery=Sparql.createSPARQLQuery(question_entity_type,question_entity,questionWord,subject,predicate,object,containsFilter,filterCondition);
      //  //if endpoint could compile
        graphAnswer=EndpointExecution.searchGraph(fullSPARQLQuery);
        System.out.println(graphAnswer);
        if(graphAnswer.trim()==""){
            chatGPTPrompt="Given this query: "+userInputQuery+"And this result: Sorry there are no records available, please check your spelling/chosen year and try another question"
                    +" Turn the result into a natural language sentence";
        }
        else if(graphAnswer.contains("http://www.w3.org/2001/XMLSchema#integer")){
       // Pattern pattern = Pattern.compile("\\d+\\^\\^http://www\\.w3\\.org/2001/XMLSchema#integer");
     //   Matcher matcher = pattern.matcher(graphAnswer);
            System.out.println("DERTGERTGERT");

            Pattern pattern2 = Pattern.compile("0\\^\\^");
            Matcher matcher2 = pattern2.matcher(graphAnswer);
            if(graphAnswer.contains("0^^"))
            {
                    // Extract the group of digits found just before '^^'
                    chatGPTPrompt="Given this query: "+userInputQuery+"And this result: Sorry there are no results available, please try a different question"
                    +" Turn the result into a natural language sentence";
            }
            else{
                chatGPTPrompt="Given this query: "+userInputQuery+"And this result: "
                +graphAnswer+" Turn the result into a natural language sentence";
            }
        
            
        }
        
        else if(questionWord.contains("who")){
            String httpLines=Arrays.stream(graphAnswer.split("\n")).filter(line ->line.contains("http")).collect(Collectors.joining("\n"));
            String gptNonHTTP=Arrays.stream(graphAnswer.split("\n")).filter(line ->!line.contains("http")).collect(Collectors.joining("\n"));
            graphAnswer=httpLines;
            chatGPTPrompt="Given this query: "+userInputQuery+"And this result of people's first and last name: "
            +gptNonHTTP+" Turn the result into a natural language sentence";
        }
        else if (chatGPTPrompt==""){
            // Cannot include '\n' symbol in the prompt at all
        chatGPTPrompt="Given this query: "+userInputQuery+"And this result: "
        +graphAnswer+" Turn the result into a natural language sentence";
        }
            
        
    }
    public static String getChatGPTPrompt(){
        System.out.println("Sending to chatGPT: "+chatGPTPrompt);
      //  naturalLanguageResult=NLG.connectToChatGPT(chatGPTPrompt);
      //  System.out.println("final answer: "+naturalLanguageResult);
        return chatGPTPrompt;

    }
    public static String getGraphResult(){
        return graphAnswer;
    }
    
    public static String getSPARQLQuery(){
        System.out.println("spark:: "+fullSPARQLQuery);
        return fullSPARQLQuery;
    }


    public static void populateTargetQuestions() throws FileNotFoundException{
        File file= new File("src/main/java/com/project/NlConverter/QA_Target.txt");
        Scanner scanner= new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
           // System.out.println(line);
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
        if (questionWord.equals("who")|| questionWord.equals("how many")|| questionWord.equals("count")){
            unknown="subject";
        }
        else if (questionWord.equals("when")||questionWord.equals("where")){
            unknown="object";
        }
        System.out.println("Target Answer: "+targetAnswer+" Word type: "+unknown);
    }
    public static void SPOExtraction(LinkedList<String> qualOfQuery) {
        LinkedList<String[]> listOfArray = intoArray(qualOfQuery);
        //More complex questions with more than one of these
        // LinkedList<String> subject = new LinkedList<>();
        //  LinkedList<String> predicate = new LinkedList<>();
        // LinkedList<String> object = new LinkedList<>();

        // this extracts it using the root as the predicate and the subject is the entity extraction
        if( unknown.equals("subject")){
            //entity is the object of the question
            object=question_entity;
            
           // System.out.print("objects: "+object.get(0));
            subject=targetAnswer;
            
        }
        else if(unknown.equals("object")){
                for (String[] array : listOfArray){
                    if (array[0].equals("compound")) {
                        String firstName=array[2].substring(0,array[2].indexOf("-"));
                        firstName=firstName.substring(0,1).toUpperCase()+firstName.substring(1);
                        String secondName=array[1].substring(0, array[1].indexOf("-"));
                        secondName=secondName.substring(0,1).toUpperCase()+secondName.substring(1);
                        subject=firstName+" "+secondName;
                        if(question_entity==subject){
                            question_entity=question_entity.substring(0,1).toUpperCase()+question_entity.substring(1);
                            subject=question_entity;
                        }else{
                            question_entity_type="PERSON";
                            question_entity=subject;
                        }
                        
                    }
                    }
                }
                object=targetAnswer;
        
        for (String[] array : listOfArray) {
            if (array[0].equals("root")) {
                String lemmaVersionPred=lemmaQuery(array[2].substring(0, array[2].indexOf("-")));
                predicate=lemmaVersionPred;
            }
            if(array[0].equals("obl"))
            {
                System.out.println("tester");
                object=array[2].substring(0,array[2].indexOf("-"));     
                object=object.substring(0,1).toUpperCase()+object.substring(1);
                question_entity=object;
                question_entity_type="PLACE";
           }
        }
        System.out.println("Subject: "+ subject+ " Predicate: "+predicate+ " Object: "+object);
        System.out.println("Entity: "+question_entity+"\nEntity type: "+question_entity_type);
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
        System.out.println("\nDependency Parser");
        for (String line:qualOfQuery){

            System.out.println("\n"+line);

        }
        SPOExtraction(qualOfQuery);
    }

    public static void languageAnalysis(String query){
        //String withoutQ=removeQ(query);
        CoreDocument document = new CoreDocument(query);
    //    Annotation annDocument=new Annotation(query);
    //    pipeline.annotate(annDocument);
        pipeline.annotate(document);
        findNER(document);
        findPOStags(document);
       // constituencyParse(document);
        depQualities=dependancyParser(query);
      //  String lemmaVersionQuery=lemmaQuery(query);

    }
    
    public static String lemmaQuery(String query){
        CoreDocument doc=new CoreDocument(query);
        pipeline.annotate(doc);
        String lemmaVersion="";
        for(CoreLabel tok:doc.tokens()){
            System.out.println(String.format("%s\t%s", tok.word(),"\nLemmatisation: "+ tok.lemma()));
            lemmaVersion=tok.lemma();
        }
        return lemmaVersion;
    }

    public static void findPOStags(CoreDocument doc){
        //Method 1
        List<CoreLabel> tokens=doc.tokens();
        int y=0;
        System.out.println("POS tags:");
        for(int i=0; i<tokens.size()-1;i++){
            CoreLabel firstToken=tokens.get(i);
            y=i+1;
            CoreLabel nextToken=tokens.get(y);
            System.out.println(String.format("%s\t%s", firstToken.word(),firstToken.tag()));
            if(firstToken.tag().contains("W")){
                if(nextToken.word().contains("many")||nextToken.word().contains("much")){
                    questionWord= firstToken.word().toLowerCase()+" "+nextToken.word();
                }
                else{
                    questionWord=firstToken.word().toLowerCase();
                }
            }
        }
        System.out.println("\nIdentified Question: "+questionWord);
    }
    public static void findNER(CoreDocument doc){
        //CoreMap sentence=doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        StringBuilder combinedEntity=new StringBuilder();
        String currEntityType=null;
        System.out.println("---");
        System.out.println("Entities found");
        for (CoreEntityMention em :doc.entityMentions()){
            String entityType=em.entityType();
            String entityText=em.text();
            if(entityType.equals(currEntityType)){
                // combinedEntity.append(", ").append(entityText); person
                question_entity=entityText;
        }
            else{
                question_entity_type=entityType;
                question_entity=entityText;
                currEntityType=entityType;
                combinedEntity.append(entityText);
               
            }
        }
        System.out.println("Entity: "+question_entity);
        System.out.println("Entity type: "+question_entity_type+"\n");

    }

    public static LinkedList<String> dependancyParser(String query)
    {
        LinkedList<String> depParse= new LinkedList<>();
        //Method 2:
        Annotation doc=new Annotation(query);
        pipeline.annotate(doc);
        CoreMap sentence=doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);

        SemanticGraph depGraph =sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        //Gets the dependancy Parse
        for (TypedDependency typedDependency : depGraph.typedDependencies()) {
            String line = typedDependency.toString();
            depParse.add(line);

        }
        return depParse;
    }
}