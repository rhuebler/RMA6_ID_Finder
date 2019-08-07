package utility;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import NCBI_MapReader.NCBI_MapReader;
import RMA6Processor.RMA6Processor;
import behaviour.Filter;
/**
 * Write overall Summy file from processed RMA6Files
 * @author huebler
 *
 */
public class SummaryWriter {
	/**
	 * @param List<Future<RMA6Processor>> processedFiles, NCBI_MapReader mapReader, Set<Integer> processedIDs,
	 * String outDir Logger,Warning
	 * @throws none thrown all caught
	 */
	private NCBI_MapReader mapReader;
	private List<Future<RMA6Processor>> processedFiles;
	private Set<Integer> processedIDs;
	private String outDir;
	private Logger warning;
	private Filter behave;
	public SummaryWriter(List<Future<RMA6Processor>> pFiles, NCBI_MapReader mReader, String oDir, Logger warning, Filter behave){
		this.processedFiles = pFiles;
		this.mapReader = mReader;
		this.outDir = oDir;
		this.warning = warning;
		this.behave = behave;
		
	}
	public void process(){
		setProcessedIds();
		if(behave == Filter.NON){
			prepareOutput(behave);
		}else if(behave == Filter.ANCIENT){
			prepareOutput(behave);
		}else if(behave == Filter.ALL){
			prepareOutput(behave);
		}else if(behave == Filter.NONDUPLICATES){
			prepareOutput(behave);
		}else if(behave == Filter.NON_ANCIENT){
			prepareOutput(Filter.NON);
			prepareOutput(Filter.ANCIENT);
		}
		else if(behave == Filter.IDS){
			processContained();
		}
		
	}
	private void processContained(){
		List<String> summary = new ArrayList<String>();
		   String header ="Node"; // could and should be its own function 
		   String reads = "Total_Count";
		   boolean first = true;	   
		   for(Future<RMA6Processor> future : processedFiles){
			   RMA6Processor current;
			   try {
				   current = future.get();
				   header+="\t" + current.getfileName();
				   reads += "\t" + current.getTotalCount();
				   HashMap<Integer,String> fileResults = new  HashMap<Integer,String>();
				  fileResults = current.getContained();
				  
				   if(first ==true){
					   for(int id : processedIDs){
						   String line;
							if( mapReader.getNcbiIdToNameMap().get(id) != null){
								line = mapReader.getNcbiIdToNameMap().get(id).replace(' ', '_');
							}else{
								line = "unasigned";
							}
						   if(fileResults.containsKey(id)){
							   line+= "\t"+fileResults.get(id);
							   summary.add(line);
						   }else{
							   line+= "\t"+"0/0";
							   summary.add(line);
						   }
					   first = false;
					   }//for
				   	}else{
					   int i = 0;
					   for(int id : processedIDs){ 
						   String line = summary.get(i);
						   if(fileResults.containsKey(id)){
							   line+= "\t"+fileResults.get(id);
							   summary.set(i,line);
						   }else{
							   line+= "\t"+"0/0";
							   summary.set(i,line);
						   }
					  
						   i++;
					   }
				   	}
				  
			}catch (InterruptedException e) {
				warning.log(Level.SEVERE, "Interuption Error" , e);
			} catch (ExecutionException e) {
				warning.log(Level.SEVERE, "Execution Error", e);
			}
			   
		   }//for
		   summary.sort(null);
		   summary.add(0,reads);
		   summary.add(0,header);
		   writeSummary(summary,outDir+"RunSummary"+".txt");
	}
	private void setProcessedIds(){
		Set<Integer> pIDs = new HashSet<>();
		try{
		for(Future<RMA6Processor> future : processedFiles)
			pIDs.addAll(future.get().getContainedIDs());
		this.processedIDs = pIDs;
		}catch(InterruptedException ie){
			warning.log(Level.SEVERE, "Error", ie);
		}catch(ExecutionException ee){
			warning.log(Level.SEVERE, "Error", ee);
		}
	}
	private void prepareOutput(Filter switcher) {
		   List<String> summary = new ArrayList<String>();
		   String header ="Node"; // could and should be its own function 
		   String reads = "Total_Count";
		   boolean first = true;	   
		   for(Future<RMA6Processor> future : processedFiles){
			   RMA6Processor current;
			   try {
				   current = future.get();
				   header+="\t" + current.getfileName();
				   reads += "\t" + current.getTotalCount();
				   HashMap<Integer,Integer> fileResults = new  HashMap<Integer,Integer>();
				   if(switcher==Filter.NON)
					   fileResults = current.getSumLine();
				   else if(switcher==Filter.ANCIENT)
					   fileResults = current.getAncientLine();
				   else if(switcher==Filter.NONDUPLICATES)
					   fileResults = current.getNonDuplicateLine();
				   else if(switcher==Filter.ALL)
					   fileResults = current.getAncientNonDuplicateLine();
				  
				   if(first ==true){
					   for(int id : processedIDs){
						   String line;
							if( mapReader.getNcbiIdToNameMap().get(id) != null){
								line = mapReader.getNcbiIdToNameMap().get(id).replace(' ', '_');
							}else{
								line = "unasigned";
							}
						   if(fileResults.containsKey(id)){
							   line+= "\t"+fileResults.get(id);
							   summary.add(line);
						   }else{
							   line+= "\t"+0;
							   summary.add(line);
						   }
					   first = false;
					   }//for
				   	}else{
					   int i = 0;
					   for(int id : processedIDs){ 
						   String line = summary.get(i);
						   if(fileResults.containsKey(id)){
							   line+= "\t"+fileResults.get(id);
							   summary.set(i,line);
						   }else{
							   line+= "\t"+0;
							   summary.set(i,line);
						   }
					  
						   i++;
					   }
				   	}
				  
			}catch (InterruptedException e) {
				warning.log(Level.SEVERE, "Interuption Error" , e);
			} catch (ExecutionException e) {
				warning.log(Level.SEVERE, "Execution Error", e);
			}
			   
		   }//for
		   summary.sort(null);
		   summary.add(0,reads);
		   summary.add(0,header);
		   if(switcher==Filter.NON)
			   writeSummary(summary,outDir+"/default/"+"RunSummary"+".txt");
		   else if(switcher==Filter.ANCIENT)
			   writeSummary(summary,outDir+"/ancient/"+"RunSummary"+".txt");
		   else if(switcher==Filter.NONDUPLICATES)
			   writeSummary(summary,outDir+"/nonDuplicates/"+"RunSummary"+".txt");
		   else if(switcher==Filter.ALL)
			   writeSummary(summary,outDir+"/ancientNonDuplicates/"+"RunSummary"+".txt");
		   
	}

	private void writeSummary(List<String> summary,String outDir) {
		try{
		Path file = Paths.get(outDir);
		Files.write(file, summary, Charset.forName("UTF-8"));
		}catch(IOException io){
			warning.log(Level.SEVERE, "Error", io);
		}
	}
}
