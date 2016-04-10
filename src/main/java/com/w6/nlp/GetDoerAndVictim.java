package com.w6.nlp;

import com.w6.data.Node;
import com.w6.data.ObjectsAndSubjects;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;



public class GetDoerAndVictim 
{    
    static final TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    static final GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();


    private static void getResultWithViolentVerbs(
            Collection<TypedDependency> list, 
            ObjectsAndSubjects result, 
            List<String> violentList
    ){
        List<Pair<String, Integer>> listOfSubjects = new ArrayList<>();
        List<Pair<String, Integer>> listOfObjects = new ArrayList<>();
        
        HashMap<Pair<String, Integer>, Node> mapOfNodes = new HashMap<>();
        
        for (TypedDependency dependency:list)
        {
            String tag = dependency.reln().toString();
            
            Pair<String, Integer> govStruct = new Pair(dependency.gov().value(), dependency.gov().index());
            Pair<String, Integer> depStruct = new Pair(dependency.dep().value(), dependency.dep().index());
            
            
            addWordToMap(govStruct, mapOfNodes);
            addWordToMap(depStruct, mapOfNodes);
            
            Node firstWord = mapOfNodes.get(govStruct);
            firstWord.addEdge(tag, mapOfNodes.get(depStruct));
            
            if( violentList.contains(govStruct.first))
            {   
                if(tag.equals("nsubj") || tag.equals("nmod:agent"))
                {
                    listOfSubjects.add(depStruct);
                }

                if(tag.equals("dobj") || tag.equals("nsubjpass"))
                {
                    listOfObjects.add(depStruct);
                }
            }
        }
        
        result.objects = getComplexEntity(listOfObjects, mapOfNodes);
        result.subjects = getComplexEntity(listOfSubjects, mapOfNodes);
        
    }
    
    private static void addWordToMap(Pair<String, Integer> word, HashMap<Pair<String, Integer>, Node> mapOfNodes)
    {
        if(!mapOfNodes.containsKey(word))
        {
            mapOfNodes.put(word, new Node(word, new ArrayList<>()));
        }
    }
    
    private static ArrayList<String> getComplexEntity(List<Pair<String, Integer>> listOfObjects, HashMap<Pair<String, Integer>, Node> mapOfNodes)
    {
        ArrayList<String> result = new ArrayList();
         
        for (Pair<String, Integer> word : listOfObjects)
        {
            List<Pair<String, Integer>> childs = getAllChilds(word,mapOfNodes);
            Collections.sort(childs, new ComparatorOfWords());
            String newObject = fromListToOneWord(childs);
            result.add(newObject);
        }
        
        return result;
    }
    
    private static String fromListToOneWord(List<Pair<String, Integer>> words)
    {
        StringBuilder result = new StringBuilder();
        
        for (Pair<String, Integer> word : words)
        {
            result.append(word.first + " ");
        }
        
        return result.toString();
    }
    
    private static List<Pair<String, Integer>> getAllChilds(Pair<String, Integer> word, HashMap<Pair<String, Integer>,Node> mapOfNodes)
    {
        ArrayList<Pair<String, Integer>> result = new ArrayList<>();
        Node nodeOfWord = mapOfNodes.get(word);
        result.add(word);
        
        for(Pair<String, Node> node : nodeOfWord.getAllEdges())
        {
            List<Pair<String, Integer>> listOfChilds = getAllChilds(node.second.getWord(), mapOfNodes);
            result.addAll(listOfChilds);
        }
        
        return result;
    }
    
    private static class ComparatorOfWords implements Comparator<Pair<String, Integer>>
    {
        @Override
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            return o1.second.compareTo(o2.second);
        }    
    }

    public static ObjectsAndSubjects getSubjectAndObjectOfViolence(
            Tree tree, 
            List<String> violentVerbs
    ) {
        ObjectsAndSubjects result = new ObjectsAndSubjects();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
            
        getResultWithViolentVerbs(td, result, violentVerbs);
        return result;
    }
}
