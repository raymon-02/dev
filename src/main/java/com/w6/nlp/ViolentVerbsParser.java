package com.w6.nlp;

import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViolentVerbsParser {
    
   Set violentWords;
   String globalPpath = "/violentVerbsDictionary.txt";
   
   
   public ViolentVerbsParser(LexicalizedParser globalParser) throws IOException
   {
        setViolentDictionary();
   }
   
   private void setViolentDictionary() throws IOException
   {
       
        violentWords = new HashSet<String>();
        
        InputStream in = this.getClass().getResourceAsStream(globalPpath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str = br.readLine()) != null) 
        {
            str = str.toLowerCase();
            str.replaceAll("\\s+","");
            violentWords.add(str);
        }
   }
   
   public List<String> getAllViolentVerbs(Tree tree)
   {    
        List<String> result = new ArrayList();
        for(Tree leave : tree.getLeaves())
        {
            Tree parent = leave.parent(tree);
            if(parent != null)
            {
                if(parent.label().value().contains("VB"))
                {
                    WordTag tag = Morphology.stemStatic(
                            leave.label().value(),
                            parent.label().value()
                    );
                    if(violentWords.contains(tag.value()))
                    {
                        result.add(leave.label().value());
                    }
                }
            }
        }
    
        return result;      
   }
           
}
