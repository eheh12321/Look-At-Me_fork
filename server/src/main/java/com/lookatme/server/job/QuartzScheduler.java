package com.lookatme.server.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuartzScheduler extends QuartzJobBean {
    @Getter @Setter
    private String jobName;
    @Getter @Setter
    private JobLauncher jobLauncher;
    @Getter @Setter
    private JobLocator jobLocator;
    private static int count = 0;
    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            Job job = jobLocator.getJob(jobName);
            JobParameters params = new JobParametersBuilder()
                    .addString("jobID", String.format("%d-%d", System.currentTimeMillis(), ++count))
                    .toJobParameters();
            jobLauncher.run(job, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
