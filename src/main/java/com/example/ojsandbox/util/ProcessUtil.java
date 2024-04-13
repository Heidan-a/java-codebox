package com.example.ojsandbox.util;

import cn.hutool.core.util.StrUtil;
import com.example.ojsandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;

@Component
public class ProcessUtil {
    /**
     * 进程信息以及执行
     *
     * @param process
     * @return
     * @throws Exception
     */
    public static ExecuteMessage getProcessMessage(Process process, String opName) {
        ExecuteMessage message = new ExecuteMessage();
        int exitValue = 0;
        try {
            exitValue = process.waitFor();

            message.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String compileOutput;
                StringBuilder sb = new StringBuilder();
                while ((compileOutput = bufferedReader.readLine()) != null) {
                    sb.append(compileOutput);
                }
                message.setMessage(sb.toString());
            } else {
                System.out.println(opName + "错误码：" + exitValue);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String compileOutput;
                StringBuilder sb = new StringBuilder();
                while ((compileOutput = bufferedReader.readLine()) != null) {
                    sb.append(compileOutput);
                }
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorCompileOutput;
                StringBuilder errorsb = new StringBuilder();
                while ((errorCompileOutput = errorBufferedReader.readLine()) != null) {
                    errorsb.append(errorCompileOutput);
                }
                message.setMessage(errorsb.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }
    /**
     * 交互性进程信息以及执行
     *
     * @param process
     * @return
     * @throws Exception
     */
    public static ExecuteMessage getInteractProcessMessage(Process process, String opName,String args) {
        ExecuteMessage message = new ExecuteMessage();
        int exitValue = 0;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            InputStream input = process.getInputStream();
            OutputStream output = process.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(output);
            String[] s = args.split(" ");
            String join = StrUtil.join("\n",s) + "\n";
            outputStreamWriter.write(join);
            outputStreamWriter.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
            String compileOutput;
            StringBuilder sb = new StringBuilder();
            while ((compileOutput = bufferedReader.readLine()) != null) {
                sb.append(compileOutput);
            }
            message.setMessage(sb.toString());
            outputStreamWriter.close();
            output.close();
            input.close();
            process.destroy();
            message.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }


}
