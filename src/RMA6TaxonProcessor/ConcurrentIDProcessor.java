package RMA6TaxonProcessor;

import java.util.HashSet;
import java.util.concurrent.Callable;

import NCBI_MapReader.NCBI_MapReader;

public class ConcurrentIDProcessor implements Callable<readIDFinder>{
	readIDFinder idFinder;
	int id;
	NCBI_MapReader reader;
	String inDir; 
	String fileName;
	HashSet<String> IDs;
	public ConcurrentIDProcessor(readIDFinder idFinder, int id, NCBI_MapReader reader,String inDir, String fileName,HashSet<String> IDs){
		this.idFinder = idFinder;
		this.id = id;
		this.reader = reader;
		this.inDir = inDir;
		this.fileName = fileName;
		this.IDs = IDs;
	}
	@Override
	public readIDFinder call(){
		try{
			idFinder.process(id,reader,inDir, fileName, IDs);
		}catch(Exception e){
			e.printStackTrace();
		}
		return this.idFinder;
	}
}
