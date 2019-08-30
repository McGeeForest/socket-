import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainTread implements Runnable {

	private Thread thread;
	private String thread_name;
	private ObjectInputStream thread_ois;
	private FileOutputStream thread_fos;
	private String serverZipPath;
	private String serverPath;
	private String uploaded_files;

	public MainTread(InputStream ins,String serverPath,String uploaded_files, String threadName) {
		
		try {			
			this.thread_name = threadName;
			this.thread_ois = new ObjectInputStream(ins);
			
			// 命名为当前时间.zip
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 设置日期格式
			String date = df.format(new Date());// Date()为获取当前系统时间，也可使用当前时间戳
			System.out.println(date);
			String zipName = date;
			this.serverZipPath = serverPath + zipName + ".zip"; // 传输来的zip的全路径
			// 将客户端上传的文件存到服务器里面
			this.thread_fos = new FileOutputStream(serverZipPath);
			
			this.serverPath = serverPath;
			this.uploaded_files = uploaded_files;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		// 从客户端收取zip文件并获得zip文件全路径
		getZip(thread_ois, thread_fos, serverPath);

		// 解压上传到服务器的zip文件
		// File serverZipFile = new File(serverZipPath);
		System.out.println(serverZipPath + "***");
		unZip(serverZipPath, uploaded_files);

		// 清空上传的zip临时存放的文件夹
		delAllFile(serverPath);
		
	}

	public void start() {
		if (this.thread == null) {
			this.thread = new Thread(this, this.thread_name);
			this.thread.start();
		}
	}
	
	
	/**
	 * 删除指定文件夹下的所有文件
	 * 
	 * @param path
	 *            上传的zip临时存放地址
	 * @return boolen 是否删除成功
	 */
	public void delAllFile(String path) {

		File file = new File(path);

		if (!file.exists()) {// 判断是否待删除目录是否存在
			System.err.println("The dir are not exists!");
		}

		String[] tempList = file.list();// 取得当前目录下所有文件和文件夹

		for (String fileName : tempList) {
			System.gc(); // 加上确保文件能删除，不然可能删不掉
			File temp = new File(path + "\\" + fileName);
			if (temp.isDirectory()) {// 判断是否是目录
				delAllFile(temp.getAbsolutePath());// 递归调用，删除目录里的内容
				temp.delete();// 删除空目录
			} else {
				if (!temp.delete()) {// 直接删除文件
					System.err.println("Failed to delete " + fileName);
				}

			}
		}
		System.out.println(">>>>>>zip文件删除成功！(" + path + ")<<<<<<");
	}

	public void getZip(ObjectInputStream ois, FileOutputStream fos , String serverPath) {

		// System.out.println("有客户端链接成功！");

		try {

			// System.out.println("开始读取文件……");

			// 1.读取的数组长度
			int lenght = ois.readInt();
			// 2.读取次数
			long times = ois.readLong();
			// 3.读取最后一次字节长度
			int lastBytes = ois.readInt();
			byte[] bytes = new byte[lenght];
			// ois.read(bytes);
			/**
			 * 和read相比，都是读一个字节数组 read不一定能读完2048个字节里面的全部字节，会继续往下走
			 * readFully是通信里面才用到的函数，将会判断流里面还有没有字节剩余
			 * 有一种情况，会在字节数组里面没有将全部字节传送到位，而阻塞在网络上，或者阻塞到发送端的网卡上
			 * readFully方法，会等大byte数组中所有数据全部读取完毕后，继续往下执行
			 * read方法，会检测流中是否还有剩余字节，如果没有，则会继续往下执行
			 * 
			 **/
			// 循环读取文件
			while (times > 1) {
				ois.readFully(bytes);
				fos.write(bytes);
				fos.flush();
				times--;
			}
			// 处理最后一次字节数组
			bytes = new byte[lastBytes];
			ois.readFully(bytes);
			fos.write(bytes);
			fos.flush();

			ois.close();
			fos.close();

			// socket.close();

		} catch (Exception e) {
		}
	}

	/**
	 * zip解压
	 * 
	 * @param srcFile zip源文件的全路径
	 * @param unzipFilePath 解压后的目标文件夹
	 */
	public void unZip(String sourceFilePath, String unzipFilePath) {
		File sourceFile = new File(sourceFilePath);
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		FileInputStream fis = null;

		if (sourceFile.exists() == false) {
			System.out.println(">>>>>> 待解压的文件目录：" + sourceFilePath + " 不存在. <<<<<<");
		} else {
			try {
				System.out.println(">>>>>> 开始解压：" + sourceFilePath + " <<<<<<");
				zipFile = new ZipFile(sourceFile);
				zis = new ZipInputStream(new FileInputStream(sourceFile));
				while ((zipEntry = zis.getNextEntry()) != null) {
					String fileName = zipEntry.getName();
					File temp = new File(unzipFilePath + "\\" + fileName);
					System.out.println(fileName + ">>>>>>解压到" + unzipFilePath);
					if (!temp.getParentFile().exists()) {
						temp.getParentFile().mkdirs();
					}
					fos = new FileOutputStream(temp);
					InputStream is = zipFile.getInputStream(zipEntry);
					int len = 0;
					while ((len = is.read()) != -1) {
						fos.write(len);
					}
					is.close();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// 关闭流
				try {
					if (null != fos)
						fos.close();
					if (null != fis)
						fis.close();
					if (null != zis)
						zis.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}

	}
	
	
	
	
	
	
	

	public String getThread_name() {
		return thread_name;
	}

	public void setThread_name(String thread_name) {
		this.thread_name = thread_name;
	}

	public InputStream getThread_ois() {
		return thread_ois;
	}

	public void setThread_ois(ObjectInputStream thread_ois) {
		this.thread_ois = thread_ois;
	}

	public FileOutputStream getThread_fos() {
		return thread_fos;
	}

	public void setThread_fos(FileOutputStream thread_fos) {
		this.thread_fos = thread_fos;
	}



}
