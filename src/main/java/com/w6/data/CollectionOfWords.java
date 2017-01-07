package com.w6.data;

import java.util.*;

public class CollectionOfWords {
    
    private Set<WordOfCollections> collection;
    private String tag;
    
    public CollectionOfWords()
    {
        this.collection = new HashSet<>();
    }
    
    public CollectionOfWords(String tag)
    {
        this();
        this.tag = tag;
    }
    
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    public boolean addNewWord(WordOfCollections word)
    {
        return collection.add(word);
    }
    
    public List<WordOfCollections> getAllWords()
    {
        List<WordOfCollections> listOfWords = new ArrayList<WordOfCollections>();
        listOfWords.addAll(collection);
        return listOfWords;
    }
    
    public String getCollocationFromCollection()
    {
        StringBuilder collocation = new StringBuilder();
        
        return collocation.toString();
    }
    
    public String getTagOfCollection()
    {
        return tag;
    }
    
    public boolean isWordInCollection(String word, int position)
    {
        return false;
    }
    
    public String getCollectionAsString()
    {
        List<WordOfCollections> words = new ArrayList(collection);
        Collections.sort(words, new ComparatorOfWords());
        return serialiseListOfWords(words);
        
    }
    
    private class ComparatorOfWords implements Comparator<WordOfCollections>
    {
        @Override
        public int compare(WordOfCollections lhs, WordOfCollections rhs) {
            return lhs.getPosition().compareTo(rhs.getPosition());
        }    
    }
     
    private String serialiseListOfWords(List<WordOfCollections> words)
    {
        StringBuilder collocation = new StringBuilder();
        
        words.forEach((word) -> {
            collocation.append(word.getValue());
            collocation.append(" ");
        });
       
        return collocation.toString();
    }
    
    public boolean hasThisCollection(CollectionOfWords collection)
    {
        return this.collection.containsAll(collection.collection);
    }
     
     
    
}
