package Demo;

import java.io.File;

public class TestCommend {
	
	public void DeleteDatasInFolder(String folderPath) {
		File file = new File(folderPath);
		File [] listFile = file.listFiles();
		for (File fileInFolder : listFile) {
			System.out.println(fileInFolder.getAbsolutePath());
			if (fileInFolder.isDirectory()) {
				this.DeleteDatasInFolder(fileInFolder.getAbsolutePath());
			}
			else {
				fileInFolder.delete();
			}
		}
		file.delete();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestCommend t = new TestCommend();
		t.DeleteDatasInFolder("/home/zoubohao/DataScp0/");
		
		
	}

}
