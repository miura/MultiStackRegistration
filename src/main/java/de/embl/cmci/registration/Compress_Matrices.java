package de.embl.cmci.registration;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Comparable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import java.lang.String;

public class Compress_Matrices implements PlugIn  {

private String loadPathA;
private String loadPathB;
private String savePath;

private double[][] srcPtsA = new double[3][2];
private double[][] srcPtsB = new double[3][2];
private double[][] tgtPtsA = new double[3][2];
private double[][] tgtPtsB = new double[3][2];

//the set of points to use when squishing and output
private double[][] gblPts = new double[3][2];
private boolean gblPtsInit;

private SortedSet listA = new TreeSet();
private SortedSet listB = new TreeSet();

private int globalIndex;

public void run (final String arg) {
	loadPathA="";
	loadPathB="";
	savePath="";
    
	Runtime.getRuntime().gc();
	
	GenericDialog gd = new GenericDialog("Compress Matrices");
	gd.addMessage("Please select two array files in the order you want them applied.");
	
	gd.showDialog();
	if (gd.wasCanceled()) {
		return;
	}
	gblPtsInit=false;
	final Frame t = new Frame();
	final FileDialog fl = new FileDialog( t, "Load first transformation file", FileDialog.LOAD);
	fl.setVisible(true);
	if (fl.getFile() == null){
		IJ.error("Action cancelled");
		return;
	}
	loadPathA = fl.getDirectory()+fl.getFile();
	processFile("A");
	
	
	//final Frame t2 = new Frame();
	final FileDialog f2 = new FileDialog( t, "Load second transformation file", FileDialog.LOAD);
	f2.setVisible(true);
	if (f2.getFile() == null){
		IJ.error("Action cancelled");
		return;
	}
	loadPathB = f2.getDirectory()+f2.getFile();
	processFile("B");
	
	//final Frame t3 = new Frame();
	final FileDialog f3 = new FileDialog( t, "Save new transformation file", FileDialog.SAVE);
	f3.setVisible(true);
	if (f3.getFile() == null){
		IJ.error("Action cancelled");
		return;
	}
	savePath = f3.getDirectory()+f3.getFile();
	//processFile("B");
	squishArrays();
	
} /* end run */

public void squishArrays(){
	Iterator itA = listA.iterator();
	Iterator itB = listB.iterator();
	listElement currA;
	listElement currB=null;

	startWriting();
	if (itB.hasNext()) 
		currB= (listElement)itB.next();
	while (itA.hasNext()){
		currA= (listElement)itA.next();
		while(currB != null && currA.compareTo(currB)>0){
			keepWriting(currB);
			if (itB.hasNext())
				currB= (listElement)itB.next();
			else
				currB = null;
		}
        
		if (currB != null && currA.compareTo(currB)<0){//A comes before B, so write A and keep going
			keepWriting(currA);
		}else if (currB != null && currA.compareTo(currB)==0){//A==B.  Squish them together and write the result
			//SQUISH
			keepWriting(sliceSquish(currA,currB));
			if (itB.hasNext()) 
                currB= (listElement)itB.next();
            else
                currB= null;
		}else{
			keepWriting(currA);
		}
	}
	if (currB != null) keepWriting(currB);
	while(itB.hasNext()){		
		currB= (listElement)itB.next();
		keepWriting(currB);
	}
}

public listElement sliceSquish(listElement oA,listElement oB){
    /*
    Take the effect that matrix A has on a set of constants, run that through 
    matrix B, then use the result to derive a new matrix that does both at once.
    */	
	double[][] m;
	double[][] m1,m2;
	double[][] pts=new double[3][2];
	double[][] pts2=new double[3][2];

	m=oA.matrix;
	pts[0][0]=(m[0][0]*gblPts[0][0] + m[0][1]*gblPts[0][1] + m[0][2]);
	pts[0][1]=(m[1][0]*gblPts[0][0] + m[1][1]*gblPts[0][1] + m[1][2]);
	pts[1][0]=(m[0][0]*gblPts[1][0] + m[0][1]*gblPts[1][1] + m[0][2]);
	pts[1][1]=(m[1][0]*gblPts[1][0] + m[1][1]*gblPts[1][1] + m[1][2]);
	pts[2][0]=(m[0][0]*gblPts[2][0] + m[0][1]*gblPts[2][1] + m[0][2]);
	pts[2][1]=(m[1][0]*gblPts[2][0] + m[1][1]*gblPts[2][1] + m[1][2]);	
	
    //These strings are very useful for debugging, though the pts calc does clog the screen
    String s=oA.index+" "+oB.index+"\n"+oA.index+":First transformation\n"+pts[0][0]+" "+pts[0][1]+"\n"+pts[1][0]+" "+pts[1][1]+"\n"+pts[2][0]+" "+pts[2][1];

	m=oB.matrix;
	pts2[0][0]=(m[0][0]*pts[0][0] + m[0][1]*pts[0][1] + m[0][2]);
	pts2[0][1]=(m[1][0]*pts[0][0] + m[1][1]*pts[0][1] + m[1][2]);
	pts2[1][0]=(m[0][0]*pts[1][0] + m[0][1]*pts[1][1] + m[0][2]);
	pts2[1][1]=(m[1][0]*pts[1][0] + m[1][1]*pts[1][1] + m[1][2]);
	pts2[2][0]=(m[0][0]*pts[2][0] + m[0][1]*pts[2][1] + m[0][2]);
	pts2[2][1]=(m[1][0]*pts[2][0] + m[1][1]*pts[2][1] + m[1][2]);
	
	pts[0][0]=(m[0][0]*gblPts[0][0] + m[0][1]*gblPts[0][1] + m[0][2]);
	pts[0][1]=(m[1][0]*gblPts[0][0] + m[1][1]*gblPts[0][1] + m[1][2]);
	pts[1][0]=(m[0][0]*gblPts[1][0] + m[0][1]*gblPts[1][1] + m[0][2]);
	pts[1][1]=(m[1][0]*gblPts[1][0] + m[1][1]*gblPts[1][1] + m[1][2]);
	pts[2][0]=(m[0][0]*gblPts[2][0] + m[0][1]*gblPts[2][1] + m[0][2]);
	pts[2][1]=(m[1][0]*gblPts[2][0] + m[1][1]*gblPts[2][1] + m[1][2]);	
	
    String s2="\n\n"+oB.index+":Second transformation (raw)\n"+pts[0][0]+" "+pts[0][1]+"\n"+pts[1][0]+" "+pts[1][1]+"\n"+pts[2][0]+" "+pts[2][1];
    String s3="\n\n"+oB.index+":Second transformation (from first)\n"+pts2[0][0]+" "+pts2[0][1]+"\n"+pts2[1][0]+" "+pts2[1][1]+"\n"+pts2[2][0]+" "+pts2[2][1];

	m = solveEQ(gblPts,pts2);

	pts[0][0]=(m[0][0]*gblPts[0][0] + m[0][1]*gblPts[0][1] + m[0][2]);
	pts[0][1]=(m[1][0]*gblPts[0][0] + m[1][1]*gblPts[0][1] + m[1][2]);
	pts[1][0]=(m[0][0]*gblPts[1][0] + m[0][1]*gblPts[1][1] + m[0][2]);
	pts[1][1]=(m[1][0]*gblPts[1][0] + m[1][1]*gblPts[1][1] + m[1][2]);
	pts[2][0]=(m[0][0]*gblPts[2][0] + m[0][1]*gblPts[2][1] + m[0][2]);
	pts[2][1]=(m[1][0]*gblPts[2][0] + m[1][1]*gblPts[2][1] + m[1][2]);		
	
    //IJ.error(s+s2+s3+"\n\nBoth transformations\n"+pts[0][0]+" "+pts[0][1]+"\n"+pts[1][0]+" "+pts[1][1]+"\n"+pts[2][0]+" "+pts[2][1]);

	return new listElement(oA.index,m);
}

public void startWriting(){
	try{
		FileWriter fw= new FileWriter(savePath);
		fw.write("MultiStackReg Transformation File\n");
		fw.write("File Version 1.0\n");
		fw.write("1");
		fw.close();
	}catch(IOException e){}
}

public void keepWriting(Object ob){
	if(!(ob instanceof listElement))
        throw new ClassCastException();
	double[][] m=((listElement)ob).matrix;
	try{
		FileWriter fw= new FileWriter(savePath,true);
		fw.append("\nAFFINE\n");
		fw.append("Source img: "+((listElement)ob).index+" Target img: "+((listElement)ob).index+"\n");
		

		fw.append(gblPts[0][0]+"\t"+gblPts[0][1]+"\n");
		fw.append(gblPts[1][0]+"\t"+gblPts[1][1]+"\n");
		fw.append(gblPts[2][0]+"\t"+gblPts[2][1]+"\n");
        fw.append("\n");
        fw.append((m[0][0]*gblPts[0][0] + m[0][1]*gblPts[0][1] + m[0][2])+"\t"+(m[1][0]*gblPts[0][0] + m[1][1]*gblPts[0][1] + m[1][2])+"\n");
		fw.append((m[0][0]*gblPts[1][0] + m[0][1]*gblPts[1][1] + m[0][2])+"\t"+(m[1][0]*gblPts[1][0] + m[1][1]*gblPts[1][1] + m[1][2])+"\n");
		fw.append((m[0][0]*gblPts[2][0] + m[0][1]*gblPts[2][1] + m[0][2])+"\t"+(m[1][0]*gblPts[2][0] + m[1][1]*gblPts[2][1] + m[1][2])+"\n");
        
		fw.close();
	}catch(IOException e){}
}

public void processFile(String AorB){
	String transform="AFFINE";
	try{
		FileReader fr;
		if (AorB == "A"){
			fr=new FileReader(loadPathA);
		}else{
			fr=new FileReader(loadPathB);
		}
		BufferedReader br = new BufferedReader(fr);
		String record;
		int separatorIndex;
		int sepInd2;
		String[] fields=new String[3];
		
		record = br.readLine();	
		record = record.trim();
		if (record.equals("Transformation")){
			//the given file was not made in multistackreg, so let's treat it like a turboreg file
			record = br.readLine().trim();	
			if (record.equals("RIGID_BODY")) 
				transform="RIGID_BODY";
			else if (record.equals("AFFINE")) 
				transform="AFFINE";
			else{
				IJ.error("We are only processing rigid body and affine \ntransformations at this time.  Sorry.");
				System.exit(0);
			}
			
			for (int j=0;j<8;j++)
				record = br.readLine();	
			for (int i=0;i<3;i++){
				record = br.readLine();		
				record = record.trim();
				separatorIndex = record.indexOf('\t');			
				fields[0] = record.substring(0, separatorIndex);
				fields[1] = record.substring(separatorIndex);
				fields[0] = fields[0].trim();
				fields[1] = fields[1].trim();
				srcPtsA[i][0]=(new Double(fields[0])).doubleValue();
				srcPtsA[i][1]=(new Double(fields[1])).doubleValue();
			}                   
            if (gblPtsInit != true){ //we're initializing, copy the first set of points as reference
                for (int i=0;i<3;i++){
                    gblPts[i][0]=srcPtsA[i][0];
                    gblPts[i][1]=srcPtsA[i][1];
                }
                if (transform=="RIGID_BODY"){ //we need the points to be acolinear
                    gblPts[1][0] *= 0.5;
                    gblPts[2][0] *= 1.5;
                    gblPts[1][1] = gblPts[2][1];                
                }
                gblPtsInit=true;
            }     
			record = br.readLine();
			record = br.readLine();
			for (int i=0;i<3;i++){
				record = br.readLine();		
				record = record.trim();
				separatorIndex = record.indexOf('\t');			
				fields[0] = record.substring(0, separatorIndex);
				fields[1] = record.substring(separatorIndex);
				fields[0] = fields[0].trim();
				fields[1] = fields[1].trim();
				tgtPtsA[i][0]=(new Double(fields[0])).doubleValue();
				tgtPtsA[i][1]=(new Double(fields[1])).doubleValue();
			}
			GenericDialog gd = new GenericDialog("Compress Matrices");
			gd.addMessage("What images should this be applied to? (3, 5-10, etc)");
			gd.addStringField("","",30);
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			
			String index = gd.getNextString();
			SortedSet set = stringToSet(index);
			
			listElement tmp;
			Iterator it = set.iterator();
		    while (it.hasNext()) {
		        // Get element
				Object element = it.next();
				
				double[][] mat;// = solveEQ(srcPtsA,tgtPtsA);
				
				if (transform.equals("RIGID_BODY")){
					mat = solveRBEQ(srcPtsA,tgtPtsA);
					//TODO
					tgtPtsB[0][0]=500;
					tgtPtsB[1][0]=tgtPtsB[0][1]=250;
					tgtPtsB[2][0]=tgtPtsB[1][1]=tgtPtsB[2][1]=750;
					srcPtsB[0][0]=mat[0][1]*tgtPtsB[0][0]-mat[0][2]*tgtPtsB[0][1]-mat[0][0];
					srcPtsB[0][1]=mat[1][2]*tgtPtsB[0][1]-mat[1][1]*tgtPtsB[0][0]-mat[1][0];
					
					srcPtsB[1][0]=mat[0][1]*tgtPtsB[1][0]-mat[0][2]*tgtPtsB[1][1]-mat[0][0];
					srcPtsB[1][1]=mat[1][2]*tgtPtsB[1][1]-mat[1][1]*tgtPtsB[1][0]-mat[1][0];
					
					srcPtsB[2][0]=mat[0][1]*tgtPtsB[2][0]-mat[0][2]*tgtPtsB[2][1]-mat[0][0];
					srcPtsB[2][1]=mat[1][2]*tgtPtsB[2][1]-mat[1][1]*tgtPtsB[2][0]-mat[1][0];
					 
					mat = solveEQ(srcPtsB,tgtPtsB);
					
					transform="AFFINE";
					
				}else
					mat = solveEQ(srcPtsA,tgtPtsA);
				tmp = new listElement(((Integer)element).intValue(),mat);
				if (AorB == "A")
					listA.add(tmp);
				else
					listB.add(tmp);
		    }
		}else if (record.equals("MultiStackReg Transformation File")){
			//this is a multistackreg transformation file
			record = br.readLine();
			record = br.readLine();   
            
            int twoStackAlignment=1;
            record = record.trim();
            twoStackAlignment=(new Integer(record)).intValue();
            double[][] globalTransform = {
                {1.0, 0.0, 0.0},
                {0.0, 1.0, 0.0},
                {0.0, 0.0, 1.0}
            };
            double gx,gy;
            gx=gy=0;
			record = br.readLine();
			int cont=1;
			int newIndex,srcIndex;
			listElement tmp,global,otmp;
            global = new listElement(0,globalTransform);
			if (record.equals("RIGID_BODY"))
				transform="RIGID_BODY";
			else if (!record.equals("AFFINE")) {
				IJ.error("We are only processing rigid body and affine \ntransformations at this time.  Sorry.");
				System.exit(0);
			}
            boolean forward = false;
			while (cont>0){
				record = br.readLine();
				if (record==null) break;
                separatorIndex = record.indexOf(" Target");	
                fields[0] = record.substring(separatorIndex+12).trim();
                srcIndex=(new Integer(fields[0])).intValue();
				separatorIndex = record.indexOf(" Target");	
				fields[0] = record.substring(0, separatorIndex);
				separatorIndex = fields[0].indexOf(":");	
				fields[1] = fields[0].substring(separatorIndex+1).trim();
				newIndex = (new Integer(fields[1])).intValue();
                if (!forward && twoStackAlignment==0 && 1<newIndex && newIndex>srcIndex) {
                    //at the halfway point in the stack, we need to reset the global matrix
		            global.matrix[0][0] = global.matrix[1][1] = global.matrix[2][2] = 1.0;
		            global.matrix[0][1] = global.matrix[0][2] = global.matrix[1][0] = 0.0;
		            global.matrix[1][2] = global.matrix[2][0] = global.matrix[2][1] = 0.0;
                    forward=true;
                }
                globalIndex=newIndex;
				for (int i=0;i<3;i++){
					record = br.readLine();		
					record = record.trim();
					separatorIndex = record.indexOf('\t');			
					fields[0] = record.substring(0, separatorIndex);
					fields[1] = record.substring(separatorIndex);
					fields[0] = fields[0].trim();
					fields[1] = fields[1].trim();
					srcPtsA[i][0]=(new Double(fields[0])).doubleValue();
					srcPtsA[i][1]=(new Double(fields[1])).doubleValue();
				}
                if (gblPtsInit != true){ //we're initializing, copy the first set of points as reference
                    for (int i=0;i<3;i++){
                        gblPts[i][0]=srcPtsA[i][0];
                        gblPts[i][1]=srcPtsA[i][1];
                    }
                    if (transform=="RIGID_BODY"){ //we need the points to be acolinear
                        gblPts[1][0] *= 0.5;
                        gblPts[2][0] *= 1.5;
                        gblPts[1][1] = gblPts[2][1];                
                    }
                    gblPtsInit=true;
                }   
				record = br.readLine();
				for (int i=0;i<3;i++){
					record = br.readLine();		
					record = record.trim();
					separatorIndex = record.indexOf('\t');			
					fields[0] = record.substring(0, separatorIndex);
					fields[1] = record.substring(separatorIndex);
					fields[0] = fields[0].trim();
					fields[1] = fields[1].trim();
					tgtPtsA[i][0]=(new Double(fields[0])).doubleValue();
					tgtPtsA[i][1]=(new Double(fields[1])).doubleValue();
				}
				double[][] mat;
				if (transform.equals("RIGID_BODY")){
					mat = solveRBEQ(srcPtsA,tgtPtsA);
					//TODO
                    //IJ.error("SRC:\n"+srcPtsA[0][0]+" "+srcPtsA[0][1]+"\n"+srcPtsA[1][0]+" "+srcPtsA[1][1]+"\n"+srcPtsA[2][0]+" "+srcPtsA[2][1]
                    //    +"\n\nTGT:\n"+tgtPtsA[0][0]+" "+tgtPtsA[0][1]+"\n"+tgtPtsA[1][0]+" "+tgtPtsA[1][1]+"\n"+tgtPtsA[2][0]+" "+tgtPtsA[2][1]);

					srcPtsB[0][0]=500;
					srcPtsB[1][0]=srcPtsB[0][1]=250;
					srcPtsB[2][0]=srcPtsB[1][1]=srcPtsB[2][1]=750;
                
                    //uncomment to test that the transformation can generate the points what spawned it
                    /*for (int i=0;i<3;i++){
                        srcPtsB[i][0]=tgtPtsA[i][0];
                        srcPtsB[i][1]=tgtPtsA[i][1];
                    } */                
                    
					tgtPtsB[0][0]=mat[0][1]*srcPtsB[0][0]+mat[0][2]*srcPtsB[0][1]+mat[0][0];
					tgtPtsB[0][1]=mat[1][2]*srcPtsB[0][1]+mat[1][1]*srcPtsB[0][0]+mat[1][0];
					
					tgtPtsB[1][0]=mat[0][1]*srcPtsB[1][0]+mat[0][2]*srcPtsB[1][1]+mat[0][0];
					tgtPtsB[1][1]=mat[1][2]*srcPtsB[1][1]+mat[1][1]*srcPtsB[1][0]+mat[1][0];
					
					tgtPtsB[2][0]=mat[0][1]*srcPtsB[2][0]+mat[0][2]*srcPtsB[2][1]+mat[0][0];
					tgtPtsB[2][1]=mat[1][2]*srcPtsB[2][1]+mat[1][1]*srcPtsB[2][0]+mat[1][0];

                    //String s=mat[0][0]+" "+mat[0][1]+" "+mat[0][2]+"\n"+mat[1][0]+" "+mat[1][1]+" "+mat[1][2];					 
                    //IJ.error(s+"\n\n"+tgtPtsB[0][0]+" "+tgtPtsB[0][1]+"\n"+tgtPtsB[1][0]+" "+tgtPtsB[1][1]+"\n"+tgtPtsB[2][0]+" "+tgtPtsB[2][1]);                    

					mat = solveEQ(srcPtsB,tgtPtsB);
					
					transform="AFFINE";
					
				}else
					mat = solveEQ(srcPtsA,tgtPtsA);
				otmp = new listElement(newIndex,mat,transform);
                if (twoStackAlignment == 0){
                    //String s=global.matrix[0][0]+" "+global.matrix[0][1]+" "+global.matrix[0][2]+"\n"+global.matrix[1][0]+" "+global.matrix[1][1]+" "+global.matrix[1][2]+"\n"+global.matrix[2][0]+" "+global.matrix[2][1]+" "+global.matrix[2][2]+"\n";
                    double[][] rescued = {
			            {global.matrix[0][0], global.matrix[0][1], global.matrix[0][2]},
			            {global.matrix[1][0], global.matrix[1][1], global.matrix[1][2]},
			            {global.matrix[2][0], global.matrix[2][1], global.matrix[2][2]}
		            };
		            for (int i = 0; (i < 3); i++) {
			            for (int j = 0; (j < 3); j++) {
				            global.matrix[i][j] = 0.0;
				            for (int k = 0; (k < 3); k++) {
					            global.matrix[i][j] += otmp.matrix[i][k] * rescued[k][j];
				            }
			            }
		            }
                    double[][] rescued2 = {
			                        {global.matrix[0][0], global.matrix[0][1], global.matrix[0][2]},
			                        {global.matrix[1][0], global.matrix[1][1], global.matrix[1][2]},
			                        {global.matrix[2][0], global.matrix[2][1], global.matrix[2][2]}
		                        };
                    tmp=new listElement(newIndex,rescued2,transform);
                    //IJ.error(newIndex+"\n"+tmp.matrix[0][0]+" "+tmp.matrix[0][1]+" "+tmp.matrix[0][2]+"\n"+tmp.matrix[1][0]+" "+tmp.matrix[1][1]+" "+tmp.matrix[1][2]+"\n"+tmp.matrix[2][0]+" "+tmp.matrix[2][1]+" "+tmp.matrix[2][2]+"\n");
                }else{
                    tmp=otmp;
                }
				if (AorB == "A")
					listA.add(tmp);
				else
					listB.add(tmp);
				record = br.readLine();
				record = br.readLine();
				//IJ.error(record.trim());
				if (record != null)
					if (record.equals("RIGID_BODY"))
						transform="RIGID_BODY";
					else if (record.equals("AFFINE")) 
						transform="AFFINE";
					else{
						IJ.error("We are only processing rigid body and affine \ntransformations at this time.  Sorry.");
						System.exit(0);
					}
			}
			
		}else{
			//dunno what this is.  Crash horribly
		}
	}catch(FileNotFoundException e){
		IJ.error("Could not find proper transformation matrix.");
	}catch (IOException e) {
		IJ.error("Error reading from file.");
	}
	
}

public SortedSet stringToSet(String in){
	SortedSet set = new TreeSet();
	in.trim();
	int separatorIndex = in.indexOf(",");	
	int sepInd2;
	String record;
	String[] fields=new String[3];
	while (separatorIndex != -1){
		record=in.substring(0, separatorIndex).trim();
		in=in.substring(separatorIndex+1).trim();
		separatorIndex = in.indexOf(",");
		sepInd2=record.indexOf("-");
		if ( sepInd2 != -1){
			fields[0]=record.substring(0, sepInd2).trim();
			fields[1]=record.substring(sepInd2+1).trim();
			for (int i=(new Integer(fields[0])).intValue();i<=(new Integer(fields[1])).intValue();i++){
				set.add(new Integer(i));
			}
		}else{
			set.add(new Integer(record));
		}
	}
	sepInd2=in.indexOf("-");
	if ( sepInd2 != -1){
		fields[0]=in.substring(0, sepInd2).trim();
		fields[1]=in.substring(sepInd2+1).trim();
		for (int i=(new Integer(fields[0])).intValue();i<=(new Integer(fields[1])).intValue();i++){
			set.add(new Integer(i));
		}
	}else{
		set.add(new Integer(in));
	}
			
	return set;
}

public double[][] solveRBEQ(double[][] srcPts, double[][] tgtPts){
	final double angle = Math.atan2(srcPts[2][0] - srcPts[1][0],
		srcPts[2][1] - srcPts[1][1])
		- Math.atan2(tgtPts[2][0] - tgtPts[1][0],
		tgtPts[2][1] - tgtPts[1][1]);
	final double c = Math.cos(angle);
	final double s = Math.sin(angle);
	double[][] matrix = new double[3][3];
	matrix[0][0] = tgtPts[0][0]
		- c * srcPts[0][0] + s * srcPts[0][1];
	matrix[0][1] = c;
	matrix[0][2] = -s;
	matrix[1][0] = tgtPts[0][1]
		- s * srcPts[0][0] - c * srcPts[0][1];
	matrix[1][1] = s;
	matrix[1][2] = c;
	
	matrix[2][0]=matrix[2][1]=0;
	matrix[2][2]=1;

	//IJ.error(matrix[0][0]+" "+matrix[0][1]+" "+matrix[0][2]+"\n"+matrix[1][0]+" "+matrix[1][1]+" "+matrix[1][2]);
	return matrix;
}

public double[][] solveEQ(double[][] srcPts, double[][] tgtPts){
	double[][] outPts = new double[3][3];
	
	outPts[2][0]=outPts[2][1]=0;
	outPts[2][2]=1;
    
	
	double[][] tmp = new double[2][3];
	
	tmp[0][0] = srcPts[1][1]-(srcPts[0][1]*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	tmp[0][1] = 1-(1*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	tmp[0][2] = tgtPts[1][1]-(tgtPts[0][1]*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	
	tmp[1][0] = srcPts[2][1]-(srcPts[0][1]*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	tmp[1][1] = 1-(1*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	tmp[1][2] = tgtPts[2][1]-(tgtPts[0][1]*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	
	tmp[1][1] = tmp[1][1]-(tmp[0][1]*tmp[1][0]/(tmp[0][0]+0.00000001));
	tmp[1][2] = tmp[1][2]-(tmp[0][2]*tmp[1][0]/(tmp[0][0]+0.00000001));
	
	
	double C = tmp[1][2]/(tmp[1][1]+0.00000001);
	double B = (tmp[0][2]-C*tmp[0][1])/(tmp[0][0]+0.00000001);
	double A = (tgtPts[0][1]-C-B*srcPts[0][1])/(srcPts[0][0]+0.00000001);
	
	outPts[1][0] = A;
	outPts[1][1] = B;
	outPts[1][2] = C;
    
    
	
	tmp[0][0] = srcPts[1][1]-(srcPts[0][1]*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	tmp[0][1] = 1-(1*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	tmp[0][2] = tgtPts[1][0]-(tgtPts[0][0]*srcPts[1][0]/(srcPts[0][0]+0.00000001));
	
	tmp[1][0] = srcPts[2][1]-(srcPts[0][1]*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	tmp[1][1] = 1-(1*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	tmp[1][2] = tgtPts[2][0]-(tgtPts[0][0]*srcPts[2][0]/(srcPts[0][0]+0.00000001));
	
	tmp[1][1] = tmp[1][1]-(tmp[0][1]*tmp[1][0]/(tmp[0][0]+0.00000001));
	tmp[1][2] = tmp[1][2]-(tmp[0][2]*tmp[1][0]/(tmp[0][0]+0.00000001));
	
	
	C = tmp[1][2]/(tmp[1][1]+0.00000001);
	B = (tmp[0][2]-C*tmp[0][1])/(tmp[0][0]+0.00000001);
	A = (tgtPts[0][0]-C-B*srcPts[0][1])/(srcPts[0][0]+0.00000001);
	
	outPts[0][0] = A;
	outPts[0][1] = B;
	outPts[0][2] = C;
    
	return outPts;
}

public class listElement implements Comparable{

public int index;
private double[][] matrix;
private String transform;

public listElement(int ind, double[][] mat){
	index=ind;
	matrix=mat;
	transform="AFFINE";
}

public listElement(int ind, double[][] mat, String trans){
	index=ind;
	matrix=mat;
	transform=new String(trans);
}

public int compareTo(Object o2){
    if(!(o2 instanceof listElement))
        throw new ClassCastException();

    int result = ((listElement)o2).index;//compare(new Integer(index),new Integer(((listElement)o2).index));
	
    return index - result;
}


}




}






