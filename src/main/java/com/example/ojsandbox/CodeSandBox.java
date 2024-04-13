package com.example.ojsandbox;

import com.example.ojsandbox.model.ExecuteCodeRequest;
import com.example.ojsandbox.model.ExecuteCodeResponse;

import java.io.IOException;

public interface CodeSandBox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException;
}
