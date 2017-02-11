package com.w6.nlp;

import edu.stanford.nlp.trees.TypedDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class GetDoerAndVictim 
{    
    private final Collection<TypedDependency> listOfDependencies;
    private final DependencyTree dependencyTree;
    

    public GetDoerAndVictim(Collection<TypedDependency> listOfDependencies, DependencyTree dependencyTree)
    {
        this.listOfDependencies = listOfDependencies;
        this.dependencyTree = dependencyTree;
    }
    
    public List<String> getSubjectsOfViolence(List<String> violenceVerbs)
    {
        List<String> subjectsOfViolence = new ArrayList<>();
       
        dependencyTree.getCollectionsFromWordsByTag(violenceVerbs, "dobj").forEach((collection) -> {
                                        subjectsOfViolence.add(collection.getCollectionAsString());
                                    });
        
        dependencyTree.getCollectionsFromWordsByTag(violenceVerbs, "nsubjpass").forEach((collection) -> {
                                        subjectsOfViolence.add(collection.getCollectionAsString());
                                    });
        
        return subjectsOfViolence;
    }
    
    public List<String> getObjectsOfViolence(List<String> violenceVerbs)
    {
        List<String> subjectsOfViolence = new ArrayList<>();
        
        dependencyTree.getCollectionsFromWordsByTag(violenceVerbs, "nsubj").forEach((collection) -> {
                                        subjectsOfViolence.add(collection.getCollectionAsString());
                                    });
        
        dependencyTree.getCollectionsFromWordsByTag(violenceVerbs, "nmod:agent").forEach((collection) -> {
                                        subjectsOfViolence.add(collection.getCollectionAsString());
                                    });
        
        return subjectsOfViolence;
    }
   
}
