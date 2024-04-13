package com.example.ojsandbox;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.example.ojsandbox.model.ExecuteCodeRequest;
import com.example.ojsandbox.model.ExecuteCodeResponse;
import com.example.ojsandbox.model.ExecuteMessage;
import com.example.ojsandbox.model.JudgeInfo;
import com.example.ojsandbox.security.DefaultSecurityManager;
import com.example.ojsandbox.security.DenySecurityManager;
import com.example.ojsandbox.security.MySecurityManager;
import com.example.ojsandbox.util.ProcessUtil;

public class JavaNativeCodeSandBox implements CodeSandBox {
    private final static String GLOBAL_CODE_DIR_PATH = "tmpCode";
    private final static String DEFAULT_RUN_CLASS_NAME = "Main.java";
    private final static long TIME_LIMIT = 5000L;
    private final static List<String> blackList = Arrays.asList("Files","exec");
    private final static WordTree WORD_TREE;
    private final static String SECURITY_MANAGER_PATH = "C:\\Users\\yang2\\Desktop\\oj-sandbox\\src\\main\\resources\\security";
    private final static String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }


    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        request.setInputList(Arrays.asList("1 2","3 4"));
        //String code = ResourceUtil.readStr("testCode/testCodeArgs/Main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/error/ExecFileError.java", StandardCharsets.UTF_8);
        request.setCode(code);
        request.setLanguage("java");
        ExecuteCodeResponse response = javaNativeCodeSandBox.executeCode(request);
        System.out.println(response);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        //System.setSecurityManager(new DenySecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //字典树检查
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if(foundWord != null){
//            System.out.println("包含敏感词:" + foundWord.getFoundWord());
//            return null;
//        }

        //用户文件夹
        String userDir = System.getProperty("user.dir");
        //所有用户代码区
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_PATH;
        //每个用户的代码目录
        String userCodePathDIR = globalCodePathName + File.separator + UUID.randomUUID();
        //用户代码文件
        String userCodePath = userCodePathDIR + File.separator + DEFAULT_RUN_CLASS_NAME;

        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        //编译代码
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        System.out.println(compileCmd);
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage processMessage = ProcessUtil.getProcessMessage(compileProcess, "编译");
            System.out.println(processMessage);
        } catch (Exception e) {
            return getExceptionResponse(e);
        }
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        //执行代码
        for (String inputArg : inputList) {

            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s  MySecurityManager %s",SECURITY_MANAGER_PATH, inputArg);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodePathDIR,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArg);
            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodePathDIR,inputArg);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_LIMIT);
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtil.getProcessMessage(runProcess, "运行");
//              ExecuteMessage executeMessage = ProcessUtil.getInteractProcessMessage(runProcess, "运行", inputarg);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                return getExceptionResponse(e);
            }
        }
        //收集结果输出
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            //寻找错误信息是否存在
            if (StrUtil.isNotBlank(errorMessage)) {
                response.setMessage(errorMessage);
                //设置错误状态码
                response.setStatus(3);
                break;
            }
            //设置输出
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(time, maxTime);
            }
        }
        //如果输出信息里面没有报错，无跳出的话就设置为1
        if (outputList.size() == executeMessageList.size()) {
            response.setStatus(1);
        }
        response.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(maxTime);
        response.setJudgeInfo(judgeInfo);
        //清理代码
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodePathDIR);
            if (del) {
                System.out.println("删除" + (del ? "成功" : "失败"));
            }
        }
        return response;
    }

    private ExecuteCodeResponse getExceptionResponse(Throwable e) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(2);
        response.setOutputList(new ArrayList<>());
        response.setJudgeInfo(new JudgeInfo());
        response.setMessage(e.getMessage());
        return response;
    }
}
