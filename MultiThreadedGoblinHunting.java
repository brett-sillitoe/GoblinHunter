//Binay carver for jpegs
//Brett Sillitoe

import java.io.*;
import java.util.Scanner;

public class MultiThreadedGoblinHunting {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Please enter the name of the file you want to carve.");
		String inputFile = scanner.nextLine();
		
		System.out.println("Please enter the directory where you want to save the jpegs. Hint: double up the backslashes. Ex: C:\\\\Desktop");
		String directory = scanner.nextLine();
		
		
		//starting timer
		long startTime = System.nanoTime();
		
		
		Carver[] carvers = new Carver[20];
		Thread[] thread = new Thread[20];
		int fileNum = 0;
		long inputStreamCounter = 0;
		
		try(
			InputStream inputStream = new FileInputStream(inputFile);
				){

			//input stream returns bytes in the form of integer values
			int byte1;
			
			//jpegs start with ff d8 ff (e0 or e1)
			//The decimal equivalent is 255 216 255 224
			while ((byte1 = inputStream.read()) != -1){
				inputStreamCounter++;
				
				//Checking for header
				//ff
				if(byte1 == 255){
					byte1 = inputStream.read();
					inputStreamCounter++;
					//d8
					if(byte1 == 216){
						byte1 = inputStream.read();
						inputStreamCounter++;
						//ff
						if(byte1 == 255){
							byte1 = inputStream.read();
							inputStreamCounter++;
							//e0 or e1
							if(byte1 == 224 || byte1 == 225){
								fileNum++;
								String outputFile = "Goblin" + String.valueOf(fileNum) + ".jpg";
								OutputStream outputStream = new FileOutputStream(directory + "\\" + outputFile);
								carvers[fileNum-1] = new Carver(inputFile, outputStream, byte1, inputStreamCounter);
								thread[fileNum-1] = new Thread(carvers[fileNum-1]);
								thread[fileNum-1].start();
							}
						}
					}
				}
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		
		//joining the threads so the main thread doesn't finish until the rest are done
		try{
			for(int i = 0; i < fileNum; i++){
				thread[i].join();
			}
		}
		catch (InterruptedException ex){
			System.out.print(ex);
		}
		
		
		//stopping timer
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;  // in milliseconds.
		
		System.out.println("Time: " + duration + " ms");
	}
}

class Carver implements Runnable{
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private int byte4;
	private long inputStreamCounter;
	
	Carver(String inputFile, OutputStream outputStream, int byte4, long inputStreamCounter){
		try{
			this.inputStream = new FileInputStream(inputFile);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		this.outputStream = outputStream;
		this.byte4 = byte4;
		this.inputStreamCounter = inputStreamCounter;
	}
	
	public void run(){
		
		int byteRead;
		
		try{
			//write the header
			outputStream.write(255);
			outputStream.write(216);
			outputStream.write(255);
			outputStream.write(byte4);
			
			//skipping to the correct spot in the new inputStream
			inputStream.skip(inputStreamCounter);
			
			//write loop until you find the footer ff d9 -> 255 217
			while((byteRead = inputStream.read()) != -1){
				outputStream.write(byteRead);
				
				//if you find an ff look for a d9
				if (byteRead == 255){
					byteRead = inputStream.read();
					outputStream.write(byteRead);
					
					if(byteRead == 217){
						break;
					}
				}
			}	
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
}


