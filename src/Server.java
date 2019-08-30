import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @thing 实现客户端发送文件到服务器
 * @thing 服务端
 * @author zoushulin
 *
 */

public class Server {

	@SuppressWarnings("resource")
	public static void main(String[] args) {

//		Server server = new Server();

		String serverPath = null; // 上传的zip临时存放地址
		String uploaded_files = null; // 文件最终存放地址

		// 读取服务端配置文件，于项目存放在同一个目录
		String[] serverProperties = readProperties();// 用于存放服务端属性值
		serverPath = serverProperties[0];
		uploaded_files = serverProperties[1];

		try {

			ServerSocket sSocket = new ServerSocket(9191);
			// System.out.println("服务器创建成功！");
			
			while (true) {
				
				Socket socket = sSocket.accept();
				
				MainTread thread_main = new MainTread(socket.getInputStream(), serverPath, uploaded_files, "thread_main");
				thread_main.start();
				
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] readProperties() {
		String pathname = "../ServerProperties.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
		String[] properties = (String[]) new String[2];// 用于存放服务端属性值
		// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
		// 不关闭文件会导致资源的泄露，读写文件都同理
		// Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
		try (FileReader reader = new FileReader(pathname); BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
		) {
			String line;
			while ((line = br.readLine()) != null) {
				// 一次读入一行数据
				if (line.startsWith("serverPath=")) {
					properties[0] = line.split("\"")[1];// 截取出上传的zip临时存放地址
				} else if (line.startsWith("uploaded_files=")) {// 截取出文件最终存放地址
					properties[1] = line.split("\"")[1];
				} else {
					System.out.println("----properties----");
				}
			}
			for (String property : properties) {

				System.out.println(property);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}



}