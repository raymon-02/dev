package com.w6.nlp;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DateTimeParser {
    
    private Collection<TypedDependency> listOfDependencies;
    private DependencyTree dependencyTree;
    private Collection<String> targetTags;
    private Sentence sentence;
    
    public DateTimeParser(Collection<TypedDependency> listOfDependencies, DependencyTree dependencyTree, Collection<String> targetTags, Sentence sentence)
    {
        this.dependencyTree = dependencyTree;
        this.listOfDependencies = listOfDependencies;
        this.targetTags = targetTags;
        this.sentence = sentence;
    }

    
    public List<String> parseDateAndTime() 
    {
        
        List<String> timeAndDate = new ArrayList<>();
    
        /*List<Pair<String,Integer>> dates = new ArrayList<>();
        
        List<String> words = sentence.lemmas();
        List<String> tags = sentence.nerTags();
        
        for (int i = 0; i < tags.size(); ++i) 
        {
            if (targetTags.contains(tags.get(i))) 
            {
                dates.add(new Pair(words.get(i), i+1));
            }
        }
        
        
        
        List<CollectionOfWords> collections = dependencyTree.getComplexEntity(dates);
        for(int i=0; i<collections.size(); i++)
        {
            boolean isInnerCollection = false;
            for(int j=0; j<collections.size(); j++)
            {
                if(i != j)
                {
                    if(collections.get(i).hasThisCollection(collections.get(j)))
                    {
                        isInnerCollection = true;
                    }
                }
            }
            if(!isInnerCollection)
            {
                timeAndDate.add(collections.get(i).getCollectionAsString());
            }
        }
        */
        
        dependencyTree.getCollectionsByTag("nmod:tmod").forEach((collection) -> {
                                        timeAndDate.add(collection.getCollectionAsString());
                                    });
        
         dependencyTree.getCollectionsByTag("nmod:since").forEach((collection) -> {
                                        timeAndDate.add(collection.getCollectionAsString());
                                    });
         
          dependencyTree.getCollectionsByTag("nmod:on").forEach((collection) -> {
                                        timeAndDate.add(collection.getCollectionAsString());
                                    });
         
        
        return timeAndDate;
    }
}