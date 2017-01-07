package com.w6.data;

import edu.stanford.nlp.util.Pair;

import java.util.List;

public class Node {
    private Pair<String,Integer> word;
    private List<Pair<String,Node>> edges;
    
    public Node(Pair<String,Integer> w, List<Pair<String,Node>> ed)
    {
        this.edges = ed;
        this.word = w;
    }
    
    public void addEdge( String tag, Node toWhatWord)
    {
        edges.add(new Pair<>(tag, toWhatWord));
    }
    
    public Pair<String,Integer> getWord()
    {
        return word;
    }
    
    public List<Pair<String, Node>> getAllEdges()
    {
        return edges;
    }
}
