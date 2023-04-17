package com.lookatme.server.job;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SomeBatchJobWithTaskletAdapter {
    public void businessLogic() {
        log.warn(">>>> MethodInvokingTaskletAdapter를 이용한 Tasklet 구현");
    }
}
