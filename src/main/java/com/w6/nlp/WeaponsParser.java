package com.w6.nlp;

import com.w6.data.CollectionOfWords;
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

public class WeaponsParser {
   private Set weapons;
   private final String globalPath = "/weapons.txt";
   
   
   public WeaponsParser(LexicalizedParser globalParser) throws IOException
   {
        weapons = new HashSet<String>();
        
        InputStream in =
            this.getClass().getResourceAsStream(globalPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str = br.readLine()) != null) 
        {
            str = str.toLowerCase();
            str.replaceAll("\\s+","");
            weapons.add(str);
        }
   }
   
   public List<String> getAllWeapons(DependencyTree dependencyTree, Tree tree)
   {
       List<String> fromWhat = new ArrayList();
       
        tree.getLeaves().forEach((leave) -> {
            Tree parent = leave.parent(tree);
            if(parent != null)
            {
                
                WordTag tag = Morphology.stemStatic(
                        leave.label().value(),
                        parent.label().value()
                );
                
                if(weapons.contains(tag.value()))
                {
                    fromWhat.add(leave.label().value());
                }
            }
        });
       
       List<String> listOfWeapons = new ArrayList<>();
       
       List<CollectionOfWords> collections = dependencyTree.getCollectionsFromWords(fromWhat);
       
       collections.forEach((collection) -> {
           listOfWeapons.add(collection.getCollectionAsString());
       });
       
       return listOfWeapons;
   }
    
}
