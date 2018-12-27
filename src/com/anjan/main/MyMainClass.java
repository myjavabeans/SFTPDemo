package com.anjan.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.anjan.log4j.LoggerFile;
import com.anjan.pojo.ServerDetails;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

public class MyMainClass {
	
	private static String baseDir = ""; // Update the Base Directory
	private static String processId = "";
	private static String logPath = "";
	
	static{
		createBaseDir();
		
		logPath = baseDir+File.separator+"logs"+File.separator;
		
		System.setProperty("logfile.name", logPath+"sftplog.txt");
	}

	static Logger logger = Logger.getLogger(MyMainClass.class);
	
	private static JSch jsch;
	private static Session session;
	private static Channel channel;
	private static ChannelSftp sftpChannel;

	private static File newFile = null;

	/**
	 * This is instance of SftpProgressMonitor
	 */
	final static SftpProgressMonitor monitor = new SftpProgressMonitor() {
		public void init(final int op, final String source, final String target, final long max) {
			System.out.println("sftp processing file...");
		}

		public boolean count(final long count) {
			System.out.println("sftp sending bytes: " + count);
			return true;
		}

		public void end() {
			System.out.println("sftp processing is done...");
		}
	};

	/**
	 * This method is used for Connecting server using Sftp protocol
	 * 
	 * @param serverDetails
	 *            - Object of ServerDetails class
	 */
	public static void connect(ServerDetails serverDetails) {

		logger.info("connecting..." + serverDetails.getHostName());
		try {
			jsch = new JSch();
			session = jsch.getSession(serverDetails.getUserName(), serverDetails.getHostName(),
					serverDetails.getPortNumber());
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(serverDetails.getPassword());
			session.connect();

			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp) channel;

		} catch (JSchException e) {
			logger.error("JschException", e);
		}

	}

	/**
	 * This method is used for disconnecting Sftp
	 */
	public static void disconnect() {
		logger.info("disconnecting...");
		
		if(sftpChannel != null){
			sftpChannel.disconnect();
		}
		
		if(channel != null){
			channel.disconnect();
		}
		
		if(session != null){
			session.disconnect();
		}
		
	}

	/**
	 * This method is used for Uploading file from local to Remote Server
	 * 
	 * @param serverDetails
	 *            - Destination Server Details
	 * @param file
	 *            - File Object
	 */
	private static boolean uploadFile(ServerDetails serverDetails, File file) {

		FileInputStream fis = null;
		connect(serverDetails);
		try {
			
			Files.copy(file.toPath(), new File(baseDir + processId + File.separator +file.getName()+".processed").toPath());
			
			// Change to output directory
			sftpChannel.cd(serverDetails.getFileLocation());

			// Upload file
			fis = new FileInputStream(file);
			sftpChannel.put(fis, file.getName(), monitor);

			fis.close();
			logger.info("File uploaded successfully to " + serverDetails.getFileLocation());

		} catch (Exception e) {
			logger.error("Exception", e);
			return false;
		} finally {
			disconnect();
		}
		return true;
	}

	/**
	 * This method is used for Downloading file from Remote Server to Local
	 * 
	 * @param serverDetails
	 *            - Source Server Details
	 * @return
	 */
	private static boolean downloadFile(ServerDetails serverDetails) {

		byte[] buffer = new byte[1024];
		BufferedInputStream bis;
		connect(serverDetails);

		try {
			// Change to output directory
			String cdDir = (serverDetails.getFileLocation()).substring(0,
					(serverDetails.getFileLocation()).lastIndexOf("/") + 1);
			sftpChannel.cd(cdDir);

			File file = new File((serverDetails.getFileLocation()));
			bis = new BufferedInputStream(sftpChannel.get(file.getName(), monitor));

			newFile = new File(baseDir + processId + File.separator + file.getName());

			// Download file
			OutputStream os = new FileOutputStream(newFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			int readCount;
			while ((readCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, readCount);
			}
			bis.close();
			bos.close();
			logger.info("File downloaded successfully to " + newFile.getAbsolutePath());

		} catch (Exception e) {
			logger.error("Exception", e);
			return false;
		} finally {
			disconnect();
		}

		return true;
	}

	/**
	 * This method is used for transferring file from One server to Another
	 * Server
	 * 
	 * @param srcServer
	 *            - Source Server Details
	 * @param destServer
	 *            - Destination Server Details
	 */
	public static void transferFile(ServerDetails srcServer, ServerDetails destServer) {
		boolean flag = downloadFile(srcServer);

		if (flag) {
			flag = uploadFile(destServer, newFile);

			if (flag) {
				logger.info("Transfer Process ID - [" + processId + "] Successful!!!");
			} else {
				logger.info("Transfer Process ID - [" + processId + "] Failed!!!");
			}
		} else {
			logger.error("Aborted Process ID - [" + processId + "] !!!");
		}

	}

	/**
	 * This method is used to take user input
	 * 
	 * @param flag
	 *            - False - for Source, True - For Destination
	 * @param ch
	 * @return
	 */
	private static ServerDetails userInput(boolean flag, int ch) {

		Scanner sc = new Scanner(System.in);

		String typeSrc = "";
		String path = "";

		if (!flag) {
			typeSrc = "Source";
		} else {
			typeSrc = "Destination";
		}

		System.out.println("---------------------------------");
		System.out.println("Enter " + typeSrc + " Details");
		System.out.println("---------------------------------");

		System.out.println("Enter Hostname : ");
		String hn = sc.nextLine();
		System.out.println("Enter Username : ");
		String un = sc.nextLine();
		System.out.println("Enter password : ");
		String pass = sc.nextLine();
		System.out.println("Enter Port : ");
		int port = Integer.parseInt(sc.nextLine());
		System.out.println("Enter " + typeSrc + " Path : ");
		String fp = sc.nextLine();

		if (ch == 1) {
			System.out.println("Enter Source Path : ");
			path = sc.nextLine();
		}

		ServerDetails tempServerDetails = new ServerDetails(hn, un, pass, port, fp, path);

		return tempServerDetails;
	}

	/**
	 * This method will create Process Directory
	 */
	public static void createProcessDir() {

		processId = System.currentTimeMillis() + "";

		File file = new File(baseDir + processId);
		file.mkdir();

		System.out.println("All files are present at " + file.getAbsolutePath());
		logger.info("All files are present at " + file.getAbsolutePath());

	}

	public static void createBaseDir() {

		String os = System.getProperty("os.name").toLowerCase();

		if (os.indexOf("win") >= 0) {

			baseDir = "C:"+File.separator+"SFTPDemo"+File.separator;

		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {

			baseDir = File.separator+"opt"+File.separator+"SFTPDemo"+File.separator;

		} else {
			System.out.println("Error!!! Application is not Supported in " + os);
			logger.error("Error!!! Application is not Supported in " + os);
			System.exit(0);
		}

		if (baseDir != null && !baseDir.isEmpty()) {
			File directory = new File(baseDir);

			if (!directory.exists()) {
				directory.mkdirs();
			}
			
		}
		
		File logDir = new File(logPath);
		if (!logDir.exists()) {
			logDir.mkdirs();
		}

	}

	public static void main(String args[]) {
		
		LoggerFile.setUpLogger(logPath);

		System.out.println("*************************************************************");
		System.out.println("Disclaimer : SFTP Application Supports only Windows and Linux");
		System.out.println("*************************************************************");
		
		logger.info("All Application Files is present at "+baseDir);

		Scanner scanner = new Scanner(System.in);
		int ch = 0;

		do {

			System.out.println("*********************************");
			System.out.println("| Please Choose Action\t\t|");
			System.out.println("*********************************");
			System.out.println("| 1. Upload File\t\t|");
			System.out.println("| 2. Download File\t\t|");
			System.out.println("| 3. Transfer File\t\t|");
			System.out.println("| 4. Exit\t\t\t|");
			System.out.println("*********************************");

			ch = scanner.nextInt();

			switch (ch) {
			case 1:

				createProcessDir();

				ServerDetails destServer = userInput(true, ch);

				File newFile = new File(destServer.getSrcPath());
				boolean flag = uploadFile(destServer, newFile);

				if (flag) {
					System.out.println("Upload Process ID - [" + processId + "] Successful!!!");
					logger.info("Upload Process ID - [" + processId + "] Successful!!!");
				} else {
					System.out.println("Upload Process ID - [" + processId + "] Failed!!!");
					logger.error("Upload Process ID - [" + processId + "] Failed!!!");
				}

				break;
			case 2:

				createProcessDir();

				ServerDetails srcServer = userInput(false, ch);
				flag = downloadFile(srcServer);

				if (flag) {
					System.out.println("Download Process ID - [" + processId + "] Successful!!!");
					logger.info("Download Process ID - [" + processId + "] Successful!!!");
				} else {
					System.out.println("Download Process ID - [" + processId + "] Failed!!!");
					logger.error("Download Process ID - [" + processId + "] Failed!!!");
				}

				break;
			case 3:

				createProcessDir();

				srcServer = userInput(false, ch);
				destServer = userInput(true, ch);

				transferFile(srcServer, destServer);

				break;
			default:
				System.out.println("Bye Bye!!!");
			}

		} while (ch > 0 && ch < 4);

	}

}
