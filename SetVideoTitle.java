import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 批量设置视频元数据中的 title
 * <p>
 * 将文件名（不包含文件后缀）作为视频元数据中的 Title
 * <p>
 * 需要事先安装 <a href="https://exiftool.org/">ExifTool</a>，并配置好环境变量
 */
public class SetVideoTitle {

    /**
     * 文件列表
     */
    static List<File> files = new ArrayList<>();

    /**
     * 视频文件格式，后于过滤非视频文件
     */
    static List<String> videoTypes = new ArrayList<>() {{
        add(".mp4");
        add(".mkv");
        add(".mov");
        add(".m4v");
        add(".wmv");
        add(".avi");
        add(".rmvb");
        add(".flv");
        add(".f4v");
    }};

    public static void main(String[] args) throws IOException {
        System.out.println();
        System.out.println("====== Welcome to MDRenamer ======");

        // 获取工作目录
        String path = getPath();

        // 获取是否递归目录标识
        boolean recurse = getRecurseFlag();

        // 收集需要处理的文件
        File directory = new File(path);
        getFiles(directory, recurse);


        System.out.println("All of the following files will be processed:");
        System.out.println("============================================");
        for (int i = 0; i < files.size(); i++) {
            System.out.println(i + 1 + ": " + files.get(i).getPath());
        }
        System.out.println("============================================");

        System.out.println("enter \"exit\" to exit, or enter anything else to continue:");
        Scanner in = new Scanner(System.in);
        String exit = in.nextLine();
        if ("exit".equalsIgnoreCase(exit)) {
            System.out.println("bye~~");
            return;
        }

        System.out.println("this will take some times, you can use \"control+c\" to stop the procedure");
        long startTime = System.currentTimeMillis();
        setMetaData();
        long endDate = System.currentTimeMillis();
        long total = (endDate - startTime) / 1000;
        System.out.println("all done, total time spent "+ total +" seconds, please check the output log above.");
    }

    public static String getPath() {
        System.out.println("please enter the folder directory you want to process，or enter \".\" to use current directory.");
        Scanner in = new Scanner(System.in);
        String path = in.nextLine();
        if (path == null || "".equals(path)) {
            getPath();
        }
        assert path != null;
        return path.trim();
    }

    public static boolean getRecurseFlag() {
        System.out.println("enter \"1\" to use current directory only, enter \"2\" to recurse current directory");
        Scanner in = new Scanner(System.in);
        String recurseNum = in.nextLine();
        if (recurseNum == null || "".equals(recurseNum)) {
            getRecurseFlag();
        }
        if (!"1".equals(recurseNum) && !"2".equals(recurseNum)) {
            getRecurseFlag();
        }
        return "2".equals(recurseNum);
    }

    public static void getFiles(File directory, Boolean recurse) {
        File[] listFiles = directory.listFiles();
        if (listFiles == null) {
            System.out.println("there is no file in this directory, the program will exit automatically.");
            return;
        }
        for (File currentFile : listFiles) {
            if (recurse && currentFile.isDirectory()) {
                // 递归遍历目录
                getFiles(currentFile, true);
            }
            if (currentFile.isFile()) {
                String fileType = getFileType(currentFile);
                // 过滤掉非视频文件
                if (!videoTypes.contains(fileType)) {
                    continue;
                }
                files.add(currentFile);
            }
        }
        if (files.size() == 0) {
            System.out.println("there is no video in this directory, the program will exit automatically.");
        }
    }

    public static void setMetaData() throws IOException {
        for (File file : files) {
            String fileName = file.getName();
            String titleName = fileName.substring(0, fileName.lastIndexOf("."));

            String[] command = {"exiftool", "-api", "LargeFileSupport=1", "-overwrite_original", "-Title=" + titleName, file.getAbsolutePath()};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line.concat(", file name is: ".concat(fileName)));
            }

        }
    }

    public static String getFileType(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
