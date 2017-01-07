package com.w6.nlp;

import com.w6.data.CollectionOfWords;
import com.w6.data.Node;
import com.w6.data.WordOfCollections;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DependencyTree {
    
    private final HashMap<Pair<String, Integer>, Node> mapOfNodes;
    private final Collection<TypedDependency> dependencies;
    
    public DependencyTree(Collection<TypedDependency> dependencies)
    {
        this.dependencies = dependencies;
        this.mapOfNodes = new HashMap<>();
        
        for (TypedDependency dependency : dependencies)
        {
            String tag = dependency.reln().toString();
            Pair<String, Integer> mainWord = new Pair(dependency.gov().value(), dependency.gov().index());
            Pair<String, Integer> depWord = new Pair(dependency.dep().value(), dependency.dep().index());
            
            addWordToMap(mainWord, mapOfNodes);
            addWordToMap(depWord, mapOfNodes);
            
            Node firstWord = mapOfNodes.get(mainWord);
            firstWord.addEdge(tag, mapOfNodes.get(depWord));
        }  
        
    }
    
    public List<CollectionOfWords> getCollectionsFromWordsByTag(List<String> keyWords, String tag)
    {
        List<Pair<String, Integer>> dependentWordList = new ArrayList<>();
        for(TypedDependency dependency : dependencies)
        {
            String localTag = dependency.reln().toString();
            Pair<String, Integer> mainWord = new Pair(dependency.gov().value(), dependency.gov().index());
            Pair<String, Integer> dependentWord = new Pair(dependency.dep().value(), dependency.dep().index());
            
            if(keyWords.contains(mainWord.first))
            {
                if(localTag.equals(tag))
                {
                    dependentWordList.add(dependentWord);
                }
            }
        }
        
        return getComplexEntity(dependentWordList);
    }
    
    public List<CollectionOfWords> getCollectionsFromWords(List<String> keyWords)
    {
        List<Pair<String, Integer>> dependentWordList = new ArrayList<>();
        for(TypedDependency dependency : dependencies)
        {
            Pair<String, Integer> mainWord = new Pair(dependency.gov().value(), dependency.gov().index());
            Pair<String, Integer> dependentWord = new Pair(dependency.dep().value(), dependency.dep().index());
           
            if(keyWords.contains(mainWord.first))
            {
                dependentWordList.add(mainWord);
            } else 
            {
                if(keyWords.contains(dependentWord.first))
                {
                     dependentWordList.add(dependentWord);
                }
            }
        }
        
        return getComplexEntity(dependentWordList);
    }
    
    public List<CollectionOfWords> getCollectionsByTag(String tag)
    {
        List<Pair<String, Integer>> dependentWordList = new ArrayList<>();
        for(TypedDependency dependency : dependencies)
        {
            String localTag = dependency.reln().toString();
            
            Pair<String, Integer> dependentWord = new Pair(dependency.dep().value(), dependency.dep().index());
            
            if(localTag.equals(tag))
            {
                dependentWordList.add(dependentWord);
            }
        }
        
        return getComplexEntity(dependentWordList);
    }
    
    
    
    
    public  List<CollectionOfWords> getComplexEntity(List<Pair<String, Integer>> listOfWords)
    {
        List<CollectionOfWords> arrayOfCollections = new ArrayList();
         
        for (Pair<String, Integer> word : listOfWords)
        {
            List<Pair<String, Integer>> childs = getAllChilds(word,mapOfNodes);
            CollectionOfWords collection = getCollectionFromWords(childs);
            arrayOfCollections.add(collection);
        }
        
        return arrayOfCollections;
    }
    
    private CollectionOfWords getCollectionFromWords(List<Pair<String, Integer>> words)
    {
        CollectionOfWords collection = new CollectionOfWords();
        
        for(Pair<String, Integer> word : words)
        {
            WordOfCollections newWord = new WordOfCollections(word.first,word.second,collection);
            collection.addNewWord(newWord);
        }
        
        return collection;
    }
    

    private  List<Pair<String, Integer>> getAllChilds(Pair<String, Integer> word, HashMap<Pair<String, Integer>,Node> mapOfNodes)
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
    
    
    private void addWordToMap(Pair<String, Integer> word, HashMap<Pair<String, Integer>, Node> mapOfNodes)
    {
        if(!mapOfNodes.containsKey(word))
        {
            mapOfNodes.put(word, new Node(word, new ArrayList<>()));
        }
    }
    
}
