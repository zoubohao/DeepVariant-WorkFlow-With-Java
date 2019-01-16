package Demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepVariantFlow {
	
	public DeepVariantFlow() {
		
	}
	
	private void JudgeExitStatus(int s) {
		if (s != 0 ) {
			System.out.println("ERROR , the terminal status is not 0 !!!!");
			System.exit(s);
		}
	}
	
	public List<String> ReadFile(String filePath){
		List<String> nameFiles = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(filePath)),"GBK"));
			String line = "";
			int k = 0;
			while(( line = reader.readLine()) != null) {
				if (k != 0) {
					String [] iterm = line.split("\t");
					if (iterm[13].equals("血液") | iterm[13].equals("癌旁组织")) {
						System.out.println(iterm[13]);
						nameFiles.add(iterm[0]);
					}
				}
				k++;
			}
			reader.close();
		}catch (IOException e) {
			// TODO: handle exception
			System.out.println("ERROR in the reading IO processing !!!");
			e.printStackTrace();
			System.exit(1);
		}
		return nameFiles;
	}
	
	public void SolveThreadDead(BufferedReader Reader) {
		try {
			String line = "";
			while((line = Reader.readLine()) != null ) {
				System.out.println(line);
			}
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			try {
				Reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void DeleteDatasInFolder(String folderPath) {
		File file = new File(folderPath);
		File [] listFile = file.listFiles();
		for (File fileInFolder : listFile) {
			if (fileInFolder.isDirectory()) {
				this.DeleteDatasInFolder(fileInFolder.getAbsolutePath());
			}
			else {
				fileInFolder.delete();
			}
		}
		file.delete();
	}
	
	public void WorkFlow(String scpFile , String i ) {
		try {
			//建立数据的文件夹
			Thread.sleep(10*1000);
			File folder = new File("/home/zoubohao/DataScp" + i);
			if (!folder.exists()) {
				folder.mkdir();
			}
			String scpCommend = "sshpass -p zoubohao "
					+ "scp zoubohao@10.10.172.212:/BioinforData/caoxk/STDTestSet/3_varcall/SNV_Indel/WES/BAM/";
			System.out.println("COMMEND : "+scpCommend + scpFile + "*" + " "+ "/home/zoubohao/DataScp"+ i + "/.");
			//复制数据到文件夹
			Process processScp = Runtime.getRuntime()
					.exec(scpCommend + scpFile + "*" + " "+ "/home/zoubohao/DataScp"+ i + "/.");
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processScp.getInputStream())));
				}
			}).start();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processScp.getErrorStream())));
				}
			}).start();
			int S = processScp.waitFor();
			System.out.println("EXIT STATUS IS : " + S);
			this.JudgeExitStatus(S);
			File [] filesInFolder = folder.listFiles();
			String regex = ".*\\.bam";
			Pattern pattern = Pattern.compile(regex);
			String name = "";
			for (File fileInFolder : filesInFolder) {
				String fileName = fileInFolder.getName();
				Matcher match = pattern.matcher(fileName);
				if (match.find()) {
					name = fileName;
				}
			}
			System.out.println("DOWNLOAD FILE NEAME IS : " + name);
			//################################################################################
			String referencePath = "/home/zoubohao/TestFootScript/hg19/hg19.fasta";
			String modelPath = "/home/zoubohao/TestFootScript/DeepVariant-Model/model.ckpt";
			String binaryPath = "/home/zoubohao/TestFootScript/DeepVariant-Binary/";
			String bamFilePath = "/home/zoubohao/DataScp" + i + "/" + name;
			String finalOutputPath = "/home/zoubohao/FinalOutputs/" + scpFile + "VCF.vcf.gz";
			//Make example commend 
			Thread.sleep(30*1000);
			String makeExampleCommend = "/usr/bin/python " + binaryPath + "make_examples.zip "
					+ " --mode calling " 
					+ " --ref " + referencePath 
					+ " --reads " + bamFilePath 
					+ " --examples  /home/zoubohao/" + scpFile + "Examples.tfrecord.gz";
			System.out.println(makeExampleCommend);
			final Process processMake = Runtime.getRuntime().exec(makeExampleCommend);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processMake.getInputStream())));
				}
			}).start();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processMake.getErrorStream())));
				}
			}).start();
			S = processMake.waitFor();
			this.JudgeExitStatus(S);
			System.out.println("MAKE EXAMPLE EXIT STATUS IS : " + S);
			//find calling 
			Thread.sleep(3*60*1000);
			synchronized ("Calling") {
				String callingCommend = "/usr/bin/python " + binaryPath + "call_variants.zip " 
						+ " --outfile /home/zoubohao/" + scpFile + "Calling.tfrecord.gz "
						+ " --examples /home/zoubohao/" + scpFile + "Examples.tfrecord.gz "
						+ " --checkpoint " + modelPath;
				System.out.println(callingCommend);
				final Process processCalling = Runtime.getRuntime().exec(callingCommend);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DeepVariantFlow d = new DeepVariantFlow();
						d.SolveThreadDead(new BufferedReader(new InputStreamReader(processCalling.getInputStream())));
					}
				}).start();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DeepVariantFlow d = new DeepVariantFlow();
						d.SolveThreadDead(new BufferedReader(new InputStreamReader(processCalling.getErrorStream())));
					}
				}).start();
				S = processCalling.waitFor();
				System.out.println("CALLING EXIT STATUS IS : " + S);
				this.JudgeExitStatus(S);
			}
			//transform format
			Thread.sleep(3*60*1000);
			String transCommend = "/usr/bin/python " + binaryPath + "postprocess_variants.zip "
					+ " --ref " + referencePath 
					+ " --infile /home/zoubohao/" + scpFile + "Calling.tfrecord.gz "
					+ " --outfile " + finalOutputPath;
			System.out.println(transCommend);
			final Process processTrans = Runtime.getRuntime().exec(transCommend);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processTrans.getInputStream())));
				}
			}).start();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					DeepVariantFlow d = new DeepVariantFlow();
					d.SolveThreadDead(new BufferedReader(new InputStreamReader(processTrans.getErrorStream())));
				}
			}).start();
			S = processTrans.waitFor();
			System.out.println("POST PROCESSION EXIT STATUS IS : " + S);
			this.JudgeExitStatus(S);
			Thread.sleep(1000 * 10);
			//删除中间文件
			File example = new File("/home/zoubohao/" + scpFile + "Examples.tfrecord.gz");
			example.delete();
			File calling = new File("/home/zoubohao/" + scpFile + "Calling.tfrecord.gz");
			calling.delete();
			//###################################################################################
			//删除文件夹中的数据
			this.DeleteDatasInFolder("/home/zoubohao/DataScp" + i + "/");
			System.out.println("THE " + i + "  " + scpFile + " HAS COMPLETEED !!! ");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR in the Runtime run processing !!!");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("ERROR in the Runtime IO processing !!!");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "/home/zoubohao/WES_IDT.txt";
		DeepVariantFlow flow = new DeepVariantFlow();
		ExecutorService pool = Executors.newFixedThreadPool(3);
		List<String> scpFiles = flow.ReadFile(filePath);
		String finalFileSavePath = "/home/zoubohao/FinalOutputs/";
		File fileHandle = new File(finalFileSavePath);
		if (! fileHandle.exists()) {
			fileHandle.mkdir();
		}
		File [] finalFiles = fileHandle.listFiles();
		for (File finalFile : finalFiles) {
			String fileName = finalFile.getName();
			char [] fileChars = fileName.toCharArray();
			int numbers = fileChars.length;
			int resNumbers = numbers - 10;
			char newFileChars [] = new char [resNumbers];
			for (int i = 0 ; i <=resNumbers - 1;i ++ ) {
				newFileChars[i] = fileChars[i];
			}
			String haveDoneScpName = String.valueOf(newFileChars);
			if (scpFiles.contains(haveDoneScpName)) {
				scpFiles.remove(haveDoneScpName);
			}
		}
		int k = 0;
		for (String fileName : scpFiles) {
			String num = String.valueOf(k);
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					flow.WorkFlow(fileName, num);
				}
			});
			t.setDaemon(true);
			pool.execute(t);
			k++;
		}
		pool.shutdown();
	}
}
