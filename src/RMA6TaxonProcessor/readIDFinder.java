package RMA6TaxonProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import NCBI_MapReader.NCBI_MapReader;
import jloda.util.ListOfLongs;
import megan.data.IReadBlock;
import megan.data.IReadBlockIterator;
import megan.data.ReadBlockIterator;
import megan.rma6.ClassificationBlockRMA6;
import megan.rma6.RMA6File;
import megan.rma6.ReadBlockGetterRMA6;

public class readIDFinder {
	private int taxID;
	private int contained = 0;
	private int total=0;
	private ArrayList<String> foundReads = new ArrayList<String>();
	private String quotient = "0"+"/"+"0";
	NCBI_MapReader mapReader;
	public String getQuotient(){
		return this.quotient;
	}
	public ArrayList<String> getReads(){
		return this.foundReads;
	}
	private String getName(int taxId){
		String name;
		if(mapReader.getNcbiIdToNameMap().get(taxId) != null)
			name = mapReader.getNcbiIdToNameMap().get(taxId).replace(' ', '_');
		else
			name = "unassignedName";
		return name;
	}
	public void process(int id,NCBI_MapReader reader,String inDir, String fileName, HashSet<String> IDs){
		this.taxID = id;
		this.mapReader = reader;
		try(RMA6File rma6File = new RMA6File(inDir+fileName, "r")){
			ListOfLongs list = new ListOfLongs();
			Long location = rma6File.getFooterSectionRMA6().getStartClassification("Taxonomy");
			if (location != null) {
			   ClassificationBlockRMA6 classificationBlockRMA6 = new ClassificationBlockRMA6("Taxonomy");
			   classificationBlockRMA6.read(location, rma6File.getReader());
			   if (classificationBlockRMA6.getSum(taxID) > 0) {
				   classificationBlockRMA6.readLocations(location, rma6File.getReader(), taxID, list);
			   }
			 }
			IReadBlockIterator classIt  = new ReadBlockIterator(list, new ReadBlockGetterRMA6(rma6File, true, true, (float) 1.0,(float) 100.00,false,true));
			if(!classIt.hasNext()){ // check if reads are assigned to TaxID if not print to console and skip and set all values to default
				
			}else{
				while(classIt.hasNext()){
					IReadBlock current = classIt.next();
					total++;
					for(String name : IDs)
						if ((current.getReadName().contains(name))){
							foundReads.add(current.getReadHeader());
							foundReads.add(current.getReadSequence());
						contained++;
						}
				}
				quotient = + contained+"/"+total;
			}
			classIt.close();
		}catch(IOException io){
			
		}	
	}
}
