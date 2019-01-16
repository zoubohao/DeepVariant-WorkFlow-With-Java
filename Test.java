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


public class Test {
	
	
	public  void JudgeExitStatue(int s) {
		if (s != 0 ) {
			System.out.println("ERROR , the terminal statue is not 0 !!!!");
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
			//################################################################################
			String referencePath = "/home/zoubohao/TestFootScript/hg38T/GRCh38.fasta";
			String modelPath = "/home/zoubohao/TestFootScript/DeepVariant-Model/model.ckpt";
			String binaryPath = "/home/zoubohao/TestFootScript/DeepVariant-Binary/";
			String bamFilePath = "/home/zoubohao/DataScp" + i + "/" + scpFile + ".bam";
			String finalOutputPath = "/home/zoubohao/FinalOutputs/" + scpFile + "VCF.vcf.gz";
			//Make example commend 
			Thread.sleep(60*1000);
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
			int S = processMake.waitFor();
			this.JudgeExitStatue(S);
			//find calling 
			Thread.sleep(2*60*1000);
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
				this.JudgeExitStatue(S);
			}
			//transform format
			Thread.sleep(2*60*1000);
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
			this.JudgeExitStatue(S);
			//删除中间文件
			File example = new File("/home/zoubohao/" + scpFile + "Examples.tfrecord.gz");
			example.delete();
			File calling = new File("/home/zoubohao/" + scpFile + "Calling.tfrecord.gz");
			calling.delete();
			//###################################################################################
			//删除文件夹中的数据
			this.DeleteDatasInFolder("/home/zoubohao/DataScp" + i + "/");
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
		for (String name : scpFiles) {
			if (name.equals("CP60000135-N")) {
				System.out.println("Failed");
				break;
			}
			else {
				System.out.println("Not find this file in List !!!");
			}
		}
		System.out.println(scpFiles.size());
	}

}
