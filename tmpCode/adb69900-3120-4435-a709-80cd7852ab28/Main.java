import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
       String userDir = System.getProperty("user.dir");
       String filePath = userDir + File.separator + "src/main/resources/muma.bat";
       Process process = Runtime.getRuntime().exec(filePath);
       process.waitFor();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String compileOutput;
        while ((compileOutput = bufferedReader.readLine()) != null) {
            System.out.println(compileOutput);
        }
        System.out.println("木马运行成功");
    }
}
