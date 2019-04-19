/*
 * Copyright (c) 2011, Regents of the University of Massachusetts Amherst 
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:

 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 * 		and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 * 		and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the University of Massachusetts Amherst nor the names of its contributors 
 * 		may be used to endorse or promote products derived from this software without specific prior written 
 * 		permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import net.sourceforge.jeval.EvaluationException;
import engine.ConfigFlags;
import engine.EngineController;
import engine.Profiling;
import stream.ParseStockStreamConfig;
import stream.StockStreamConfig;
import stream.StreamController;



/**
 * The interface
 * @author haopeng
 *
 */
public class CommandLineUI {
	/**
	 * The main entry to run the engine under command line
	 * 
	 * @param args the inputs 
	 * 0: the nfa file location 
	 * 1: the stream config file
	 * 2: print the results or not (1 for print, 0 for not print)
	 * 3: use sharing techniques or not, ("sharingengine" for use, nothing for not use)
	 */
	public static void main(String args[]) throws CloneNotSupportedException, EvaluationException, IOException{


		String nfaFileLocation = "/Users/shuhaozhang/briskstream/CEP/src/main/resources/application/Q2/Q2.query";
		String streamConfigFile = "/Users/shuhaozhang/briskstream/CEP/src/main/resources/application/Q2/Q2.stream";
		
	
		
		String engineType = null;
		if(args.length > 0){
			nfaFileLocation = args[0];
		}
		
		if(args.length > 1){
			streamConfigFile = args[1];
		}
		
		if(args.length > 2){
			if(Integer.parseInt(args[2])== 1){
				ConfigFlags.printResults = true;
			}else{
				ConfigFlags.printResults = false;
			}
		}
		
		if(args.length > 3){
			engineType = args[3];
		}
		ParseStockStreamConfig.parseStockEventConfig(streamConfigFile);
		
				
		StreamController myStreamController = null; 
		
		EngineController myEngineController = new EngineController();
		
		if(engineType != null){
			myEngineController = new EngineController(engineType);
		}
		myEngineController.setNfa(nfaFileLocation);
		
				
		for(int i = 0; i < 20; i ++){
			//repreat multiple times for a constant performance
			myEngineController.initializeEngine();
			System.gc();
			System.out.println("\nRepeat No." + (i+1) +" is started...");
			myStreamController = new StreamController(StockStreamConfig.streamSize,"StockEvent");
			myStreamController.generateStockEventsAsConfigType();			
			myEngineController.setInput(myStreamController.getMyStream());			
			//myStreamController.printStream();
			myEngineController.runEngine();
			System.out.println("\nProfiling results for repeat No." + (i+1) +" are as follows:");
			Profiling.printProfiling();
		}
}
}